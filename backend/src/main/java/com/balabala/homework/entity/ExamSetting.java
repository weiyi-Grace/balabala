package com.balabala.homework.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * 考试防作弊设置
 */
@Data
@Entity
@Table(name = "exam_settings")
public class ExamSetting {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homework_id", nullable = false)
    private Homework homework;  // 关联的作业/考试
    
    private Boolean shuffleQuestions = true;  // 题目乱序
    
    private Boolean shuffleOptions = true;  // 选项乱序
    
    private Boolean fullScreenMode = false;  // 全屏模式
    
    private Boolean preventCopyPaste = true;  // 禁止复制粘贴
    
    private Boolean limitSwitchWindow = true;  // 限制切屏次数
    
    private Integer maxSwitchTimes = 3;  // 最大切屏次数
    

    @CreationTimestamp
    private LocalDateTime createTime;
    
    @UpdateTimestamp
    private LocalDateTime updateTime;
}
