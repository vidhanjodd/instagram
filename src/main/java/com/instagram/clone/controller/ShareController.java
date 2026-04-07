package com.instagram.clone.controller;

import com.instagram.clone.dto.MessageRequest;
import com.instagram.clone.entity.Follow;
import com.instagram.clone.entity.User;
import com.instagram.clone.repository.FollowRepository;
import com.instagram.clone.repository.UserRepository;
import com.instagram.clone.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequiredArgsConstructor
public class ShareController {

    private final MessageService messageService;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;

    /**
     * GET /users/following/{userId}
     * Returns the list of users that currentUser follows.
     */
    @GetMapping("/users/following/{userId}")
    public ResponseEntity<List<Map<String, Object>>> getFollowing(@PathVariable Long userId) {
        List<Follow> follows = followRepository.findByFollowerId(userId);
        List<Map<String, Object>> result = follows.stream()
                .map(f -> {
                    User u = f.getFollowing();
                    return Map.<String, Object>of(
                            "id",            u.getId(),
                            "username",      u.getUsername(),
                            "profilePicUrl", u.getProfilePicUrl() != null ? u.getProfilePicUrl() : ""
                    );
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(result);
    }

    /**
     * POST /messages/send/http
     * Always allowed — privacy is enforced at VIEW time (PostController),
     * not at send time. Just like real Instagram.
     */
    @PostMapping("/messages/send/http")
    public ResponseEntity<?> sendMessageHttp(
            @RequestBody MessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User sender = userRepository.findByUsername(userDetails.getUsername())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            messageService.sendMessage(sender, request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "failed", "message", e.getMessage()));
        }
    }
}