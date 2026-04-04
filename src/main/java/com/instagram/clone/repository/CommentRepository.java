package com.instagram.clone.repository;

import com.instagram.clone.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // Top-level comments for a reel (no replies)
    List<Comment> findByParentIsNullAndReelId(Long reelId);

    // Top-level comments for a post
    List<Comment> findByParentIsNullAndPostId(Long postId);

    // Replies to a comment
    List<Comment> findByParentId(Long parentId);
}