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
    private final UserRepository userRepository;
    private final PostRepository postRepository;


    public boolean toggleLike(String username, Long postId) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        boolean alreadyLiked = postLikeRepository.existsByUserIdAndPostId(user.getId(), postId);

        if (alreadyLiked) {
            postLikeRepository.deleteByUserIdAndPostId(user.getId(), postId);
            return false;
        } else {
            Post post = postRepository.findById(postId)
                    .orElseThrow(() -> new RuntimeException("Post not found"));

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