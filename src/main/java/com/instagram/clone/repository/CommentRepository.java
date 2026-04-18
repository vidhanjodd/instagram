package com.instagram.clone.repository;

import com.instagram.clone.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByParentIsNullAndReelId(Long reelId);

    List<Comment> findByParentIsNullAndPostId(Long postId);

    List<Comment> findByParentId(Long parentId);
}