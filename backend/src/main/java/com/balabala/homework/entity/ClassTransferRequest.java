package com.balabala.homework.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.time.LocalDateTime;

@Data
@Entity
@Table(name = "class_transfer_requests")
@JsonIgnoreProperties({"hibernateLazyInitializer", "handler"})
public class ClassTransferRequest {
    
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_class_id")
    private ClassInfo fromClass;
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_class_id", nullable = false)
    private ClassInfo toClass;
    
    @Column(nullable = false)
    private Integer status = 0; // 0-待审批, 1-已通过, 2-已拒绝
    
    private String reason; // 申请理由
    
    private String remark; // 审批备注
    
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "approved_by")
    private User approvedBy;
    
    private LocalDateTime approvedAt;
    
    @CreationTimestamp
    private LocalDateTime createTime;
    
    @UpdateTimestamp
    private LocalDateTime updateTime;
}
