package com.instagram.clone.service;

import com.instagram.clone.entity.Reel;
import com.instagram.clone.entity.ReelLike;
import com.instagram.clone.entity.User;
import com.instagram.clone.repository.ReelLikeRepository;
import com.instagram.clone.repository.ReelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ReelLikeService {

    private final ReelLikeRepository reelLikeRepository;
    private final ReelRepository reelRepository;

    @Transactional
    public long likeReel(Long reelId, User user) {

        Reel reel = reelRepository.findById(reelId)
                .orElseThrow(() -> new RuntimeException("Reel not found"));

        if (reelLikeRepository.existsByUserAndReel(user, reel)) {
            return reel.getLikeCount();
        }

        ReelLike like = new ReelLike();
        like.setUser(user);
        like.setReel(reel);

        reelLikeRepository.save(like);

        reel.setLikeCount(reel.getLikeCount() + 1);

        return reel.getLikeCount();
    }

    @Transactional
    public long unlikeReel(Long reelId, User user) {

        Reel reel = reelRepository.findById(reelId)
                .orElseThrow(() -> new RuntimeException("Reel not found"));

        ReelLike like = reelLikeRepository.findByUserAndReel(user, reel)
                .orElseThrow(() -> new RuntimeException("Like not found"));

        reelLikeRepository.delete(like);

        reel.setLikeCount(Math.max(0, reel.getLikeCount() - 1));

        return reel.getLikeCount();
    }

    @Transactional(readOnly = true)
    public boolean hasUserLiked(Long reelId, User user) {

        Reel reel = reelRepository.findById(reelId)
                .orElseThrow(() -> new RuntimeException("Reel not found"));

        return reelLikeRepository.existsByUserAndReel(user, reel);
    }
}