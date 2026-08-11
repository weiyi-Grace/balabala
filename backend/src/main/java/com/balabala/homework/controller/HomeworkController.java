package com.balabala.homework.controller;

import com.balabala.homework.dto.CreateExamRequest;
import com.balabala.homework.dto.HomeworkDetailDTO;
import com.balabala.homework.dto.HomeworkListDTO;
import com.balabala.homework.dto.Result;
import com.balabala.homework.entity.*;
import com.balabala.homework.repository.ClassRepository;
import com.balabala.homework.repository.HomeworkQuestionRepository;
import com.balabala.homework.repository.HomeworkRepository;
import com.balabala.homework.repository.QuestionRepository;
import com.balabala.homework.repository.UserRepository;
import com.balabala.homework.service.ExamService;
import com.balabala.homework.websocket.CorrectionWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/homework")
@RequiredArgsConstructor
public class HomeworkController {

    private final HomeworkRepository homeworkRepository;
    private final ExamService examService;
    private final ClassRepository classRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository;
    private final HomeworkQuestionRepository homeworkQuestionRepository;
    private final CorrectionWebSocketHandler webSocketHandler;

    /**
     * 发布作业
     */
    @PostMapping
    public Result<Homework> createHomework(@RequestBody Map<String, Object> data) {
        Homework homework = new Homework();
        
        // 设置基本字段
        homework.setTitle((String) data.get("title"));
        homework.setDescription((String) data.get("description"));
        homework.setSubject((String) data.get("subject"));
        homework.setDeadline(parseDateTime(data.get("deadline")));
        homework.setStartTime(parseDateTime(data.get("startTime")));
        homework.setDuration((Integer) data.get("duration"));
        homework.setType((String) data.getOrDefault("type", "homework"));
        homework.setStatus(1);
        
        // AI设置
        homework.setAiEnabled((Boolean) data.getOrDefault("aiEnabled", true));
        homework.setAiStrictness((Integer) data.getOrDefault("aiStrictness", 70));
        homework.setAiDimensions((String) data.get("aiDimensions"));
        homework.setAllowResubmit((Boolean) data.getOrDefault("allowResubmit", false));
        homework.setShowAnswer((Boolean) data.getOrDefault("showAnswer", false));
        homework.setShowAnalysis((Boolean) data.getOrDefault("showAnalysis", true));
        
        // 设置班级
        Long classId = data.get("classId") instanceof Number ? ((Number) data.get("classId")).longValue() : null;
        if (classId != null) {
            ClassInfo classInfo = classRepository.findById(classId).orElse(null);
            homework.setClassInfo(classInfo);
        }
        
        // 设置教师
        Long teacherId = data.get("teacherId") instanceof Number ? ((Number) data.get("teacherId")).longValue() : null;
        if (teacherId != null) {
            User teacher = userRepository.findById(teacherId).orElse(null);
            homework.setTeacher(teacher);
        }
        
        Homework saved = homeworkRepository.save(homework);

        // 发送作业发布通知给班级学生
        if (classId != null && webSocketHandler != null) {
            List<User> students = userRepository.findStudentsByClassId(classId);
            for (User student : students) {
                if (student.getId() != null) {
                    webSocketHandler.sendHomeworkPublishedNotification(
                        student.getId(), saved.getId(), saved.getTitle(), saved.getSubject()
                    );
                }
            }
        }

        return Result.success(saved);
    }
    
    private LocalDateTime parseDateTime(Object value) {
        if (value == null) return null;
        if (value instanceof String) {
            String str = (String) value;
            // 支持ISO 8601格式（带Z的UTC时间）如 2025-03-01T16:00:00.000Z
            if (str.endsWith("Z")) {
                return LocalDateTime.parse(str.substring(0, str.length() - 1));
            }
            return LocalDateTime.parse(str);
        }
        return null;
    }

    /**
     * 创建考试
     */
    @PostMapping("/exam")
    public Result<Homework> createExam(@RequestBody CreateExamRequest request) {
        Homework exam = examService.createExam(request);
        return Result.success(exam);
    }

