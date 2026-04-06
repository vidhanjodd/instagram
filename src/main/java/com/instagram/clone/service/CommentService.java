package com.instagram.clone.service;

import com.instagram.clone.dto.CommentRequest;
import com.instagram.clone.dto.CommentResponse;
import com.instagram.clone.entity.Comment;
import com.instagram.clone.entity.Post;
import com.instagram.clone.entity.Reel;
import com.instagram.clone.entity.User;
import com.instagram.clone.repository.CommentRepository;
import com.instagram.clone.repository.PostRepository;
import com.instagram.clone.repository.ReelRepository;
import com.instagram.clone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final ReelRepository reelRepository;
    private final NotificationService notificationService;

    @Transactional
    public CommentResponse createTopLevelComment(CommentRequest request) {
        request.validate();

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Comment comment = Comment.builder()
                .user(user)
                .content(request.getContent())
                .parent(null)
                .build();

        if (request.isPostComment()) {
            Post post = postRepository.findById(request.getPostId())
                    .orElseThrow(() -> new RuntimeException("Post not found"));
            comment.setPost(post);
            post.getComments().add(comment);
            commentRepository.save(comment);
            if (!user.getId().equals(post.getUser().getId())) {
                notificationService.notifyPostComment(user.getId(), post.getUser().getId(), post.getId());
            }
        } else {
            Reel reel = reelRepository.findById(request.getReelId())
                    .orElseThrow(() -> new RuntimeException("Reel not found"));
            comment.setReel(reel);
            reel.addComment(comment);
            commentRepository.save(comment);
            if (!user.getId().equals(reel.getUser().getId())) {
                notificationService.notifyReelComment(user.getId(), reel.getUser().getId(), reel.getId());
            }
        }

        commentRepository.save(comment);
        
        return mapToBasicResponse(comment);
    }

    @Transactional
    public CommentResponse addReply(CommentRequest request) {
        return createReplyInternal(request);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsForPost(Long postId) {
        return commentRepository.findByParentIsNullAndPostId(postId)
                .stream()
                .map(this::mapToResponseWithReplies)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsForReel(Long reelId) {
        return commentRepository.findByParentIsNullAndReelId(reelId)
                .stream()
                .map(this::mapToResponseWithReplies)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getCommentsByReelId(Long reelId) {
        return getCommentsForReel(reelId);
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> getRepliesToComment(Long parentId) {
        return commentRepository.findByParentId(parentId)
                .stream()
                .map(this::mapToBasicResponse)
                .toList();
    }

    @Transactional
    public void deleteComment(Long commentId) {
        throw new UnsupportedOperationException("Use deleteCommentIfOwner to ensure ownership check.");
    }

    @Transactional(readOnly = true)
    public CommentResponse getCommentById(Long commentId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        return mapToResponseWithReplies(comment);
    }

    private CommentResponse mapToResponseWithReplies(Comment comment) {

        List<CommentResponse> replies = comment.getReplies() != null
                ? comment.getReplies().stream()
                .map(this::mapToResponseWithReplies)
                .toList()
                : Collections.emptyList();

        return CommentResponse.builder()
                .id(comment.getId())
                .username(comment.getUser() != null ? comment.getUser().getUsername() : "unknown")
                .userId(comment.getUser() != null ? comment.getUser().getId() : null)
                .content(comment.getContent())
                .profilePicUrl(comment.getUser() != null ? comment.getUser().getProfilePicUrl() : null)
                .createdAt(comment.getCreatedAt())
                .replyCount(replies.size())
                .replies(replies)
                .build();
    }

    private CommentResponse mapToBasicResponse(Comment comment) {
        return CommentResponse.builder()
                .id(comment.getId())
                .username(comment.getUser() != null ? comment.getUser().getUsername() : "unknown")
                .userId(comment.getUser() != null ? comment.getUser().getId() : null)
                .content(comment.getContent())
                .profilePicUrl(comment.getUser() != null ? comment.getUser().getProfilePicUrl() : null)
                .createdAt(comment.getCreatedAt())
                .replyCount(0)
                .replies(Collections.emptyList())
                .build();
    }

    @Transactional
    public CommentResponse createComment(CommentRequest request) {

        if (request.getParentId() != null) {
            return createReply(request);
        }

        request.validate();

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new RuntimeException("User not found"));

        Reel reel = reelRepository.findById(request.getReelId())
                .orElseThrow(() -> new RuntimeException("Reel not found"));

        Comment comment = Comment.builder()
                .user(user)
                .content(request.getContent())
                .reel(reel)
                .build();

        reel.addComment(comment);
        commentRepository.save(comment);

        notificationService.notifyReelComment(user.getId(),
                reel.getUser().getId(),
                reel.getId());

        return CommentResponse.builder()
                .id(comment.getId())
                .username(user.getUsername())
                .userId(user.getId())
                .content(comment.getContent())
                .profilePicUrl(user.getProfilePicUrl())
                .createdAt(comment.getCreatedAt())
                .replyCount(0)
                .replies(List.of())
                .build();
    }

        @Transactional
        private CommentResponse createReply(CommentRequest request) {
        return createReplyInternal(request);
        }

        private CommentResponse createReplyInternal(CommentRequest request) {
        User user = userRepository.findById(request.getUserId())
            .orElseThrow(() -> new RuntimeException("User not found"));

        Comment parent = commentRepository.findById(request.getParentId())
            .orElseThrow(() -> new RuntimeException("Parent comment not found"));

        String content = request.getReplyingToUsername() != null
            ? "@" + request.getReplyingToUsername() + " " + request.getContent()
            : request.getContent();

        Comment reply = Comment.builder()
            .user(user)
            .content(content)
            .parent(parent)
            .build();

        if (parent.getPost() != null) {
            reply.setPost(parent.getPost());
        } else {
            reply.setReel(parent.getReel());
        }

        commentRepository.save(reply);

        return CommentResponse.builder()
            .id(reply.getId())
            .username(user.getUsername())
            .userId(user.getId())
            .content(reply.getContent())
            .profilePicUrl(user.getProfilePicUrl())
            .createdAt(reply.getCreatedAt())
            .replyCount(0)
            .replies(List.of())
            .build();
        }

    @Transactional
    public void deleteCommentIfOwner(Long commentId, Long userId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new RuntimeException("Comment not found"));

        if (!comment.getUser().getId().equals(userId))
            throw new RuntimeException("Not authorized");

        deleteCommentAndReplies(comment);
    }

    private void deleteCommentAndReplies(Comment comment) {
        if (comment.getReplies() != null && !comment.getReplies().isEmpty()) {
            for (Comment reply : new ArrayList<>(comment.getReplies())) {
                deleteCommentAndReplies(reply);
            }
        }

        if (comment.getReel() != null) {
            comment.getReel().removeComment(comment);
        } else if (comment.getPost() != null) {
            comment.getPost().getComments().remove(comment);
        }

        if (comment.getParent() != null) {
            comment.getParent().getReplies().remove(comment);
        }

        commentRepository.delete(comment);
    }
}