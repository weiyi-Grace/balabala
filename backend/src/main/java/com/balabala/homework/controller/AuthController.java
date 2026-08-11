package com.balabala.homework.controller;

import com.balabala.homework.dto.LoginRequest;
import com.balabala.homework.dto.LoginResponse;
import com.balabala.homework.dto.RegisterRequest;
import com.balabala.homework.dto.Result;
import com.balabala.homework.entity.ClassInfo;
import com.balabala.homework.entity.User;
import com.balabala.homework.repository.ClassRepository;
import com.balabala.homework.service.UserService;
import com.balabala.homework.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;
    private final ClassRepository classRepository;
    private final JwtUtil jwtUtil;
    private final PasswordEncoder passwordEncoder;

    @PostMapping("/login")
    public Result<LoginResponse> login(@RequestBody LoginRequest request) {
        User user = userService.getUserByUsername(request.getUsername());
        if (user == null) {
            return Result.error("用户名或密码错误");
        }
        
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            return Result.error("用户名或密码错误");
        }
        
        String token = jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        
        LoginResponse response = new LoginResponse();
        response.setToken(token);
        response.setUser(user);
        
        return Result.success(response);
    }

    @PostMapping("/register")
    public Result<User> register(@RequestBody RegisterRequest request) {
        if (userService.existsByUsername(request.getUsername())) {
            return Result.error("用户名已存在");
        }
        
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRealName(request.getRealName());
        user.setRole(request.getRole());
        user.setPhone(request.getPhone());
        
        // 学生注册时通过邀请码加入班级（可选）
        if (request.getRole() == 3 && request.getInviteCode() != null && !request.getInviteCode().isEmpty()) {
            ClassInfo classInfo = classRepository.findByInviteCode(request.getInviteCode()).orElse(null);
            if (classInfo != null) {
                user.setClassInfo(classInfo);
            }
        }
        
        User saved = userService.saveUser(user);
        return Result.success(saved);
    }
}
