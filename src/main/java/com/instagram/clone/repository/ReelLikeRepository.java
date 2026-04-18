package com.instagram.clone.repository;

import com.instagram.clone.entity.Reel;
import com.instagram.clone.entity.ReelLike;
import com.instagram.clone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ReelLikeRepository extends JpaRepository<ReelLike, Long> {

    boolean existsByUserAndReel(User user, Reel reel);

    Optional<ReelLike> findByUserAndReel(User user, Reel reel);

    long countByReel(Reel reel);
}