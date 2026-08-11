package com.balabala.homework.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "error_books")
public class ErrorBook {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homework_id")
    private Homework homework;
    
    @Column(length = 4000)
    private String wrongAnswer;  // 错误答案
    
    @Column(length = 4000)
    private String correctAnswer;  // 正确答案
    
    @Column(length = 2000)
    private String analysis;  // 错题分析
    
    private String knowledgePoint;  // 知识点
    
    private Integer masteryStatus = 0;  // 0-未掌握, 1-已掌握
    
    @Column(length = 1000)
    private String notes;  // 学生笔记
    
    private Integer reviewCount = 0;  // 复习次数
    
    private LocalDateTime lastReviewTime;  // 最后复习时间
    
    @CreationTimestamp
    private LocalDateTime createTime;
}
