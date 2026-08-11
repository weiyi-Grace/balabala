package com.balabala.homework.controller;

import com.balabala.homework.dto.ClassTransferRequestDTO;
import com.balabala.homework.dto.Result;
import com.balabala.homework.entity.ClassInfo;
import com.balabala.homework.entity.ClassTransferRequest;
import com.balabala.homework.entity.User;
import com.balabala.homework.repository.ClassRepository;
import com.balabala.homework.repository.ClassTransferRequestRepository;
import com.balabala.homework.repository.UserRepository;
import com.balabala.homework.websocket.CorrectionWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/class-transfer")
@RequiredArgsConstructor
public class ClassTransferController {

    private final ClassTransferRequestRepository requestRepository;
    private final ClassRepository classRepository;
    private final UserRepository userRepository;
    private final CorrectionWebSocketHandler webSocketHandler;

    /**
     * 学生申请转班
     */
    @PostMapping("/apply")
    public Result<Void> applyForTransfer(@RequestBody ClassTransferRequestDTO dto) {
        // 检查学生是否存在
        User student = userRepository.findById(dto.getStudentId()).orElse(null);
        if (student == null) {
            return Result.error("学生不存在");
        }

        // 检查目标班级是否存在
        ClassInfo toClass = classRepository.findById(dto.getToClassId()).orElse(null);
        if (toClass == null) {
            return Result.error("目标班级不存在");
        }

        // 检查是否已有待审批的申请
        boolean exists = requestRepository.existsByStudentIdAndToClassIdAndStatus(
            dto.getStudentId(), dto.getToClassId(), 0);
        if (exists) {
            return Result.error("您已申请过该班级，请等待审批");
        }

        // 创建申请
        ClassTransferRequest request = new ClassTransferRequest();
        request.setStudent(student);
        request.setFromClass(student.getClassInfo());
        request.setToClass(toClass);
        request.setReason(dto.getReason());
        request.setStatus(0); // 待审批

        requestRepository.save(request);
        
        // WebSocket通知目标班级的老师
        User teacher = toClass.getTeacher();
        if (teacher != null) {
            webSocketHandler.sendClassTransferRequestNotification(
                teacher.getId(),
                student.getId(),
                student.getRealName() != null ? student.getRealName() : student.getUsername(),
                toClass.getName(),
                dto.getReason()
            );
        }
        
        return Result.success();
    }

    /**
     * 获取学生的转班申请历史
     */
    @GetMapping("/student/{studentId}")
    public Result<List<ClassTransferRequestDTO>> getStudentRequests(@PathVariable Long studentId) {
        List<ClassTransferRequest> requests = requestRepository.findByStudentIdOrderByCreateTimeDesc(studentId);
        List<ClassTransferRequestDTO> dtos = requests.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return Result.success(dtos);
    }

    /**
     * 获取老师待审批的转班申请
     */
    @GetMapping("/teacher/{teacherId}/pending")
    public Result<List<ClassTransferRequestDTO>> getPendingRequests(@PathVariable Long teacherId) {
        List<ClassTransferRequest> requests = requestRepository.findByToClassTeacherIdAndStatusOrderByCreateTimeDesc(teacherId, 0);
        List<ClassTransferRequestDTO> dtos = requests.stream()
            .map(this::convertToDTO)
            .collect(Collectors.toList());
        return Result.success(dtos);
    }

    /**
     * 审批转班申请
     */
    @PostMapping("/{requestId}/approve")
    public Result<Void> approveRequest(
            @PathVariable Long requestId,
            @RequestParam Long teacherId,
            @RequestParam Boolean approved,
            @RequestParam(required = false) String remark) {
        
        ClassTransferRequest request = requestRepository.findById(requestId).orElse(null);
        if (request == null) {
            return Result.error("申请不存在");
        }

        if (request.getStatus() != 0) {
            return Result.error("该申请已处理");
        }

        User teacher = userRepository.findById(teacherId).orElse(null);
        if (teacher == null) {
            return Result.error("审批人不存在");
        }

        request.setStatus(approved ? 1 : 2); // 1-通过, 2-拒绝
        request.setApprovedBy(teacher);
        request.setApprovedAt(LocalDateTime.now());
        request.setRemark(remark);

        // 如果通过，更新学生班级
        if (approved) {
            User student = request.getStudent();
            student.setClassInfo(request.getToClass());
            userRepository.save(student);
        }

        requestRepository.save(request);
        return Result.success();
    }

    private ClassTransferRequestDTO convertToDTO(ClassTransferRequest request) {
        ClassTransferRequestDTO dto = new ClassTransferRequestDTO();
        dto.setId(request.getId());
        dto.setStudentId(request.getStudent().getId());
        dto.setStudentName(request.getStudent().getRealName());
        dto.setFromClassId(request.getFromClass() != null ? request.getFromClass().getId() : null);
        dto.setFromClassName(request.getFromClass() != null ? request.getFromClass().getName() : null);
        dto.setToClassId(request.getToClass().getId());
        dto.setToClassName(request.getToClass().getName());
        dto.setStatus(request.getStatus());
        dto.setReason(request.getReason());
        dto.setRemark(request.getRemark());
        dto.setCreateTime(request.getCreateTime());
        return dto;
    }
}
