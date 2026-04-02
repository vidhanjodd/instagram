package com.instagram.clone.service;

import com.instagram.clone.dto.CommentRequest;
import com.instagram.clone.dto.CommentResponse;
import com.instagram.clone.entity.Comment;
import com.instagram.clone.entity.Post;
import com.instagram.clone.entity.User;
import com.instagram.clone.repository.CommentRepository;
import com.instagram.clone.repository.PostRepository;
import com.instagram.clone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;

    public void createTopLevelComment(CommentRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = Comment.builder()
                .user(user)
                .post(post)
                .content(request.getContent())
                .parent(null)
                .build();

        commentRepository.save(comment);
    }

    // 2. Creates a reply to an existing comment
    public void addReply(CommentRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment parentComment = commentRepository.findById(request.getParentId())
                .orElseThrow(() -> new RuntimeException("Parent comment not found"));

        // Tag the original commenter automatically
        String taggedContent;

        if (request.getReplyingToUsername() != null) {
            taggedContent = "@" + request.getReplyingToUsername() + " " + request.getContent();
        } else {
            taggedContent = request.getContent();
        }

        Comment reply = Comment.builder()
                .user(user)
                .post(post)
                .content(taggedContent)
                .parent(parentComment)
                .build();

        commentRepository.save(reply);
    }

    // 3. Returns only top-level (parent) comments for a post
    //    Replies are NOT loaded here — they are loaded on demand via getRepliesToComment()
    public List<CommentResponse> getCommentsForPost(Long postId) {
        List<Comment> topLevelComments = commentRepository.findByParentIsNullAndPostId(postId);

        return topLevelComments.stream()
                .map(this::mapToResponseWithReplyCount)
                .toList();
    }

    // 4. Returns direct replies for a parent comment (called when user clicks "View X replies")
    //    Only ONE level deep — we load replies flat, just like Instagram
    public List<CommentResponse> getRepliesToComment(Long parentId) {
        List<Comment> replies = commentRepository.findByParentId(parentId);

        return replies.stream()
                .map(this::mapToBasicResponse)
                .toList();
    }

    // 5. Deletes a comment (and its replies cascade automatically due to orphanRemoval=true on Comment entity)
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        commentRepository.delete(comment);
    }

    // Maps a top-level comment — includes reply count but NOT the actual replies
    // This is what we show initially in the popup (Instagram-style: lazy replies)
    private CommentResponse mapToResponseWithReplyCount(Comment comment) {
        int replyCount = (comment.getReplies() != null) ? comment.getReplies().size() : 0;

        return CommentResponse.builder()
                .id(comment.getId())
                .username(comment.getUser().getUsername())
                .userId(comment.getUser().getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .replyCount(replyCount)
                .replies(Collections.emptyList()) // Replies are NOT sent initially
                .build();
    }

    private CommentResponse mapToBasicResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .username(comment.getUser().getUsername())
                .userId(comment.getUser().getId())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .replyCount(0)
                .replies(Collections.emptyList())
                .build();
    }

    public CommentResponse getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        return mapToResponseWithReplyCount(comment);
    }
}