    /**
     * 获取作业列表（教师）- 返回DTO包含班级名称和统计
     */
    @GetMapping("/teacher/{teacherId}")
    public Result<List<HomeworkListDTO>> getTeacherHomeworks(@PathVariable Long teacherId) {
        List<Homework> homeworks = homeworkRepository.findByTeacherId(teacherId);
        List<HomeworkListDTO> result = homeworks.stream().map(h -> {
            ClassInfo classInfo = h.getClassInfo();
            String className = classInfo != null ? classInfo.getName() : "未知班级";
            Long classId = classInfo != null ? classInfo.getId() : null;
            
            // 统计已提交人数
            int submittedCount = h.getSubmissions() != null ? h.getSubmissions().size() : 0;
            int totalStudents = classInfo != null && classInfo.getStudents() != null 
                ? classInfo.getStudents().size() : 0;
            
            // 计算平均分
            Double avgScore = null;
            if (h.getSubmissions() != null && !h.getSubmissions().isEmpty()) {
                avgScore = h.getSubmissions().stream()
                    .filter(s -> s.getTotalScore() != null)
                    .mapToDouble(Submission::getTotalScore)
                    .average()
                    .orElse(0.0);
            }
            
            // 待批改数量（已提交但未批改的，status=1表示已提交）
            int pendingCorrection = h.getSubmissions() != null 
                ? (int) h.getSubmissions().stream()
                    .filter(s -> s.getStatus() != null && s.getStatus() == 1)
                    .count()
                : 0;
            
            return HomeworkListDTO.builder()
                .id(h.getId())
                .title(h.getTitle())
                .description(h.getDescription())
                .subject(h.getSubject())
                .type(h.getType())
                .startTime(h.getStartTime())
                .deadline(h.getDeadline())
                .duration(h.getDuration())
                .status(h.getStatus())
                .classId(classId)
                .className(className)
                .totalStudents(totalStudents)
                .submittedCount(submittedCount)
                .avgScore(avgScore)
                .pendingCorrection(pendingCorrection)
                .aiEnabled(h.getAiEnabled())
                .aiStrictness(h.getAiStrictness())
                .createTime(h.getCreateTime())
                .build();
        }).toList();
        return Result.success(result);
    }

    /**
     * 获取作业列表（学生-本班）
     */
    @GetMapping("/class/{classId}")
    public Result<List<Homework>> getClassHomeworks(@PathVariable Long classId) {
        List<Homework> homeworks = homeworkRepository.findActiveHomeworkByClassId(classId);
        return Result.success(homeworks);
    }

    /**
     * 获取作业详情（包含题目列表）
     */
    @GetMapping("/{id}")
    public Result<HomeworkDetailDTO> getHomeworkById(@PathVariable Long id) {
        Homework homework = homeworkRepository.findById(id).orElse(null);
        if (homework == null) {
            return Result.error("作业不存在");
        }
        
        ClassInfo classInfo = homework.getClassInfo();
        String className = classInfo != null ? classInfo.getName() : "未知班级";
        Long classId = classInfo != null ? classInfo.getId() : null;
        
        // 构建题目列表
        List<HomeworkDetailDTO.QuestionDTO> questions = homework.getHomeworkQuestions().stream()
            .sorted((a, b) -> Integer.compare(
                a.getQuestionOrder() != null ? a.getQuestionOrder() : 0,
                b.getQuestionOrder() != null ? b.getQuestionOrder() : 0))
            .map(hq -> {
                Question q = hq.getQuestion();
                // 解析选项
                List<String> options = null;
                if (q.getOptions() != null && !q.getOptions().isEmpty()) {
                    options = List.of(q.getOptions().split("\\|"));
                }
                
                return HomeworkDetailDTO.QuestionDTO.builder()
                    .id(q.getId())
                    .type(q.getType())
                    .content(q.getContent())
                    .score(q.getScore())
                    .difficulty(q.getDifficulty())
                    .options(options)
                    .correctAnswer(q.getCorrectAnswer())
                    .knowledgePoint(q.getKnowledgePoint())
                    .analysis(q.getAnalysis())
                    .order(hq.getQuestionOrder())
                    .build();
            })
            .toList();
        
        HomeworkDetailDTO dto = HomeworkDetailDTO.builder()
            .id(homework.getId())
            .title(homework.getTitle())
            .description(homework.getDescription())
            .subject(homework.getSubject())
            .type(homework.getType())
            .startTime(homework.getStartTime())
            .deadline(homework.getDeadline())
            .duration(homework.getDuration())
            .status(homework.getStatus())
            .classId(classId)
            .className(className)
            .aiEnabled(homework.getAiEnabled())
            .aiStrictness(homework.getAiStrictness())
            .questions(questions)
            .createTime(homework.getCreateTime())
            .build();
        
        return Result.success(dto);
    }

