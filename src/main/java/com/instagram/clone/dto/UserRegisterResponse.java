package com.instagram.clone.dto;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class UserRegisterResponse {

    private Long id;
    private String username;
    private String email;
    private String bio;
    private boolean isPrivate;
    private LocalDateTime createdAt;
}