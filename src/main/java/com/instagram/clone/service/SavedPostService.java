package com.instagram.clone.service;

import com.instagram.clone.entity.Post;
import com.instagram.clone.entity.SavedPost;
import com.instagram.clone.entity.User;
import com.instagram.clone.repository.PostRepository;
import com.instagram.clone.repository.SavedPostRepository;
import com.instagram.clone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class SavedPostService {

    private final SavedPostRepository savedPostRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean toggleSave(Long userId, Long postId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        Optional<SavedPost> existingSave = savedPostRepository.findByUserAndPost(user, post);

        if (existingSave.isPresent()) {
            savedPostRepository.delete(existingSave.get());
            return false;
        } else {
            SavedPost savedPost = SavedPost.builder()
                    .user(user)
                    .post(post)
                    .build();
            savedPostRepository.save(savedPost);
            return true;
        }
    }

    public boolean isPostSavedByUser(User user, Post post) {
        return savedPostRepository.existsByUserAndPost(user, post);
    }
}