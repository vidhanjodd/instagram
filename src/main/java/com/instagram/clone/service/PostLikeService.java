package com.instagram.clone.service;

import com.instagram.clone.entity.Post;
import com.instagram.clone.entity.PostLike;
import com.instagram.clone.entity.User;
import com.instagram.clone.repository.PostLikeRepository;
import com.instagram.clone.repository.PostRepository;
import com.instagram.clone.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;
@Service
@RequiredArgsConstructor
public class PostLikeService {

    private final PostLikeRepository postLikeRepository;
    private final UserRepository userRepository; // Added repository here
    private final PostRepository postRepository;

    // Inside com.instagram.clone.service.PostLikeService

    public boolean toggleLike(String username, Long postId) {
        // 1. Fetch the user from the repository using the username from SecurityContext
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        // 2. Check if the like already exists for this specific User and Post
        boolean alreadyLiked = postLikeRepository.existsByUserIdAndPostId(user.getId(), postId);

        if (alreadyLiked) {
            // 3a. UNLIKE: If it exists, delete the record
            postLikeRepository.deleteByUserIdAndPostId(user.getId(), postId);
            return false;
        } else {
            // 3b. LIKE: If it doesn't exist, find the post and create a new Like record
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found"));

            // Create the Like entity (Assuming your PostLike entity uses @Builder or a constructor)
            PostLike postLike = PostLike.builder()
                    .user(user)
                    .post(post)
                    .build();

            postLikeRepository.save(postLike);
            return true;
        }
    }
    public long getLikeCount(Long postId) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        return postLikeRepository.countByPost(post);
    }
}