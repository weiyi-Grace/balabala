package com.balabala.homework.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class HomeworkListDTO {
    private Long id;
    private String title;
    private String description;
    private String subject;
    private String type;  // homework-普通作业, exam-考试
    private LocalDateTime startTime;
    private LocalDateTime deadline;
    private Integer duration;
    private Integer status;  // 0-草稿, 1-已发布, 2-已结束
    
    // 班级信息
    private Long classId;
    private String className;
    
    // 统计信息
    private Integer totalStudents;  // 班级总人数
    private Integer submittedCount; // 已提交人数
    private Double avgScore;        // 平均分
    private Integer pendingCorrection; // 待批改数量
    
    // AI设置
    private Boolean aiEnabled;
    private Integer aiStrictness;
    
    private LocalDateTime createTime;
}
