package com.instagram.clone.service;

import com.instagram.clone.entity.Post;
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

    public Post createPost(MultipartFile file, String caption, Long userId) {

        Map<String, Object> uploadResult = cloudinaryService.uploadFile(file);

        String mediaUrl = uploadResult.get("secure_url").toString();
        String publicId = uploadResult.get("public_id").toString();
        String resourceType = uploadResult.get("resource_type").toString();

        String mediaType = resourceType.equals("video") ? "VIDEO" : "IMAGE";
        User user = userRepository.findById(userId).orElseThrow(() -> new RuntimeException("User does not exist"));

        Post post = Post.builder()
                .caption(caption)
                .mediaUrl(mediaUrl)
                .mediaType(mediaType)
                .publicId(publicId)
                .user(user)
                .createdAt(LocalDateTime.now())
                .build();

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

        try {
            postRepository.delete(post);
        } catch (Exception e) {
            System.err.println("Post deletion did not happen. " + e.getMessage());
        }

        try {
            cloudinaryService.deleteFile(post.getPublicId());
        } catch (Exception e) {
            System.err.println("Cloudinary delete failed: " + e.getMessage());
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