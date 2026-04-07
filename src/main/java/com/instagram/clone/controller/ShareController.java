package com.instagram.clone.controller;

import com.instagram.clone.dto.MessageRequest;
import com.instagram.clone.dto.UserRegisterResponse;
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
     * Returns the list of users that currentUser is following.
     * Used to populate the share modal with people you can send to.
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
     * HTTP version of sending a message (used by share feature).
     * The WebSocket /app/chat.send is used for real-time chat,
     * but share sends via HTTP so no WebSocket connection is needed on the feed page.
     */
    @PostMapping("/messages/send/http")
    public ResponseEntity<Void> sendMessageHttp(
            @RequestBody MessageRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            User sender = userRepository.findByUsername(userDetails.getUsername());
            messageService.sendMessage(sender, request);
            return ResponseEntity.ok().build();
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }
}