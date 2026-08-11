package com.balabala.homework.service;

import com.alibaba.fastjson2.JSONObject;
import com.balabala.homework.entity.Answer;
import com.balabala.homework.entity.ErrorBook;
import com.balabala.homework.entity.Question;
import com.balabala.homework.entity.Submission;
import com.balabala.homework.entity.User;
import com.balabala.homework.repository.AICorrectionCacheRepository;
import com.balabala.homework.repository.AnswerRepository;
import com.balabala.homework.repository.ErrorBookRepository;
import com.balabala.homework.repository.SubmissionRepository;
import com.balabala.homework.repository.UserRepository;
import com.balabala.homework.websocket.CorrectionWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class AICorrectionService {

    private final DeepSeekService deepSeekService;
    private final AnswerRepository answerRepository;
    private final SubmissionRepository submissionRepository;
    private final ErrorBookRepository errorBookRepository;
    private final UserRepository userRepository;
    private final CorrectionWebSocketHandler webSocketHandler;
    private final AICorrectionCacheRepository cacheRepository;

    @Async("taskExecutor")
    public void correctAnswersAsync(Long submissionId, List<Answer> answers,
                                     Long studentId, String studentName, Long homeworkId,
                                     String homeworkTitle, Long teacherId) {
        log.info("开始异步AI批改, submissionId={}, 题目数量={}", submissionId, answers.size());
        
        int totalScore = 0;
        int correctedCount = 0;
        
        for (Answer answer : answers) {
            try {
                // 逐题调用AI批改，避免超时
                Question question = answer.getQuestion();
                if (question == null) {
                    log.warn("题目为空, answerId={}", answer.getId());
                    continue;
                }
                
                log.info("开始批改题目, answerId={}, questionId={}", answer.getId(), question.getId());
                
                // 转换答案格式，便于AI理解
                String studentAnswer = convertAnswerForAI(answer.getContent(), question.getType());
                String correctAnswer = convertAnswerForAI(question.getCorrectAnswer(), question.getType());
                
                // 解析选项
                List<String> options = parseOptions(question.getOptions());
                
                // 使用带缓存的批改方法
                JSONObject result = deepSeekService.correctAnswerWithCache(
                    question.getId(),
                    question.getContent(),
                    studentAnswer,
                    correctAnswer,
                    question.getType(),
                    question.getScore(),
                    options
                );
                
                Integer aiScore = result.getInteger("score");
                String aiAnalysis = result.getString("analysis");
                
                // 确保解析不为空
                if (aiAnalysis == null || aiAnalysis.trim().isEmpty()) {
                    if (result.getBoolean("isCorrect") != null && result.getBoolean("isCorrect")) {
                        aiAnalysis = "回答正确！" + studentAnswer + " 是正确答案。";
                    } else {
                        aiAnalysis = "回答错误。学生答案：" + studentAnswer + "。参考答案：" + correctAnswer + "。请核对答案并复习相关知识点。";
                    }
                }
                
                answer.setAiScore(aiScore);
                answer.setAiAnalysis(aiAnalysis);
                answer.setStatus(1); // AI已批改
                answerRepository.save(answer);
                
                // 记录是否来自缓存
                if (result.getBoolean("fromCache") != null && result.getBoolean("fromCache")) {
                    log.info("题目使用缓存批改结果, answerId={}, questionId={}", answer.getId(), question.getId());
                }
                
                if (answer.getAiScore() != null) {
                    totalScore += answer.getAiScore();
                }
                correctedCount++;
                
                log.info("题目批改完成, answerId={}, score={}", answer.getId(), answer.getAiScore());
                
                // 每道题之间添加短暂延迟，避免API限流
                Thread.sleep(500);
            } catch (Exception e) {
                log.error("AI批改题目失败, answerId={}", answer.getId(), e);
            }
        }
        
        // 更新提交记录的总分
        Submission submission = submissionRepository.findById(submissionId).orElse(null);
        if (submission != null) {
            submission.setAiScore(totalScore);
            submission.setTotalScore(totalScore);
            // 如果所有题目都已处理，更新状态为已完成
            if (correctedCount == answers.size()) {
                submission.setStatus(2); // 已完成
                submission.setCorrectTime(LocalDateTime.now());
                submissionRepository.save(submission);
                log.info("AI批改全部完成, submissionId={}, totalScore={}", submissionId, totalScore);
                
                // 发送WebSocket通知给老师和学生（使用预加载的数据，避免懒加载问题）
                sendCorrectionCompleteNotification(studentId, studentName, homeworkId, homeworkTitle, teacherId, submission.getId());
                
                // 收集错题到错题本
                collectWrongAnswers(answers, studentId, homeworkId);
            } else {
                log.info("AI批改部分完成, submissionId={}, 完成{}/{}题, totalScore={}", 
                    submissionId, correctedCount, answers.size(), totalScore);
                submissionRepository.save(submission);
            }
        }
    }
    
    /**
     * 发送批改完成通知（同时通知老师和学生）
     * 参数在调用前已从实体中提取，避免异步线程中的懒加载问题
     */
    private void sendCorrectionCompleteNotification(Long studentId, String studentName, Long homeworkId, 
                                                     String homeworkTitle, Long teacherId, Long submissionId) {
        try {
            // 通知学生
            if (studentId != null) {
                webSocketHandler.sendCorrectionCompleteNotification(
                    studentId, homeworkId, submissionId, homeworkTitle, "CORRECTION_COMPLETE_STUDENT"
                );
            }

            // 通知老师
            if (teacherId != null) {
                String displayStudentName = studentName != null ? studentName : "学生" + studentId;
                webSocketHandler.sendCorrectionCompleteNotification(
                    teacherId, homeworkId, submissionId, homeworkTitle, "CORRECTION_COMPLETE_TEACHER"
                );
            }

            log.info("批改完成通知已发送 - 学生ID: {}, 老师ID: {}, 作业: {}", studentId, teacherId, homeworkTitle);
        } catch (Exception e) {
            log.error("发送批改完成通知失败", e);
        }
    }
    
    /**
     * 转换答案格式，便于AI理解
     * 单选题/多选题：数字索引 "0" -> "A", "1" -> "B"
     * 判断题："true" -> "正确", "false" -> "错误"
     */
    private String convertAnswerForAI(String answer, String questionType) {
        if (answer == null || answer.isEmpty()) {
            return "未作答";
        }
        
        // 单选题：数字索引转字母
        if ("single_choice".equals(questionType)) {
            try {
                int idx = Integer.parseInt(answer);
                if (idx >= 0 && idx < 26) {
                    return String.valueOf((char) ('A' + idx));
                }
            } catch (NumberFormatException e) {
                // 不是数字，返回原值
            }
            return answer;
        }
        
        // 多选题：数字索引数组转字母数组
        if ("multiple_choice".equals(questionType)) {
            try {
                List<String> indices = com.alibaba.fastjson2.JSON.parseArray(answer, String.class);
                if (indices != null) {
                    List<String> letters = new java.util.ArrayList<>();
                    for (String idx : indices) {
                        try {
                            int i = Integer.parseInt(idx);
                            if (i >= 0 && i < 26) {
                                letters.add(String.valueOf((char) ('A' + i)));
                            } else {
                                letters.add(idx);
                            }
                        } catch (NumberFormatException e) {
                            letters.add(idx);
                        }
                    }
                    return String.join(", ", letters);
                }
            } catch (Exception e) {
                // 不是JSON数组，返回原值
            }
            return answer;
        }
        
        // 判断题
        if ("true_false".equals(questionType)) {
            if ("true".equals(answer)) return "正确";
            if ("false".equals(answer)) return "错误";
            return answer;
        }
        
        // 填空题：JSON数组转为顿号分隔
        if ("fill_blank".equals(questionType)) {
            try {
                List<String> answers = com.alibaba.fastjson2.JSON.parseArray(answer, String.class);
                if (answers != null) {
                    return String.join("、", answers);
                }
            } catch (Exception e) {
                // 不是JSON数组，返回原值
            }
            return answer;
        }
        
        return answer;
    }
    
    /**
     * 收集错题到错题本
     * 将答错的题目自动添加到学生的错题本中
     */
    private void collectWrongAnswers(List<Answer> answers, Long studentId, Long homeworkId) {
        try {
            User student = userRepository.findById(studentId).orElse(null);
            if (student == null) {
                log.warn("无法找到学生信息, studentId={}", studentId);
                return;
            }
            
            int addedCount = 0;
            for (Answer answer : answers) {
                // 判断是否为错题：得分为0或null，或者与满分差距较大
                Integer aiScore = answer.getAiScore();
                Question question = answer.getQuestion();
                
                if (question == null) continue;
                
                // 如果得分为0或者是null，认为是错题
                boolean isWrong = (aiScore == null || aiScore == 0);
                
                // 或者得分低于满分的50%也认为是错题
                if (!isWrong && question.getScore() != null && question.getScore() > 0) {
                    double rate = (double) aiScore / question.getScore();
                    isWrong = rate < 0.5;
                }
                
                if (isWrong) {
                    // 检查是否已存在相同错题
                    List<ErrorBook> existing = errorBookRepository.findByStudentIdAndQuestionId(studentId, question.getId());
                    if (!existing.isEmpty()) {
                        log.debug("错题已存在, 跳过添加 - studentId={}, questionId={}", studentId, question.getId());
                        continue;
                    }
                    
                    // 创建错题记录
                    ErrorBook errorBook = new ErrorBook();
                    errorBook.setStudent(student);
                    errorBook.setQuestion(question);
                    errorBook.setWrongAnswer(answer.getContent());
                    errorBook.setCorrectAnswer(question.getCorrectAnswer());
                    errorBook.setAnalysis(answer.getAiAnalysis());
                    errorBook.setKnowledgePoint(question.getKnowledgePoint());
                    errorBook.setMasteryStatus(0); // 未掌握
                    
                    errorBookRepository.save(errorBook);
                    addedCount++;
                    log.info("错题已添加到错题本 - studentId={}, questionId={}, homeworkId={}", 
                        studentId, question.getId(), homeworkId);
                }
            }
            
            log.info("错题收集完成 - studentId={}, 新增错题数={}", studentId, addedCount);
        } catch (Exception e) {
            log.error("收集错题失败 - studentId={}", studentId, e);
        }
    }
    
    /**
     * 解析选项JSON字符串为List
     */
    private List<String> parseOptions(String optionsJson) {
        if (optionsJson == null || optionsJson.isEmpty()) {
            return new ArrayList<>();
        }
        try {
            List<String> options = com.alibaba.fastjson2.JSON.parseArray(optionsJson, String.class);
            return options != null ? options : new ArrayList<>();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }
}
