package com.balabala.homework.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Entity
@Table(name = "submissions")
public class Submission {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "homework_id", nullable = false)
    @JsonIgnoreProperties({"submissions", "hibernateLazyInitializer", "handler"})
    private Homework homework;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    @JsonIgnoreProperties({"submissions", "hibernateLazyInitializer", "handler"})
    private User student;
    
    @OneToMany(mappedBy = "submission", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    @JsonIgnoreProperties({"submission", "hibernateLazyInitializer", "handler"})
    private List<Answer> answers = new ArrayList<>();
    
    @Column(nullable = false)
    private Integer status = 0;  // 0-待完成, 1-批改中, 2-已完成
    
    private Integer totalScore;  // 总得分
    
    private Integer aiScore;  // AI评分
    
    private Integer teacherScore;  // 教师评分
    
    private LocalDateTime submitTime;  // 提交时间
    
    private LocalDateTime correctTime;  // 批改时间
    
    @CreationTimestamp
    private LocalDateTime createTime;
    
    @UpdateTimestamp
    private LocalDateTime updateTime;
}
