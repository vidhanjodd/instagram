package com.instagram.clone.controller;

import com.instagram.clone.dto.CommentRequest;
import com.instagram.clone.dto.CommentResponse;
import com.instagram.clone.entity.User;
import com.instagram.clone.repository.UserRepository;
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
    private final UserRepository userRepository;

    @GetMapping("/post/{postId}")
    @ResponseBody
    public ResponseEntity<List<CommentResponse>> getPostComments(@PathVariable Long postId) {
        try {
            List<CommentResponse> comments = commentService.getCommentsForPost(postId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PostMapping("/post/{postId}")
    @ResponseBody
    public ResponseEntity<Object> createPostComment(@PathVariable Long postId,
                                                     @RequestBody CommentRequest request,
                                                     Authentication authentication) {
        try {
            request.setPostId(postId);
            User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
            request.setUserId(user.getId());
            request.validate();

            CommentResponse response = commentService.createTopLevelComment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Validation error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/post/{postId}/reply")
    @ResponseBody
    public ResponseEntity<Object> replyToPostComment(@PathVariable Long postId,
                                                      @RequestParam Long parentId,
                                                      @RequestBody CommentRequest request,
                                                      Authentication authentication) {
        try {

            request.setPostId(postId);
            request.setParentId(parentId);
            request.setReelId(null);
            User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
            request.setUserId(user.getId());

            CommentResponse response = commentService.addReply(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/reel/{reelId}")
    @ResponseBody
    public ResponseEntity<List<CommentResponse>> getReelComments(@PathVariable Long reelId) {
        try {
            List<CommentResponse> comments = commentService.getCommentsForReel(reelId);
            return ResponseEntity.ok(comments);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PostMapping("/reel/{reelId}")
    @ResponseBody
    public ResponseEntity<Object> createReelComment(@PathVariable Long reelId,
                                                     @RequestBody CommentRequest request,
                                                     Authentication authentication) {
        try {

            request.setReelId(reelId);
            request.setPostId(null);
            User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
            request.setUserId(user.getId());
            request.validate();

            CommentResponse response = commentService.createTopLevelComment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Validation error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/reel/{reelId}/reply")
    @ResponseBody
    public ResponseEntity<Object> replyToReelComment(@PathVariable Long reelId,
                                                      @RequestParam Long parentId,
                                                      @RequestBody CommentRequest request,
                                                      Authentication authentication) {
        try {

            request.setReelId(reelId);
            request.setPostId(null);
            request.setParentId(parentId);
            User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
            request.setUserId(user.getId());

            CommentResponse response = commentService.addReply(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @PostMapping("/delete/{commentId}")
    @ResponseBody
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId,
                                                Authentication authentication) {
        try {
            User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
            commentService.deleteCommentIfOwner(commentId, user.getId());
            return ResponseEntity.ok("Comment deleted successfully");
        } catch (RuntimeException e) {
            if ("Not authorized".equals(e.getMessage())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only delete your own comments");
            }
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    @GetMapping("/replies/{parentId}")
    @ResponseBody
    public ResponseEntity<List<CommentResponse>> getReplies(@PathVariable Long parentId) {
        try {
            List<CommentResponse> replies = commentService.getRepliesToComment(parentId);
            return ResponseEntity.ok(replies);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(null);
        }
    }

    @PostMapping("/add")
    public String create(@ModelAttribute CommentRequest request) {
        if (request.getParentId() == null) {
            commentService.createTopLevelComment(request);
        } else {
            commentService.addReply(request);
        }

        return "redirect:/posts/" + request.getPostId();
    }
}