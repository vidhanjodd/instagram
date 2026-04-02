package com.instagram.clone.controller;

import com.instagram.clone.service.PostLikeService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostLikeController {

    private final PostLikeService postLikeService;

    @PostMapping("/{postId}/like")
    @ResponseBody
    public ResponseEntity<Map<String,Object>> togglePostLike(
            @PathVariable Long postId,
            Authentication authentication) {

        boolean liked = postLikeService.toggleLike(authentication.getName(), postId);
        long likeCount = postLikeService.getLikeCount(postId);

        Map<String ,Object> response = new HashMap<>();
        response.put("liked",liked);
        response.put("likeCount",likeCount);

        return ResponseEntity.ok(response);
    }
}
