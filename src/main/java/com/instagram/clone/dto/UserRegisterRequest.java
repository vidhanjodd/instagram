package com.instagram.clone.dto;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class UserRegisterRequest {

    private String username;
    private String email;
    private String password;



}