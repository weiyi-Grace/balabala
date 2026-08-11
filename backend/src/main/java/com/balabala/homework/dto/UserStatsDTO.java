package com.balabala.homework.dto;

import lombok.Data;

@Data
public class UserStatsDTO {
    private Integer homeworkCount;      // 作业完成数
    private Double avgScore;            // 平均分
    private Integer errorCount;         // 错题数
    private Integer masteredErrorCount; // 已掌握错题数
    private Integer totalHomework;      // 总作业数
    private Double completionRate;      // 完成率
}
