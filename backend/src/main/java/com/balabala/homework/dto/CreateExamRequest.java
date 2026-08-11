package com.balabala.homework.dto;

import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

/**
 * 创建考试请求DTO
 */
@Data
public class CreateExamRequest {
    private String title;
    private String description;
    private Long teacherId;
    private Long classId;
    private String subject;
    private LocalDateTime startTime;  // 考试开始时间
    private LocalDateTime deadline;   // 考试结束时间
    private Integer duration;         // 考试时长（分钟）
    private List<Long> questionIds;   // 题目ID列表
    
    // 考试防作弊设置
    private Boolean shuffleQuestions = true;
    private Boolean shuffleOptions = true;
    private Boolean fullScreenMode = false;
    private Boolean preventCopyPaste = true;
    private Boolean limitSwitchWindow = true;
    private Integer maxSwitchTimes = 3;

}
