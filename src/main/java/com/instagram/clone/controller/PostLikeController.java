package com.instagram.clone.controller;

import com.instagram.clone.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestHeader;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/{postId}/like")
    public String togglePostLike(
            @PathVariable Long postId,
            Authentication authentication,
            @RequestHeader(value = "Referer", required = false) String referer) {

        postLikeService.toggleLike(authentication.getName(), postId);

        if (referer != null) {
            return "redirect:" + referer;
        }
        return "redirect:/posts";
    }
}
