package com.balabala.homework.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "questions")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Question {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String type;  // single_choice, multiple_choice, fill_blank, true_false, short_answer, reading_comprehension
    
    @Column(nullable = false, length = 2000)
    private String content;  // 题目内容
    
    @Column(length = 2000)
    private String options;  // 选项，JSON格式存储
    
    @Column(length = 2000)
    private String correctAnswer;  // 正确答案
    
    @Column(nullable = false)
    private Integer score;  // 分值
    
    @Column(nullable = false)
    private String difficulty;  // easy, medium, hard
    
    private String subject;  // 学科
    
    private String knowledgePoint;  // 知识点
    
    @Column(length = 2000)
    private String analysis;  // 解析
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "creator_id")
    private User creator;  // 创建人
    
    private Integer usageCount = 0;  // 使用次数
    
    private Integer accuracy = 0;  // 正确率
    
    @Column(nullable = false)
    private Integer status = 1;  // 0-删除, 1-正常
    
    @CreationTimestamp
    private LocalDateTime createTime;
    
    @UpdateTimestamp
    private LocalDateTime updateTime;
}
