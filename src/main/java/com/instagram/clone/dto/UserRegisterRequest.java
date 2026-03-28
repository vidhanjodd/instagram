package com.instagram.clone.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserRegisterRequest {

    private long id;
    private String username;
    private String email;
    private String password;
    private String bio;
    private LocalDateTime createdAt;
    private boolean isPrivate;


}