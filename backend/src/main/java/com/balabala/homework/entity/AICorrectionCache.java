package com.balabala.homework.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

/**
 * AI批改结果缓存表
 * 用于缓存相同题目和相同答案的批改结果，避免重复调用AI API
 */
@Data
@Entity
@Table(name = "ai_correction_cache")
public class AICorrectionCache {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private Long questionId;  // 题目ID
    
    @Column(nullable = false, length = 1000)
    private String answerHash;  // 学生答案的MD5哈希值
    
    @Column(nullable = false)
    private String questionType;  // 题目类型
    
    @Column(nullable = false)
    private Integer score;  // AI评分
    
    @Column(nullable = false, length = 2000)
    private String analysis;  // AI解析
    
    @Column(nullable = false)
    private Boolean isCorrect;  // 是否正确
    
    @Column
    private Integer useCount = 1;  // 缓存被使用次数
    
    @Column(length = 1000)
    private String studentAnswer;  // 学生原始答案（用于调试）
    
    @CreationTimestamp
    private LocalDateTime createTime;
    
    @UpdateTimestamp
    private LocalDateTime updateTime;
}
