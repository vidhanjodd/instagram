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

    public CommentResponse createTopLevelComment(CommentRequest request) {
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

        Comment saved = commentRepository.save(comment);
        return mapToBasicResponse(saved);
    }

    public CommentResponse addReply(CommentRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Comment parentComment = commentRepository.findById(request.getParentId())
                .orElseThrow(() -> new RuntimeException("Parent comment not found"));

        Comment rootComment = parentComment;
        while (rootComment.getParent() != null) {
            rootComment = rootComment.getParent();
        }

        String taggedContent = request.getReplyingToUsername() != null
                ? "@" + request.getReplyingToUsername() + " " + request.getContent()
                : request.getContent();

        Comment reply = Comment.builder()
                .user(user)
                .post(post)
                .content(taggedContent)
                .parent(rootComment)
                .build();

         Comment saved = commentRepository.save(reply);
         return mapToBasicResponse(reply);
    }

    public List<CommentResponse> getCommentsForPost(Long postId) {
        List<Comment> topLevelComments = commentRepository.findByParentIsNullAndPostId(postId);

        return topLevelComments.stream()
                .map(this::mapToResponseWithReplyCount)
                .toList();
    }
    public List<CommentResponse> getRepliesToComment(Long parentId) {
        List<Comment> replies = commentRepository.findByParentId(parentId);

        return replies.stream()
                .map(this::mapToBasicResponse)
                .toList();
    }
    public void deleteComment(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));
        commentRepository.delete(comment);
    }
    private CommentResponse mapToResponseWithReplyCount(Comment comment) {

        List<CommentResponse> replies = (comment.getReplies() != null)
                ? comment.getReplies().stream()
                .map(this::mapToResponseWithReplyCount)
                .toList()
                : Collections.emptyList();

        return CommentResponse.builder()
                .id(comment.getId())
                .username(comment.getUser().getUsername())
                .userId(comment.getUser().getId())
                .content(comment.getContent())
                .profilePicUrl(comment.getUser().getProfilePicUrl())
                .createdAt(comment.getCreatedAt())
                .replyCount(replies.size())
                .replies(replies)
                .build();
    }

    private CommentResponse mapToBasicResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .username(comment.getUser().getUsername())
                .userId(comment.getUser().getId())
                .content(comment.getContent())
                .profilePicUrl(comment.getUser().getProfilePicUrl())
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