package com.instagram.clone.service;

import com.instagram.clone.dto.UserRegisterRequest;
import com.instagram.clone.entity.User;
import com.instagram.clone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(UserRegisterRequest request) {

        userRepository.findByEmail(request.getEmail())
                .ifPresent(user -> {
                    throw new RuntimeException("Email already exists");
                });

        userRepository.findByUsername(request.getUsername())
                .ifPresent(user -> {
                    throw new RuntimeException("Username already exists");
                });

        User user = User.builder()
                .username(request.getUsername())
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .bio("")
                .isPrivate(false)
                .build();

        return userRepository.save(user);
    }
}