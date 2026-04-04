package com.instagram.clone.repository;

import com.instagram.clone.entity.Post;
import com.instagram.clone.entity.SavedPost;
import com.instagram.clone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface SavedPostRepository extends JpaRepository<SavedPost, Long> {
    Optional<SavedPost> findByUserAndPost(User user, Post post);
    List<SavedPost> findByUserOrderByCreatedAtDesc(User user);
    boolean existsByUserAndPost(User user, Post post);
}