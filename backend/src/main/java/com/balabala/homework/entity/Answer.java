package com.balabala.homework.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "answers")
public class Answer {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "submission_id", nullable = false)
    private Submission submission;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;
    
    @Column(length = 4000)
    private String content;  // 学生答案
    
    @Column(length = 4000)
    private String aiAnalysis;  // AI分析结果
    
    private Integer aiScore;  // AI评分
    
    private Integer teacherScore;  // 教师评分
    
    @Column(length = 1000)
    private String teacherComment;  // 教师评语
    
    private Integer status = 0;  // 0-待批改, 1-AI已批改, 2-教师已复核
    
    @CreationTimestamp
    private LocalDateTime createTime;
}
