package com.instagram.clone.controller;

import com.instagram.clone.dto.CommentRequest;
import com.instagram.clone.dto.CommentResponse;
import com.instagram.clone.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping
    public CommentResponse create(@RequestBody CommentRequest request) {
        return commentService.createComment(request);
    }

    @GetMapping("/post/{postId}")
    public List<CommentResponse> get(@PathVariable Long postId) {
        return commentService.getComments(postId);
    }
}