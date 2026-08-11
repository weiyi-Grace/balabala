package com.balabala.homework.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 系统动态/活动记录实体
 */
@Entity
@Table(name = "activity")
@Data
public class Activity {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 活动类型：HOMEWORK_PUBLISHED(发布作业), HOMEWORK_SUBMITTED(提交作业), 
     * CORRECTION_COMPLETE(批改完成), STUDENT_JOINED(学生加入)等
     */
    @Column(nullable = false)
    private String type;
    
    /**
     * 活动标题
     */
    @Column(nullable = false)
    private String title;
    
    /**
     * 活动详情内容
     */
    @Column(length = 500)
    private String content;
    
    /**
     * 关联的用户ID（操作者）
     */
    @Column(name = "user_id")
    private Long userId;
    
    /**
     * 用户角色：TEACHER, STUDENT
     */
    @Column(name = "user_role")
    private String userRole;
    
    /**
     * 关联的作业ID
     */
    @Column(name = "homework_id")
    private Long homeworkId;
    
    /**
     * 关联的班级ID
     */
    @Column(name = "class_id")
    private Long classId;
    
    /**
     * 关联的学生ID
     */
    @Column(name = "student_id")
    private Long studentId;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}
