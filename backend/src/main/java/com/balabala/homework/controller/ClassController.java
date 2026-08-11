package com.balabala.homework.controller;

import com.balabala.homework.dto.Result;
import com.balabala.homework.entity.ClassInfo;
import com.balabala.homework.entity.User;
import com.balabala.homework.repository.ClassRepository;
import com.balabala.homework.repository.UserRepository;
import com.balabala.homework.service.UserService;
import com.balabala.homework.websocket.CorrectionWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/class")
@RequiredArgsConstructor
public class ClassController {

    private final ClassRepository classRepository;
    private final UserRepository userRepository;
    private final UserService userService;
    private final CorrectionWebSocketHandler webSocketHandler;

    /**
     * 获取班级列表
     */
    @GetMapping("/list")
    public Result<List<ClassInfo>> getClassList(@RequestParam Long teacherId) {
        List<ClassInfo> classes = classRepository.findByTeacherId(teacherId);
        return Result.success(classes);
    }

    /**
     * 获取所有班级列表
     */
    @GetMapping("/all")
    public Result<List<ClassInfo>> getAllClasses() {
        List<ClassInfo> classes = classRepository.findAll();
        return Result.success(classes);
    }

    /**
     * 创建班级
     */
    @PostMapping
    public Result<ClassInfo> createClass(@RequestBody Map<String, Object> data) {
        ClassInfo classInfo = new ClassInfo();
        classInfo.setName((String) data.get("name"));
        classInfo.setGrade((String) data.get("grade"));
        classInfo.setDescription((String) data.get("description"));
        
        // 设置教师
        Long teacherId = data.get("teacherId") instanceof Number ? ((Number) data.get("teacherId")).longValue() : null;
        if (teacherId != null) {
            User teacher = userService.getUserById(teacherId);
            classInfo.setTeacher(teacher);
        }
        
        // 生成邀请码
        classInfo.setInviteCode(UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        ClassInfo saved = classRepository.save(classInfo);
        return Result.success(saved);
    }

    /**
     * 更新班级
     */
    @PutMapping("/{id}")
    public Result<ClassInfo> updateClass(@PathVariable Long id, @RequestBody Map<String, Object> data) {
        ClassInfo existing = classRepository.findById(id).orElse(null);
        if (existing == null) {
            return Result.error("班级不存在");
        }
        
        // 只更新传入的字段
        if (data.containsKey("name")) {
            existing.setName((String) data.get("name"));
        }
        if (data.containsKey("grade")) {
            existing.setGrade((String) data.get("grade"));
        }
        if (data.containsKey("description")) {
            existing.setDescription((String) data.get("description"));
        }
        
        ClassInfo updated = classRepository.save(existing);
        return Result.success(updated);
    }

    /**
     * 删除班级
     */
    @DeleteMapping("/{id}")
    public Result<Void> deleteClass(@PathVariable Long id) {
        classRepository.deleteById(id);
        return Result.success();
    }

    /**
     * 获取班级详情
     */
    @GetMapping("/{id}")
    public Result<ClassInfo> getClassById(@PathVariable Long id) {
        ClassInfo classInfo = classRepository.findById(id).orElse(null);
        return Result.success(classInfo);
    }

    /**
     * 获取班级学生列表
     */
    @GetMapping("/{id}/students")
    public Result<List<User>> getClassStudents(@PathVariable Long id) {
        List<User> students = userService.getStudentsByClassId(id);
        return Result.success(students);
    }

    /**
     * 通过邀请码加入班级
     */
    @PostMapping("/join")
    public Result<Void> joinClass(@RequestParam String inviteCode, @RequestParam Long studentId) {
        ClassInfo classInfo = classRepository.findByInviteCode(inviteCode).orElse(null);
        if (classInfo == null) {
            return Result.error("班级不存在");
        }
        
        User student = userService.getUserById(studentId);
        if (student == null) {
            return Result.error("学生不存在");
        }
        
        student.setClassInfo(classInfo);
        userService.saveUser(student);
        
        // 发送通知给老师
        if (webSocketHandler != null && classInfo.getTeacher() != null) {
            Long teacherId = classInfo.getTeacher().getId();
            String studentName = student.getRealName();
            String className = classInfo.getName();
            webSocketHandler.sendStudentJoinedClassNotification(teacherId, studentId, studentName, className);
        }
        
        return Result.success();
    }

    /**
     * 从班级移除学生
     */
    @DeleteMapping("/{classId}/students/{studentId}")
    public Result<Void> removeStudent(@PathVariable Long classId, @PathVariable Long studentId) {
        User student = userService.getUserById(studentId);
        if (student != null) {
            student.setClassInfo(null);
            userService.saveUser(student);
        }
        return Result.success();
    }
}
