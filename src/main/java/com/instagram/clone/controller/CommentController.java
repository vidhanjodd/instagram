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

/**
 * REST API controller for managing comments on Posts and Reels.
 * Supports both POST and Reel comment operations with polymorphic handling.
 */
@Controller
@RequestMapping("/comments")
@RequiredArgsConstructor
public class CommentController {

    private final CommentService commentService;

    // ==================== POST Comments Endpoints ====================

    /**
     * Get all top-level comments for a Post.
     * Includes nested replies for each comment.
     *
     * @param postId Post ID
     * @return List of CommentResponse objects with nested replies
     */
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

    /**
     * Create a top-level comment on a Post via JSON.
     *
     * @param postId Post ID
     * @param request CommentRequest with postId, userId, and content
     * @param authentication Current user authentication
     * @return Created CommentResponse or error message
     */
    @PostMapping("/post/{postId}")
    @ResponseBody
    public ResponseEntity<Object> createPostComment(@PathVariable Long postId,
                                                     @RequestBody CommentRequest request,
                                                     Authentication authentication) {
        try {
            request.setPostId(postId);
            request.setUserId(Long.parseLong(authentication.getName()));
            request.validate();

            commentService.createTopLevelComment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Comment created successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Validation error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * Reply to a comment on a Post.
     *
     * @param postId Post ID
     * @param parentId Parent comment ID
     * @param request CommentRequest with content
     * @param authentication Current user authentication
     * @return Created reply CommentResponse or error message
     */
    @PostMapping("/post/{postId}/reply")
    @ResponseBody
    public ResponseEntity<Object> replyToPostComment(@PathVariable Long postId,
                                                      @RequestParam Long parentId,
                                                      @RequestBody CommentRequest request,
                                                      Authentication authentication) {
        try {
            request.setPostId(postId);
            request.setParentId(parentId);
            request.setUserId(Long.parseLong(authentication.getName()));

            commentService.addReply(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Reply created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    // ==================== REEL Comments Endpoints ====================

    /**
     * Get all top-level comments for a Reel.
     * Includes nested replies for each comment.
     *
     * @param reelId Reel ID
     * @return List of CommentResponse objects with nested replies
     */
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

    /**
     * Create a top-level comment on a Reel via JSON.
     *
     * @param reelId Reel ID
     * @param request CommentRequest with reelId, userId, and content
     * @param authentication Current user authentication
     * @return Created CommentResponse or error message
     */
    @PostMapping("/reel/{reelId}")
    @ResponseBody
    public ResponseEntity<Object> createReelComment(@PathVariable Long reelId,
                                                     @RequestBody CommentRequest request,
                                                     Authentication authentication) {
        try {
            request.setReelId(reelId);
            request.setPostId(null);  // Ensure no post ID
            request.setUserId(Long.parseLong(authentication.getName()));
            request.validate();

            commentService.createTopLevelComment(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Comment created successfully");
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Validation error: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * Reply to a comment on a Reel.
     *
     * @param reelId Reel ID
     * @param parentId Parent comment ID
     * @param request CommentRequest with content
     * @param authentication Current user authentication
     * @return Created reply CommentResponse or error message
     */
    @PostMapping("/reel/{reelId}/reply")
    @ResponseBody
    public ResponseEntity<Object> replyToReelComment(@PathVariable Long reelId,
                                                      @RequestParam Long parentId,
                                                      @RequestBody CommentRequest request,
                                                      Authentication authentication) {
        try {
            request.setReelId(reelId);
            request.setPostId(null);  // Ensure no post ID
            request.setParentId(parentId);
            request.setUserId(Long.parseLong(authentication.getName()));

            commentService.addReply(request);
            return ResponseEntity.status(HttpStatus.CREATED).body("Reply created successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }


    // ==================== Generic Comment Endpoints ====================

    /**
     * Delete a comment by ID.
     * Only the comment author can delete their own comments.
     *
     * @param commentId Comment ID
     * @param authentication Current user authentication
     * @return Success message or error
     */
    @DeleteMapping("/{commentId}")
    @ResponseBody
    public ResponseEntity<String> deleteComment(@PathVariable Long commentId,
                                                 Authentication authentication) {
        try {
            CommentResponse comment = commentService.getCommentById(commentId);

            if (!comment.getUsername().equals(authentication.getName())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body("You can only delete your own comments");
            }

            commentService.deleteComment(commentId);
            return ResponseEntity.ok("Comment deleted successfully");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Error: " + e.getMessage());
        }
    }

    /**
     * Get all replies to a specific comment.
     *
     * @param parentId Parent comment ID
     * @return List of reply CommentResponse objects
     */
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

    // ==================== Legacy Form-Based Endpoint ====================

    /**
     * Legacy form-based comment creation endpoint.
     * Maintains backward compatibility with existing form submissions.
     * Automatically redirects to appropriate post page.
     *
     * @param request CommentRequest from form
     * @return Redirect to post details page
     */
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