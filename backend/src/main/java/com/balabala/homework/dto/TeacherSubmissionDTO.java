package com.balabala.homework.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class TeacherSubmissionDTO {
    private Long id;
    private Long homeworkId;
    private Long studentId;
    private String studentName;
    private Integer status; // 0-待完成, 1-批改中, 2-已完成
    private Integer totalScore;
    private Integer aiScore;
    private Integer teacherScore;
    private LocalDateTime submitTime;
    private LocalDateTime correctTime;
}
