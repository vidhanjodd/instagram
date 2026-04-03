package com.instagram.clone.controller;

import com.instagram.clone.dto.CommentRequest;
import com.instagram.clone.dto.CommentResponse;
import com.instagram.clone.service.CommentService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    @PostMapping("/add")
    public String create(@ModelAttribute CommentRequest request) {

        if (request.getParentId() == null) {
            commentService.createTopLevelComment(request);
        } else {
            commentService.addReply(request);
        }

        return "redirect:/posts/" + request.getPostId();
    }


    @PostMapping("/delete/{commentId}")
    @ResponseBody
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId,
                                                Authentication authentication) {
        try {
            CommentResponse comment = commentService.getCommentById(commentId);

            if (!comment.getUsername().equals(authentication.getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("You can only delete your own comments");
            }

            commentService.deleteComment(commentId);
            return ResponseEntity.ok("deleted");
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("error: " + e.getMessage());
        }
    }

    @GetMapping("/replies/{parentId}")
    @ResponseBody
    public ResponseEntity<List<CommentResponse>> getReplies(@PathVariable Long parentId) {
        List<CommentResponse> replies = commentService.getRepliesToComment(parentId);
        return ResponseEntity.ok(replies);
    }
}