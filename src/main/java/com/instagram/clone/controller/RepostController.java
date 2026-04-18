package com.instagram.clone.controller;

import com.instagram.clone.entity.Post;
import com.instagram.clone.entity.Repost;
import com.instagram.clone.entity.User;
import com.instagram.clone.repository.UserRepository;
import com.instagram.clone.service.RepostService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/reposts")
@RequiredArgsConstructor
public class RepostController {

    private final RepostService repostService;
    private final UserRepository userRepository;


    @PostMapping("/{postId}")
    public ResponseEntity<Map<String, Object>> toggleRepost(
            @PathVariable Long postId,
            @RequestBody(required = false) Map<String, String> body,
            Principal principal) {

        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String caption = (body != null) ? body.get("caption") : null;

        try {
            boolean isNowReposted = repostService.toggleRepost(user.getId(), postId, caption);
            long count = repostService.getRepostCount(postId);

            return ResponseEntity.ok(Map.of(
                    "reposted", isNowReposted,
                    "repostCount", count
            ));
        } catch (RuntimeException e) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", e.getMessage()));
        }
    }


    @GetMapping("/{postId}/count")
    public ResponseEntity<Map<String, Long>> getRepostCount(@PathVariable Long postId) {
        long count = repostService.getRepostCount(postId);
        return ResponseEntity.ok(Map.of("repostCount", count));
    }


    @GetMapping("/my")
    public ResponseEntity<List<Map<String, Object>>> getMyReposts(Principal principal) {
        User user = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<Repost> reposts = repostService.getRepostsByUser(user.getId());

        List<Map<String, Object>> result = reposts.stream().map(r -> {
            Post post = r.getPost();

            List<Map<String, Object>> mediaList = post.getCarouselMedia().stream().map(m -> {
                Map<String, Object> media = new java.util.HashMap<>();
                media.put("mediaUrl", m.getMediaUrl());
                media.put("mediaType", m.getMediaType());
                return media;
            }).collect(java.util.stream.Collectors.toList());

            Map<String, Object> postMap = new java.util.HashMap<>();
            postMap.put("id", post.getId());
            postMap.put("caption", post.getCaption());
            postMap.put("carouselMedia", mediaList);

            Map<String, Object> repostMap = new java.util.HashMap<>();
            repostMap.put("id", r.getId());
            repostMap.put("caption", r.getCaption());
            repostMap.put("createdAt", r.getCreatedAt());
            repostMap.put("post", postMap);

            return repostMap;
        }).collect(java.util.stream.Collectors.toList());

        return ResponseEntity.ok(result);
    }
}