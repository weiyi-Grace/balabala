package com.balabala.homework.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

/**
 * 通知消息实体
 */
@Entity
@Table(name = "notification")
@Data
public class Notification {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    /**
     * 通知标题
     */
    @Column(nullable = false)
    private String title;
    
    /**
     * 通知内容
     */
    @Column(nullable = false, length = 500)
    private String content;
    
    /**
     * 通知类型：HOMEWORK(作业提醒), EXAM(考试提醒), NOTICE(一般通知), SYSTEM(系统通知)
     */
    @Column(nullable = false)
    private String type;
    
    /**
     * 发送者ID（教师ID）
     */
    @Column(name = "sender_id")
    private Long senderId;
    
    /**
     * 接收者ID（学生ID），为空表示发送给班级所有学生
     */
    @Column(name = "receiver_id")
    private Long receiverId;
    
    /**
     * 班级ID，为空表示发送给指定学生
     */
    @Column(name = "class_id")
    private Long classId;
    
    /**
     * 关联的作业ID（可选）
     */
    @Column(name = "homework_id")
    private Long homeworkId;
    
    /**
     * 是否已读
     */
    @Column(name = "is_read")
    private Boolean isRead = false;
    
    /**
     * 创建时间
     */
    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;
    
    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        if (isRead == null) {
            isRead = false;
        }
    }
}
