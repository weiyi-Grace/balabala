package com.balabala.homework.controller;

import com.balabala.homework.dto.Result;
import com.balabala.homework.dto.SubmitHomeworkRequest;
import com.balabala.homework.dto.SubmissionDetailDTO;
import com.balabala.homework.dto.TeacherSubmissionDTO;
import com.balabala.homework.dto.TeacherCorrectionDTO;
import com.balabala.homework.entity.*;
import com.balabala.homework.repository.UserRepository;
import com.balabala.homework.repository.AnswerRepository;
import com.balabala.homework.repository.ErrorBookRepository;
import com.balabala.homework.repository.HomeworkRepository;
import com.balabala.homework.repository.QuestionRepository;
import com.balabala.homework.repository.SubmissionRepository;
import com.balabala.homework.service.AICorrectionService;
import com.balabala.homework.websocket.CorrectionWebSocketHandler;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/submission")
@RequiredArgsConstructor
public class SubmissionController {

    private final SubmissionRepository submissionRepository;
    private final AnswerRepository answerRepository;
    private final HomeworkRepository homeworkRepository;
    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final ErrorBookRepository errorBookRepository;
    private final AICorrectionService aiCorrectionService;
    private final CorrectionWebSocketHandler webSocketHandler;

    /**
     * 提交作业
     */
    @PostMapping
    public Result<Submission> submitHomework(@RequestBody SubmitHomeworkRequest request) {
        Homework homework = homeworkRepository.findById(request.getHomeworkId()).orElse(null);
        if (homework == null) {
            return Result.error("作业不存在");
        }

        User student = userRepository.findById(request.getStudentId()).orElse(null);
        if (student == null) {
            return Result.error("学生不存在");
        }

        Submission saved;
        Optional<Submission> existingOpt = submissionRepository.findByHomeworkIdAndStudentId(homework.getId(), student.getId());
        if (existingOpt.isPresent()) {
            Submission existing = existingOpt.get();
            Boolean allowResubmit = homework.getAllowResubmit();
            if (allowResubmit == null || !allowResubmit) {
                return Result.error("该作业不允许重复提交");
            }

            // 覆盖：删除旧答案并重置提交状态
            answerRepository.deleteBySubmissionId(existing.getId());
            existing.setStatus(1); // 批改中
            existing.setTotalScore(null);
            existing.setAiScore(null);
            existing.setTeacherScore(null);
            existing.setCorrectTime(null);
            existing.setSubmitTime(LocalDateTime.now());
            saved = submissionRepository.save(existing);
        } else {
            // 创建提交记录
            Submission submission = new Submission();
            submission.setHomework(homework);
            submission.setStudent(student);
            submission.setStatus(1); // 批改中
            submission.setSubmitTime(LocalDateTime.now());
            saved = submissionRepository.save(submission);
        }
        
        // 保存答案（不立即触发AI批改）
        List<Answer> savedAnswers = new ArrayList<>();
        for (SubmitHomeworkRequest.AnswerDTO answerDTO : request.getAnswers()) {
            Answer answer = new Answer();
            answer.setSubmission(saved);
            answer.setQuestion(questionRepository.findById(answerDTO.getQuestionId()).orElse(null));
            answer.setContent(answerDTO.getContent());
            answer.setStatus(0); // 待批改
            answerRepository.save(answer);
            savedAnswers.add(answer);
        }
        
        // 异步分批触发AI批改 - 预加载所有需要的数据避免懒加载问题
        Long studentId = student.getId();
        String studentName = student.getRealName();
        Long homeworkId = homework.getId();
        String homeworkTitle = homework.getTitle();
        Long teacherId = homework.getTeacher() != null ? homework.getTeacher().getId() : null;
        
        // 只有作业启用AI批改时才触发
        Boolean aiEnabled = homework.getAiEnabled();
        if (aiEnabled != null && aiEnabled) {
            aiCorrectionService.correctAnswersAsync(saved.getId(), savedAnswers, 
                studentId, studentName, homeworkId, homeworkTitle, teacherId);
        }
        
        // 发送WebSocket通知给老师
        if (webSocketHandler != null && teacherId != null) {
            webSocketHandler.sendSubmissionNotification(
                teacherId, homework.getId(), student.getId(), homework.getTitle(), studentName
            );
        }
        
        return Result.success(saved);
    }