    /**
     * 获取考试设置
     */
    @GetMapping("/{id}/exam-setting")
    public Result<ExamSetting> getExamSetting(@PathVariable Long id) {
        ExamSetting setting = examService.getExamSetting(id);
        return Result.success(setting);
    }

    /**
     * 检查考试状态
     */
    @GetMapping("/{id}/exam-status")
    public Result<Boolean> checkExamStatus(@PathVariable Long id) {
        boolean active = examService.isExamActive(id);
        return Result.success(active);
    }

    /**
     * 更新作业
     */
    @PutMapping("/{id}")
    public Result<Homework> updateHomework(@PathVariable Long id, @RequestBody Homework homework) {
        homework.setId(id);
        Homework updated = homeworkRepository.save(homework);
        return Result.success(updated);
    }

    /**
     * 删除作业
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteHomework(@PathVariable Long id) {
        homeworkRepository.deleteById(id);
        return Result.success();
    }

    /**
     * 结束作业
     */
    @PostMapping("/{id}/end")
    public Result<Void> endHomework(@PathVariable Long id) {
        Homework homework = homeworkRepository.findById(id).orElse(null);
        if (homework != null) {
            homework.setStatus(2);
            homeworkRepository.save(homework);
        }
        return Result.success();
    }

    /**
     * 添加作业题目（批量）
     */
    @PostMapping("/{id}/questions")
    public Result<Void> addHomeworkQuestions(@PathVariable Long id, @RequestBody List<Map<String, Object>> questions) {
        Homework homework = homeworkRepository.findById(id).orElse(null);
        if (homework == null) {
            return Result.error("作业不存在");
        }
        
        int order = 1;
        for (Map<String, Object> qData : questions) {
            // 创建题目
            Question question = new Question();
            question.setType((String) qData.get("type"));
            question.setContent((String) qData.get("content"));
            question.setScore((Integer) qData.getOrDefault("score", 5));
            question.setDifficulty((String) qData.getOrDefault("difficulty", "medium"));
            question.setSubject(homework.getSubject());
            
            // 处理选项（支持数组或字符串）
            Object optionsObj = qData.get("options");
            if (optionsObj != null) {
                if (optionsObj instanceof List) {
                    @SuppressWarnings("unchecked")
                    List<String> options = (List<String>) optionsObj;
                    question.setOptions(String.join("|", options));
                } else if (optionsObj instanceof String) {
                    // 如果已经是字符串（如JSON格式），直接存储
                    question.setOptions((String) optionsObj);
                }
            }
            
            // 处理正确答案（支持多种字段）
            Object correctAnswer = qData.get("correctAnswer");
            Object correctAnswers = qData.get("correctAnswers");
            Object correctAnswerText = qData.get("correctAnswerText");
            
            String finalAnswer = null;
            
            // 优先使用 correctAnswerText（填空题、简答题）
            if (correctAnswerText != null && !correctAnswerText.toString().isEmpty()) {
                finalAnswer = correctAnswerText.toString();
            }
            // 多选题使用 correctAnswers（数组）
            else if (correctAnswers instanceof List && !((List<?>) correctAnswers).isEmpty()) {
                finalAnswer = String.join(",", (List<String>) correctAnswers);
            }
            // 其他题型使用 correctAnswer
            else if (correctAnswer != null && !correctAnswer.toString().isEmpty()) {
                finalAnswer = correctAnswer.toString();
            }
            
            if (finalAnswer != null) {
                question.setCorrectAnswer(finalAnswer);
            }
            
            // 处理知识点
            @SuppressWarnings("unchecked")
            List<String> knowledgePoints = (List<String>) qData.get("knowledgePoints");
            if (knowledgePoints != null && !knowledgePoints.isEmpty()) {
                question.setKnowledgePoint(String.join(",", knowledgePoints));
            }
            
            // 处理解析
            question.setAnalysis((String) qData.get("aiAnalysis"));
            
            question.setStatus(1);
            Question savedQuestion = questionRepository.save(question);
            
            // 创建作业-题目关联
            HomeworkQuestion hq = new HomeworkQuestion();
            hq.setHomework(homework);
            hq.setQuestion(savedQuestion);
            hq.setQuestionOrder(order++);
            homeworkQuestionRepository.save(hq);
        }
        
        return Result.success();
    }
}
