package com.balabala.homework.dto;

import lombok.Data;
import java.util.List;

@Data
public class AICorrectRequest {
    private String question;
    private String studentAnswer;
    private String correctAnswer;
    private String questionType;
    private Integer fullScore;
    private String knowledgePoint;
    private List<String> options;  // 题目选项
}
