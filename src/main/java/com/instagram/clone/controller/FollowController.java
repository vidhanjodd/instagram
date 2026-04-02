package com.instagram.clone.controller;

import com.instagram.clone.entity.User;
import com.instagram.clone.repository.UserRepository;
import com.instagram.clone.service.FollowService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

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
}