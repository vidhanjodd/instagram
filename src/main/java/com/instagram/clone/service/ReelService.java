package com.instagram.clone.service;

import com.instagram.clone.entity.Reel;
import com.instagram.clone.entity.User;
import com.instagram.clone.repository.ReelRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class ReelService {

    private final ReelRepository reelRepository;
    private final CloudinaryService cloudinaryService;

    @Transactional
    public Reel createReel(MultipartFile file, String caption, User user) {

        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("Video file is required");
        }

        Map<String, Object> uploadResult = cloudinaryService.uploadFile(file);

        String videoUrl = uploadResult.get("secure_url").toString();
        String publicId = uploadResult.get("public_id").toString();

        Reel reel = new Reel();
        reel.setVideoUrl(videoUrl);
        reel.setPublicId(publicId);
        reel.setCaption(caption);
        reel.setUser(user);

        return reelRepository.save(reel);
    }

    @Transactional(readOnly = true)
    public List<Reel> getAllReels() {
        return reelRepository.findAllByOrderByCreatedAtDesc();
    }

    @Transactional(readOnly = true)
    public List<Reel> getReelsByUserId(Long userId) {
        return reelRepository.findByUserIdOrderByCreatedAtDesc(userId);
    }

    @Transactional(readOnly = true)
    public Reel getReelById(Long reelId) {
        return reelRepository.findById(reelId)
                .orElseThrow(() -> new RuntimeException("Reel not found with ID: " + reelId));
    }

    @Transactional
    public long incrementViewCount(Long reelId) {
        Reel reel = getReelById(reelId);
        reel.setViewCount(reel.getViewCount() + 1);
        reelRepository.save(reel);
        return reel.getViewCount();
    }

    @Transactional
    public long incrementLikeCount(Long reelId) {
        Reel reel = getReelById(reelId);
        reel.setLikeCount(reel.getLikeCount() + 1);
        reelRepository.save(reel);
        return reel.getLikeCount();
    }

    @Transactional(readOnly = true)
    public long getViewCount(Long reelId) {
        return getReelById(reelId).getViewCount();
    }

    @Transactional
    public void deleteReel(Long reelId, User currentUser) {
        Reel reel = getReelById(reelId);

        if (!reel.getUser().getId().equals(currentUser.getId())) {
            throw new RuntimeException("You are not allowed to delete this reel");
        }

        try {
            cloudinaryService.deleteFile(reel.getPublicId());
        } catch (Exception e) {
            System.err.println("Cloudinary delete failed: " + e.getMessage());
        }

        reelRepository.delete(reel);
    }

    @Transactional(readOnly = true)
    public String generateReelShareUrl(Long reelId) {
        Reel reel = getReelById(reelId);
        return "/reels/" + reel.getId();
    }
}