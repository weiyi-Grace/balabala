package com.balabala.homework.dto;

import lombok.Data;

@Data
public class RegisterRequest {
    private String username;
    private String password;
    private String realName;
    private Integer role;
    private String phone;
    private String inviteCode;
}