    /**
     * 获取作业提交列表（教师端）
     */
    @GetMapping("/homework/{homeworkId}")
    public Result<List<TeacherSubmissionDTO>> getSubmissionsByHomework(@PathVariable Long homeworkId) {
        List<Submission> submissions = submissionRepository.findByHomeworkId(homeworkId);
        List<TeacherSubmissionDTO> dtoList = submissions.stream().map(s -> {
            TeacherSubmissionDTO dto = new TeacherSubmissionDTO();
            dto.setId(s.getId());
            dto.setHomeworkId(s.getHomework() != null ? s.getHomework().getId() : null);
            dto.setStudentId(s.getStudent() != null ? s.getStudent().getId() : null);
            dto.setStudentName(s.getStudent() != null ? s.getStudent().getRealName() : "未知学生");
            dto.setStatus(s.getStatus());
            dto.setTotalScore(s.getTotalScore());
            dto.setAiScore(s.getAiScore());
            dto.setTeacherScore(s.getTeacherScore());
            dto.setSubmitTime(s.getSubmitTime());
            dto.setCorrectTime(s.getCorrectTime());
            return dto;
        }).toList();
        return Result.success(dtoList);
    }

    /**
     * 获取指定学生的提交详情（教师端）
     */
    @GetMapping("/homework/{homeworkId}/student/{studentId}")
    public Result<SubmissionDetailDTO> getSubmissionByHomeworkAndStudent(
            @PathVariable Long homeworkId,
            @PathVariable Long studentId) {
        Optional<Submission> opt = submissionRepository.findByHomeworkIdAndStudentId(homeworkId, studentId);
        if (opt.isEmpty()) {
            return Result.error("未找到该学生的提交记录");
        }
        Submission s = opt.get();
        SubmissionDetailDTO dto = new SubmissionDetailDTO();
        dto.setId(s.getId());
        dto.setHomeworkId(s.getHomework() != null ? s.getHomework().getId() : null);
        dto.setStudentId(s.getStudent() != null ? s.getStudent().getId() : null);
        dto.setStudentName(s.getStudent() != null ? s.getStudent().getRealName() : "未知学生");
        dto.setStatus(s.getStatus());
        dto.setTotalScore(s.getTotalScore());
        dto.setAiScore(s.getAiScore());
        dto.setTeacherScore(s.getTeacherScore());
        dto.setSubmitTime(s.getSubmitTime());
        dto.setCorrectTime(s.getCorrectTime());

        // 设置作业信息（用于前端判断AI批改开关）
        if (s.getHomework() != null) {
            SubmissionDetailDTO.HomeworkInfoDTO homeworkDTO = new SubmissionDetailDTO.HomeworkInfoDTO();
            homeworkDTO.setId(s.getHomework().getId());
            homeworkDTO.setTitle(s.getHomework().getTitle());
            homeworkDTO.setAiEnabled(s.getHomework().getAiEnabled());
            homeworkDTO.setSubject(s.getHomework().getSubject());
            dto.setHomework(homeworkDTO);
        }

        // 加载答案详情
        List<Answer> answers = answerRepository.findBySubmissionId(s.getId());
        List<SubmissionDetailDTO.AnswerDetailDTO> answerDTOs = answers.stream().map(a -> {
            SubmissionDetailDTO.AnswerDetailDTO ad = new SubmissionDetailDTO.AnswerDetailDTO();
            ad.setId(a.getId());
            ad.setQuestionId(a.getQuestion() != null ? a.getQuestion().getId() : null);
            ad.setQuestionType(a.getQuestion() != null ? a.getQuestion().getType() : null);
            ad.setQuestionContent(a.getQuestion() != null ? a.getQuestion().getContent() : null);
            ad.setFullScore(a.getQuestion() != null ? a.getQuestion().getScore() : null);
            ad.setCorrectAnswer(a.getQuestion() != null ? a.getQuestion().getCorrectAnswer() : null);
            ad.setStudentAnswer(a.getContent());
            ad.setAiAnalysis(a.getAiAnalysis());
            ad.setAiScore(a.getAiScore());
            ad.setScore(a.getTeacherScore() != null ? a.getTeacherScore() : a.getAiScore());
            ad.setComment(a.getTeacherComment());
            ad.setStatus(a.getStatus());
            // 解析选项JSON或'|'分隔格式
            if (a.getQuestion() != null && a.getQuestion().getOptions() != null) {
                String optionsStr = a.getQuestion().getOptions().trim();
                log.info("解析题目选项, questionId={}, optionsStr={}", a.getQuestion().getId(), optionsStr);
                
                if (!optionsStr.isEmpty()) {
                    try {
                        // 先尝试JSON数组格式
                        if (optionsStr.startsWith("[") && optionsStr.endsWith("]")) {
                            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                            ad.setQuestionOptions(mapper.readValue(optionsStr, 
                                new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}));
                            log.info("JSON解析成功, 选项数量={}", ad.getQuestionOptions().size());
                        } else if (optionsStr.contains("|")) {
                            // '|'分隔格式
                            String[] parts = optionsStr.split("\\|");
                            java.util.List<String> opts = new java.util.ArrayList<>();
                            for (String part : parts) {
                                part = part.trim();
                                // 去掉A. B. C. D. 前缀
                                if (part.matches("^[A-Z]\\.\\s*.+")) {
                                    part = part.substring(part.indexOf(".") + 1).trim();
                                }
                                if (!part.isEmpty()) {
                                    opts.add(part);
                                }
                            }
                            ad.setQuestionOptions(opts);
                            log.info("'|'分隔解析成功, 选项数量={}", opts.size());
                        } else {
                            // 单选项
                            ad.setQuestionOptions(java.util.List.of(optionsStr));
                        }
                    } catch (Exception e) {
                        log.error("解析选项失败, questionId={}, optionsStr={}", a.getQuestion().getId(), optionsStr, e);
                        ad.setQuestionOptions(new java.util.ArrayList<>());
                    }
                }
            } else {
                log.warn("题目选项为空, questionId={}", a.getQuestion() != null ? a.getQuestion().getId() : "null");
            }
            return ad;
        }).toList();
        dto.setAnswers(answerDTOs);

