package com.instagram.clone.repository;

import com.instagram.clone.entity.Comment;
import jakarta.transaction.Transactional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment,Long> {
    List<Comment> findByParentIsNullAndPostId(Long postId);
    List<Comment> findByParentId(Long parentId);

    @Modifying
    @Transactional
    @Query("DELETE FROM Comment c WHERE c.user.id = :userId")
    void deleteAllByUserId(@Param("userId") Long userId);
}
