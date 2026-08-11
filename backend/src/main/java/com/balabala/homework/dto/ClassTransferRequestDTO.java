package com.balabala.homework.dto;

import lombok.Data;
import java.time.LocalDateTime;

@Data
public class ClassTransferRequestDTO {
    private Long id;
    private Long studentId;
    private String studentName;
    private Long fromClassId;
    private String fromClassName;
    private Long toClassId;
    private String toClassName;
    private Integer status; // 0-待审批, 1-已通过, 2-已拒绝
    private String reason;
    private String remark;
    private LocalDateTime createTime;
    private LocalDateTime approvedAt;
}
