package com.instagram.clone.controller;

import com.instagram.clone.entity.User;
import com.instagram.clone.repository.UserRepository;
import com.instagram.clone.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class FollowController {

    private final FollowService followService;
    private final UserRepository userRepository;

    @PostMapping("/{targetUserId}/follow")
    public String toggleFollow(@PathVariable Long targetUserId,
                               Principal principal,
                               @RequestHeader(value = "Referer", required = false) String referer) {

        User loggedInUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        followService.toggleFollow(loggedInUser.getId(), targetUserId);

        return (referer != null) ? "redirect:" + referer : "redirect:/users/" + targetUserId + "/profile";
    }

    @PostMapping("/{followerId}/remove-follower")
    public String removeFollower(@PathVariable Long followerId,
                                 Principal principal,
                                 @RequestHeader(value = "Referer", required = false) String referer) {

        User loggedInUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        followService.removeFollower(loggedInUser.getId(), followerId);

        return (referer != null) ? "redirect:" + referer : "redirect:/users/" + loggedInUser.getId() + "/profile";
    }

    @PostMapping("/follows/{followId}/accept")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> acceptRequest(
            @PathVariable Long followId,
            Principal principal) {

        User loggedInUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            followService.acceptFollowRequest(followId, loggedInUser.getId());
            return ResponseEntity.ok(Map.of("success", true, "action", "accepted"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
    @PostMapping("/follows/{followId}/decline")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> declineRequest(
            @PathVariable Long followId,
            Principal principal) {

        User loggedInUser = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        try {
            followService.declineFollowRequest(followId, loggedInUser.getId());
            return ResponseEntity.ok(Map.of("success", true, "action", "declined"));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("success", false, "error", e.getMessage()));
        }
    }
}