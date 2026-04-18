package com.instagram.clone.repository;

import com.instagram.clone.entity.Post;
import com.instagram.clone.entity.PostLike;
import com.instagram.clone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

public interface PostLikeRepository extends JpaRepository<PostLike, Long> {

    Optional<PostLike> findByUserAndPost(User user, Post post);

    long countByPost(Post post);
    boolean existsByUserIdAndPostId(Long userId, Long postId);

    @Transactional
    @Modifying
    void deleteByUserIdAndPostId(Long userId, Long postId);
}