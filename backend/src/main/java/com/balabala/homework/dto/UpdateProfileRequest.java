package com.balabala.homework.dto;

import lombok.Data;

@Data
public class UpdateProfileRequest {
    private Long userId;
    private String realName;
    private String nickname;
    private String phone;
    private String email;
    private String bio;
    private String avatar;
}
