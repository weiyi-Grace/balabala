package com.balabala.homework.dto;

import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Data
public class ErrorBookDTO {
    private Long id;
    private Long questionId;
    private String questionContent;
    private String questionType;  // single_choice, multiple_choice, fill_blank, true_false, short_answer
    private List<String> options; // 选项列表
    private String wrongAnswer;
    private String correctAnswer;
    private String analysis;
    private String knowledgePoint;
    private Integer masteryStatus;
    private String notes;
    private Integer reviewCount;
    private LocalDateTime lastReviewTime;
    private LocalDateTime createTime;
    private String subject;
    private Integer score;
    private String difficulty;
}
