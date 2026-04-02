package com.instagram.clone.repository;

import com.instagram.clone.entity.Follow;
import com.instagram.clone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    // Check if a specific follow relationship exists
    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    // Count how many people a user is following
    long countByFollower(User follower);

    // Count how many followers a user has
    long countByFollowing(User following);

    List<Follow> findAllByFollowing(User following);

    // NEW: get all Follow records where 'follower' = user (i.e., all people this user follows)
    List<Follow> findAllByFollower(User follower);
}