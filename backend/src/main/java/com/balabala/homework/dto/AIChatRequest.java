package com.balabala.homework.dto;

import lombok.Data;

@Data
public class AIChatRequest {
    private String question;
    private String studentAnswer;
    private String correctAnswer;
    private String knowledgePoint;
    private String chatMessage;
    private String context;
}
