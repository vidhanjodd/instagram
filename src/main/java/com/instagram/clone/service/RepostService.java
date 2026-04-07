package com.instagram.clone.service;

import com.instagram.clone.entity.Post;
import com.instagram.clone.entity.Repost;
import com.instagram.clone.entity.User;
import com.instagram.clone.repository.PostRepository;
import com.instagram.clone.repository.RepostRepository;
import com.instagram.clone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RepostService {

    private final RepostRepository repostRepository;
    private final PostRepository postRepository;
    private final UserRepository userRepository;

    @Transactional
    public boolean toggleRepost(Long currentUserId, Long postId, String caption) {

        User user = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        if (post.getUser().getId().equals(currentUserId)) {
            throw new RuntimeException("You cannot repost your own post.");
        }

        Optional<Repost> existing = repostRepository.findByUserAndPost(user, post);

        if (existing.isPresent()) {
            repostRepository.delete(existing.get());
            return false;
        } else {
            Repost repost = Repost.builder()
                    .user(user)
                    .post(post)
                    .caption(caption)
                    .build();
            repostRepository.save(repost);
            return true;
        }
    }

    public boolean hasReposted(Long userId, Long postId) {
        User user = userRepository.findById(userId).orElseThrow();
        Post post = postRepository.findById(postId).orElseThrow();
        return repostRepository.existsByUserAndPost(user, post);
    }


    public long getRepostCount(Long postId) {
        Post post = postRepository.findById(postId).orElseThrow();
        return repostRepository.countByPost(post);
    }

    @Transactional
    public List<Repost> getRepostsByUser(Long userId) {
        User user = userRepository.findById(userId).orElseThrow();
        return repostRepository.findByUserOrderByCreatedAtDesc(user);
    }


    public List<Repost> getFeedReposts(Long currentUserId) {
        User currentUser = userRepository.findById(currentUserId).orElseThrow();
        return repostRepository.findFeedRepostsByFollowedUsers(currentUser);
    }
}