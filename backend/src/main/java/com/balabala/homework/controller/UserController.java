package com.balabala.homework.controller;

import com.balabala.homework.dto.ChangePasswordRequest;
import com.balabala.homework.dto.Result;
import com.balabala.homework.dto.UpdateProfileRequest;
import com.balabala.homework.dto.UserStatsDTO;
import com.balabala.homework.entity.User;
import com.balabala.homework.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;

    @GetMapping("/{id}")
    public Result<User> getUserById(@PathVariable Long id) {
        User user = userService.getUserById(id);
        return Result.success(user);
    }

    @GetMapping("/list")
    public Result<List<User>> getUsersByRole(@RequestParam Integer role) {
        List<User> users = userService.getUsersByRole(role);
        return Result.success(users);
    }

    @GetMapping("/class/{classId}")
    public Result<List<User>> getStudentsByClassId(@PathVariable Long classId) {
        List<User> students = userService.getStudentsByClassId(classId);
        return Result.success(students);
    }

    @PutMapping("/{id}")
    public Result<User> updateUser(@PathVariable Long id, @RequestBody User user) {
        user.setId(id);
        User updated = userService.saveUser(user);
        return Result.success(updated);
    }

    /**
     * 更新个人资料
     */
    @PutMapping("/{id}/profile")
    public Result<User> updateProfile(@PathVariable Long id, @RequestBody UpdateProfileRequest request) {
        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        user.setRealName(request.getRealName());
        user.setNickname(request.getNickname());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setBio(request.getBio());
        if (request.getAvatar() != null) {
            user.setAvatar(request.getAvatar());
        }
        
        User updated = userService.saveUser(user);
        return Result.success(updated);
    }

    /**
     * 修改密码
     */
    @PostMapping("/{id}/change-password")
    public Result<Void> changePassword(@PathVariable Long id, @RequestBody ChangePasswordRequest request) {
        User user = userService.getUserById(id);
        if (user == null) {
            return Result.error("用户不存在");
        }
        
        // 验证旧密码
        if (!passwordEncoder.matches(request.getOldPassword(), user.getPassword())) {
            return Result.error("旧密码错误");
        }
        
        // 更新新密码
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userService.saveUser(user);
        
        return Result.success();
    }

    /**
     * 获取用户统计信息
     */
    @GetMapping("/{id}/stats")
    public Result<UserStatsDTO> getUserStats(@PathVariable Long id) {
        UserStatsDTO stats = userService.getUserStats(id);
        return Result.success(stats);
    }

    /**
     * 搜索未加入班级的学生（用于邀请学生）
     */
    @GetMapping("/search/students")
    public Result<List<User>> searchStudentsNotInClass(
            @RequestParam String keyword,
            @RequestParam Long excludeClassId) {
        List<User> students = userService.searchStudentsNotInClass(keyword, excludeClassId);
        return Result.success(students);
    }
}
