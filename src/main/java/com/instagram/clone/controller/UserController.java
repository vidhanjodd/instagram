package com.instagram.clone.controller;

import com.instagram.clone.dto.UpdateBioRequest;
import com.instagram.clone.dto.UserRegisterRequest;
import com.instagram.clone.dto.UserRegisterResponse;
import com.instagram.clone.entity.User;
import com.instagram.clone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @PostMapping("/register")
    public UserRegisterResponse register(@RequestBody UserRegisterRequest request) {
        User user = userService.register(request);
        return UserRegisterResponse.builder()
                .id(user.getId())
                .username(user.getUsername())
                .email(user.getEmail())
                .bio(user.getBio())
                .isPrivate(user.isPrivate())
                .createdAt(user.getCreatedAt())
                .build();
    }

    @DeleteMapping("/delete/{id}")
    public String delete(@PathVariable Long id) {
        userService.deleteUser(id);
        return "user deleted";
    }

    @PutMapping("/{id}/bio")
    public UserRegisterResponse updateBio(@PathVariable Long id,
                          @RequestBody UpdateBioRequest request) {

        return userService.updateBio(id, request.getBio());
    }

    @GetMapping("/all")
    public List<UserRegisterResponse> getAllUsers() {
        return userService.getAllUsers();
    }
}