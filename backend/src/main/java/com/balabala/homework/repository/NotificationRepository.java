package com.balabala.homework.repository;

import com.balabala.homework.entity.Notification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {
    
    /**
     * 查询学生的未读通知
     */
    List<Notification> findByReceiverIdAndIsReadFalseOrderByCreatedAtDesc(Long receiverId);
    
    /**
     * 查询学生的所有通知
     */
    List<Notification> findByReceiverIdOrderByCreatedAtDesc(Long receiverId);
    
    /**
     * 查询班级通知（接收者为空的表示发给整个班级）
     */
    List<Notification> findByClassIdAndReceiverIdIsNullOrderByCreatedAtDesc(Long classId);
    
    /**
     * 查询教师发送的通知
     */
    List<Notification> findBySenderIdOrderByCreatedAtDesc(Long senderId);
    
    /**
     * 统计未读通知数量
     */
    long countByReceiverIdAndIsReadFalse(Long receiverId);
}
