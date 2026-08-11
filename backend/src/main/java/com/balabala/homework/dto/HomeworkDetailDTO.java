package com.balabala.homework.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class HomeworkDetailDTO {
    private Long id;
    private String title;
    private String description;
    private String subject;
    private String type;
    private LocalDateTime startTime;
    private LocalDateTime deadline;
    private Integer duration;
    private Integer status;
    
    // 班级信息
    private Long classId;
    private String className;
    
    // AI设置
    private Boolean aiEnabled;
    private Integer aiStrictness;
    
    // 题目列表
    private List<QuestionDTO> questions;
    
    private LocalDateTime createTime;
    
    @Data
    @Builder
    public static class QuestionDTO {
        private Long id;
        private String type;
        private String content;
        private Integer score;
        private String difficulty;
        private List<String> options;
        private String correctAnswer;
        private String knowledgePoint;
        private String analysis;
        private Integer order;
    }
}
