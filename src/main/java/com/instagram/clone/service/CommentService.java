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

    // 1. Handles normal top-level comments
    public void createTopLevelComment(CommentRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment comment = Comment.builder()
                .user(user)
                .post(post)
                .content(request.getContent())
                .parent(null) // Top-level has no parent
                .build();

        commentRepository.save(comment);
    }

    // 2. NEW: Handles replies to existing comments
    public void addReply(CommentRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment parentComment = commentRepository.findById(request.getParentId())
                .orElseThrow(() -> new RuntimeException("Parent comment not found"));

        // Format the content to tag the original commenter: "@username original content"
        String taggedContent = "@" + parentComment.getUser().getUsername() + " " + request.getContent();

        Comment reply = Comment.builder()
                .user(user)
                .post(post)
                .content(taggedContent)
                .parent(parentComment) // Link this reply to the parent comment
                .build();

        commentRepository.save(reply);
    }

    // 3. Fetching comments (Grabs top-level, mapping handles the rest)
    public List<CommentResponse> getCommentsForPost(Long postId) {
        List<Comment> topLevelComments = commentRepository.findByParentIsNullAndPostId(postId);

        return topLevelComments.stream()
                .map(this::mapToBasicResponse)
                .toList();
    }

    // 4. NEW: Recursive mapping to handle infinite layers of replies
    private CommentResponse mapToBasicResponse(Comment comment) {

        // If there are replies, map them. Otherwise, return an empty list.
        List<CommentResponse> mappedReplies = comment.getReplies() != null ?
                comment.getReplies().stream().map(this::mapToBasicResponse).toList() :
                Collections.emptyList();

        return CommentResponse.builder()
                .id(comment.getId())
                .username(comment.getUser().getUsername())
                .content(comment.getContent())
                .createdAt(comment.getCreatedAt())
                .replies(mappedReplies) // Attach the mapped nested replies here
                .build();
    }
}