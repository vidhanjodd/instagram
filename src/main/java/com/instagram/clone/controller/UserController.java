package com.instagram.clone.controller;

import com.instagram.clone.dto.UserRegisterRequest;
import com.instagram.clone.entity.User;
import com.instagram.clone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public User register(@RequestBody UserRegisterRequest request) {
        return userService.register(request);
    }
}