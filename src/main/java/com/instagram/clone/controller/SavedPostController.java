package com.instagram.clone.controller;

import com.instagram.clone.entity.User;
import com.instagram.clone.repository.UserRepository;
import com.instagram.clone.service.SavedPostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class SavedPostController {

    private final SavedPostService savedPostService;
    private final UserRepository userRepository;

    @PostMapping("/{postId}/save")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> toggleSave(@PathVariable Long postId, Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean isSaved = savedPostService.toggleSave(user.getId(), postId);

        Map<String, Object> response = new HashMap<>();
        response.put("saved", isSaved);
        return ResponseEntity.ok(response);
    }
}