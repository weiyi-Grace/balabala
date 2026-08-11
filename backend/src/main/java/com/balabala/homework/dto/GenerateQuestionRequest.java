package com.balabala.homework.dto;

import lombok.Data;

@Data
public class GenerateQuestionRequest {
    private String subject;
    private String knowledgePoint;
    private String difficulty;
    private String questionType;
    private String prompt;
}
