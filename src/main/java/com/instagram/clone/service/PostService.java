package com.instagram.clone.service;

import com.instagram.clone.entity.Post;
import com.instagram.clone.entity.PostMedia; // Make sure to import this
import com.instagram.clone.entity.User;
import com.instagram.clone.repository.PostRepository;
import com.instagram.clone.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Service
public class PostService {

    @Autowired
    private CloudinaryService cloudinaryService;

    @Autowired
    private PostRepository postRepository;

    @Autowired
    private UserRepository userRepository;

    @Transactional
    public Post createPost(List<MultipartFile> files, String caption, Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User does not exist"));

        Post post = Post.builder()
                .caption(caption)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);

            // Upload to Cloudinary
            Map<String, Object> uploadResult = cloudinaryService.uploadFile(file);

            String mediaUrl = uploadResult.get("secure_url").toString();
            String publicId = uploadResult.get("public_id").toString();
            String resourceType = uploadResult.get("resource_type").toString();
            String mediaType = resourceType.equals("video") ? "VIDEO" : "IMAGE";

            // Create the PostMedia entity for this specific slide
            PostMedia postMedia = PostMedia.builder()
                    .mediaUrl(mediaUrl)
                    .publicId(publicId)
                    .mediaType(mediaType)
                    .sortOrder(i) // i represents the slide index (0 to 9)
                    .build();

            // 3. Attach it to the post using the helper method we created in the entity
            post.addMedia(postMedia);
        }

        // 4. Save the post. The CascadeType.ALL on carouselMedia will automatically save the PostMedia entities to the database.
        return postRepository.save(post);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAllByOrderByCreatedAtDesc();
    }

    public List<Post> getPostsByUserId(Long userId) {
        return postRepository.findByUser_Id(userId);
    }

    public Post getPostById(Long postId) {
        return postRepository.getPostById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
    }

    @Transactional
    public void deletePost(Long postId) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));

        // 1. Loop through all media attachments and delete them from Cloudinary
        for (PostMedia media : post.getCarouselMedia()) {
            try {
                cloudinaryService.deleteFile(media.getPublicId());
            } catch (Exception e) {
                System.err.println("Cloudinary delete failed for publicId " + media.getPublicId() + ": " + e.getMessage());
            }
        }

        // 2. Delete the post from the database (CascadeType.ALL handles deleting the PostMedia rows)
        try {
            postRepository.delete(post);
        } catch (Exception e) {
            System.err.println("Post deletion did not happen. " + e.getMessage());
        }
    }

    @Transactional
    public void updateCaption(Long postId, String caption) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new RuntimeException("Post not found"));
        post.setCaption(caption);
        postRepository.save(post);
    }
}