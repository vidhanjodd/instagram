package com.instagram.clone.service;

import com.instagram.clone.entity.Follow;
import com.instagram.clone.entity.User;
import com.instagram.clone.repository.FollowRepository;
import com.instagram.clone.repository.UserRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FollowService {

    private final FollowRepository followRepository;
    private final UserRepository userRepository;

    @Transactional
    public void toggleFollow(Long currentUserId, Long targetUserId) {
        if (currentUserId.equals(targetUserId)) {
            throw new RuntimeException("You cannot follow yourself.");
        }

        User follower = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        User following = userRepository.findById(targetUserId)
                .orElseThrow(() -> new RuntimeException("Target user not found"));

        Optional<Follow> existingFollow = followRepository.findByFollowerAndFollowing(follower, following);

        if (existingFollow.isPresent()) {
            followRepository.delete(existingFollow.get());
        } else {
            Follow newFollow = Follow.builder()
                    .follower(follower)
                    .following(following)
                    .build();
            followRepository.save(newFollow);
        }
    }

    @Transactional
    public void removeFollower(Long currentUserId, Long followerToRemoveId) {
        User currentUser = userRepository.findById(currentUserId)
                .orElseThrow(() -> new RuntimeException("Current user not found"));
        User followerToRemove = userRepository.findById(followerToRemoveId)
                .orElseThrow(() -> new RuntimeException("Follower not found"));

        Optional<Follow> existingFollow = followRepository.findByFollowerAndFollowing(followerToRemove, currentUser);

        existingFollow.ifPresent(followRepository::delete);
    }
}