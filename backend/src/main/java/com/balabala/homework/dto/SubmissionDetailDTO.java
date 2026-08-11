package com.balabala.homework.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class SubmissionDetailDTO {
    private Long id;
    private Long homeworkId;
    private Long studentId;
    private String studentName;
    private Integer status;
    private Integer totalScore;
    private Integer aiScore;
    private Integer teacherScore;
    private LocalDateTime submitTime;
    private LocalDateTime correctTime;
    private List<AnswerDetailDTO> answers;
    
    // 作业信息（用于前端判断AI批改开关等）
    private HomeworkInfoDTO homework;
    
    @Data
    public static class HomeworkInfoDTO {
        private Long id;
        private String title;
        private Boolean aiEnabled;
        private String subject;
    }

    @Data
    public static class AnswerDetailDTO {
        private Long id;
        private Long questionId;
        private String questionType;
        private String questionContent;
        private List<String> questionOptions;  // 题目选项
        private Integer fullScore;
        private String studentAnswer;
        private String correctAnswer;
        private String aiAnalysis;
        private Integer aiScore;
        private Integer score;
        private String comment;
        private Integer status;
    }
}
