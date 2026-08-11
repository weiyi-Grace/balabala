package com.balabala.homework.dto;

import lombok.Data;

@Data
public class TeacherCorrectionDTO {
    private Long answerId;
    private Integer score;
    private String comment;
}