        return Result.success(dto);
    }

    /**
     * 获取学生的提交列表
     */
    @GetMapping("/student/{studentId}")
    public Result<List<Submission>> getSubmissionsByStudent(@PathVariable Long studentId) {
        List<Submission> submissions = submissionRepository.findByStudentId(studentId);
        return Result.success(submissions);
    }

    /**
     * 获取提交详情（包含答案）
     */
    @GetMapping("/{id}")
    public Result<SubmissionDetailDTO> getSubmissionById(@PathVariable Long id) {
        Optional<Submission> opt = submissionRepository.findById(id);
        if (opt.isEmpty()) {
            return Result.success(null);
        }
        
        Submission s = opt.get();
        SubmissionDetailDTO dto = new SubmissionDetailDTO();
        dto.setId(s.getId());
        dto.setHomeworkId(s.getHomework() != null ? s.getHomework().getId() : null);
        dto.setStudentId(s.getStudent() != null ? s.getStudent().getId() : null);
        dto.setStudentName(s.getStudent() != null ? s.getStudent().getRealName() : "未知学生");
        dto.setStatus(s.getStatus());
        dto.setTotalScore(s.getTotalScore());
        dto.setAiScore(s.getAiScore());
        dto.setTeacherScore(s.getTeacherScore());
        dto.setSubmitTime(s.getSubmitTime());
        dto.setCorrectTime(s.getCorrectTime());

        // 设置作业信息（用于前端判断AI批改开关）
        if (s.getHomework() != null) {
            SubmissionDetailDTO.HomeworkInfoDTO homeworkDTO = new SubmissionDetailDTO.HomeworkInfoDTO();
            homeworkDTO.setId(s.getHomework().getId());
            homeworkDTO.setTitle(s.getHomework().getTitle());
            homeworkDTO.setAiEnabled(s.getHomework().getAiEnabled());
            homeworkDTO.setSubject(s.getHomework().getSubject());
            dto.setHomework(homeworkDTO);
        }

        // 加载答案详情
        List<Answer> answers = answerRepository.findBySubmissionId(s.getId());
        List<SubmissionDetailDTO.AnswerDetailDTO> answerDTOs = answers.stream().map(a -> {
            SubmissionDetailDTO.AnswerDetailDTO ad = new SubmissionDetailDTO.AnswerDetailDTO();
            ad.setId(a.getId());
            ad.setQuestionId(a.getQuestion() != null ? a.getQuestion().getId() : null);
            ad.setQuestionType(a.getQuestion() != null ? a.getQuestion().getType() : null);
            ad.setQuestionContent(a.getQuestion() != null ? a.getQuestion().getContent() : null);
            ad.setFullScore(a.getQuestion() != null ? a.getQuestion().getScore() : null);
            ad.setCorrectAnswer(a.getQuestion() != null ? a.getQuestion().getCorrectAnswer() : null);
            ad.setStudentAnswer(a.getContent());
            ad.setAiAnalysis(a.getAiAnalysis());
            ad.setAiScore(a.getAiScore());
            ad.setScore(a.getTeacherScore() != null ? a.getTeacherScore() : a.getAiScore());
            ad.setComment(a.getTeacherComment());
            ad.setStatus(a.getStatus());
            // 解析选项JSON
            if (a.getQuestion() != null && a.getQuestion().getOptions() != null) {
                try {
                    com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                    ad.setQuestionOptions(mapper.readValue(a.getQuestion().getOptions(), 
                        new com.fasterxml.jackson.core.type.TypeReference<List<String>>() {}));
                } catch (Exception e) {
                    ad.setQuestionOptions(new ArrayList<>());
                }
            }
            return ad;
        }).toList();
        dto.setAnswers(answerDTOs);

        return Result.success(dto);
    }

    /**
     * 教师批改
     */
    @PutMapping("/{submissionId}/correct")
    public Result<Void> correctSubmission(
            @PathVariable Long submissionId,
            @RequestBody List<TeacherCorrectionDTO> corrections) {
        
        Submission submission = submissionRepository.findById(submissionId).orElse(null);
        if (submission == null) {
            return Result.error("提交不存在");
        }
        
        int totalScore = 0;
        for (TeacherCorrectionDTO correction : corrections) {
            Answer answer = answerRepository.findById(correction.getAnswerId()).orElse(null);
            if (answer != null) {
                answer.setTeacherScore(correction.getScore());
                answer.setTeacherComment(correction.getComment());
                answer.setStatus(2); // 教师已复核
                answerRepository.save(answer);
                totalScore += correction.getScore();
            }
        }
        
        submission.setTeacherScore(totalScore);
        submission.setTotalScore(totalScore);
        submission.setStatus(2); // 已完成
        submission.setCorrectTime(LocalDateTime.now());
        submissionRepository.save(submission);
        
        // 收集错题到错题本（教师批改也需要收集）
        collectWrongAnswersForManualCorrection(submission);
        
        // 发送WebSocket通知给学生（教师批改完成）
        if (webSocketHandler != null && submission.getStudent() != null) {
            Long studentId = submission.getStudent().getId();
            Long hwId = submission.getHomework() != null ? submission.getHomework().getId() : null;
            String homeworkTitle = submission.getHomework() != null ? submission.getHomework().getTitle() : "作业";
            Long subId = submission.getId();
            
            webSocketHandler.sendCorrectionCompleteNotification(
                studentId, hwId, subId, homeworkTitle, "CORRECTION_COMPLETE"
            );
        }
        
        return Result.success();
    }
    
    /**
     * 收集错题到错题本（用于教师人工批改）
     */
    private void collectWrongAnswersForManualCorrection(Submission submission) {
        try {
            Long studentId = submission.getStudent() != null ? submission.getStudent().getId() : null;
            Long homeworkId = submission.getHomework() != null ? submission.getHomework().getId() : null;
            
            if (studentId == null) {
                log.warn("无法找到学生信息, submissionId={}", submission.getId());
                return;
            }
            
            User student = userRepository.findById(studentId).orElse(null);
            if (student == null) {
                log.warn("无法找到学生, studentId={}", studentId);
                return;
            }
            
            // 获取该提交的所有答案
            List<Answer> answers = answerRepository.findBySubmissionId(submission.getId());
            
            int addedCount = 0;
            for (Answer answer : answers) {
                // 判断是否为错题：教师给分为0或null
                Integer teacherScore = answer.getTeacherScore();
                Question question = answer.getQuestion();
                
                if (question == null) continue;
                
                // 如果教师给分为0或者是null，认为是错题
                boolean isWrong = (teacherScore == null || teacherScore == 0);
                
                // 或者得分低于满分的50%也认为是错题
                if (!isWrong && question.getScore() != null && question.getScore() > 0) {
                    double rate = (double) teacherScore / question.getScore();
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
                    errorBook.setAnalysis(answer.getTeacherComment()); // 使用教师评语作为解析
                    errorBook.setKnowledgePoint(question.getKnowledgePoint());
                    errorBook.setMasteryStatus(0); // 未掌握
                    
                    errorBookRepository.save(errorBook);
                    addedCount++;
                    log.info("错题已添加到错题本（教师批改）- studentId={}, questionId={}, homeworkId={}", 
                        studentId, question.getId(), homeworkId);
                }
            }
            
            log.info("教师批改错题收集完成 - studentId={}, 新增错题数={}", studentId, addedCount);
        } catch (Exception e) {
            log.error("收集教师批改错题失败 - submissionId={}", submission.getId(), e);
        }
    }
}
