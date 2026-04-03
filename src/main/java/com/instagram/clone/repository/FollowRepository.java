package com.instagram.clone.repository;

import com.instagram.clone.entity.Follow;
import com.instagram.clone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    Optional<Follow> findByFollowerAndFollowing(User follower, User following);

    long countByFollower(User follower);

    long countByFollowing(User following);

    List<Follow> findAllByFollowing(User following);

    List<Follow> findAllByFollower(User follower);
}