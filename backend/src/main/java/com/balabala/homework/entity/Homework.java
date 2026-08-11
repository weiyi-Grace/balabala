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
@Table(name = "homeworks")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class Homework {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @Column(nullable = false)
    private String title;  // 作业标题
    
    @Column(length = 2000)
    private String description;  // 作业描述
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "teacher_id")
    private User teacher;  // 发布教师
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "class_id")
    private ClassInfo classInfo;  // 发布班级
    
    @Column(nullable = false)
    private String subject;  // 学科
    
    @Column(nullable = false)
    private String type;  // homework-普通作业, exam-考试
    
    private LocalDateTime startTime;  // 开始时间（考试用）
    
    @Column(nullable = false)
    private LocalDateTime deadline;  // 截止时间
    
    private Integer duration;  // 考试时长（分钟）
    
    @OneToMany(mappedBy = "homework", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<HomeworkQuestion> homeworkQuestions = new ArrayList<>();
    
    @OneToMany(mappedBy = "homework", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Submission> submissions = new ArrayList<>();
    
    private Boolean aiEnabled = true;  // 是否启用AI批改
    
    private Integer aiStrictness = 70;  // AI批改严格程度
    
    private String aiDimensions;  // AI评分维度，JSON格式
    
    private Boolean allowResubmit = false;  // 允许重复提交
    
    private Boolean showAnswer = false;  // 提交后显示答案
    
    private Boolean showAnalysis = true;  // 显示AI解析
    
    @Column(nullable = false)
    private Integer status = 1;  // 0-草稿, 1-已发布, 2-已结束
    
    @CreationTimestamp
    private LocalDateTime createTime;
    
    @UpdateTimestamp
    private LocalDateTime updateTime;
}
