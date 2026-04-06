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
    private final NotificationService notificationService;
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
            Follow f = existingFollow.get();
            if(f.isPending()){
                notificationService.deleteFollowRequestNotification(f.getId());
            }
            followRepository.delete(existingFollow.get());
        }
        else {
            if(following.isPrivate()){
                if(followRepository.existsPendingRequest(follower,following)){
                    return;
                }
                Follow newFollow = Follow.builder()
                        .follower(follower)
                        .following(following)
                        .build();
                Follow saved = followRepository.save(newFollow);
                notificationService.notifyFollowRequest(currentUserId,targetUserId,saved.getId());
            }
            else {
                Follow newFollow = Follow.builder()
                        .follower(follower)
                        .following(following)
                        .status(Follow.FollowStatus.ACCEPTED)
                        .build();
                followRepository.save(newFollow);
                notificationService.notifyFollow(currentUserId,targetUserId);
            }

        }
    }
    @Transactional
    public void acceptFollowRequest(Long followId, Long ownerId) {
        Follow follow = followRepository.findById(followId)
                .orElseThrow(() -> new RuntimeException("Follow request not found"));

        if (!follow.getFollowing().getId().equals(ownerId)) {
            throw new RuntimeException("Not authorized to accept this request");
        }

        if (!follow.isPending()) {
            throw new RuntimeException("This request is not pending");
        }

        follow.setStatus(Follow.FollowStatus.ACCEPTED);
        followRepository.save(follow);

        notificationService.notifyFollowAccept(
                ownerId,
                follow.getFollower().getId(),
                follow.getId()
        );
    }
    @Transactional
    public void declineFollowRequest(Long followId, Long ownerId) {
        Follow follow = followRepository.findById(followId)
                .orElseThrow(() -> new RuntimeException("Follow request not found"));

        if (!follow.getFollowing().getId().equals(ownerId)) {
            throw new RuntimeException("Not authorized to decline this request");
        }

        notificationService.deleteFollowRequestNotification(follow.getId());
        followRepository.delete(follow);
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
    public long getFollowersCount(User user) {
        return followRepository.countAcceptedByFollowing(user);
    }

    public long getFollowingCount(User user) {
        return followRepository.countAcceptedByFollower(user);
    }

    public boolean isFollowing(User currentUser, User targetUser) {
        return followRepository.findByFollowerAndFollowing(currentUser, targetUser)
                .map(Follow::isAccepted)
                .orElse(false);
    }
    public boolean hasPendingRequest(User currentUser, User targetUser) {
        return followRepository.existsPendingRequest(currentUser, targetUser);
    }
}