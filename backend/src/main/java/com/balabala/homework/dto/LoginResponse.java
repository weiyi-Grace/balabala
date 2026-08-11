package com.balabala.homework.dto;

import com.balabala.homework.entity.User;
import lombok.Data;

@Data
public class LoginResponse {
    private String token;
    private User user;
}
