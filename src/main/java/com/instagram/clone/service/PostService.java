package com.instagram.clone.service;

import com.instagram.clone.entity.Post;
import com.instagram.clone.repository.PostRepository;
import com.instagram.clone.repository.UserRepository; // ✅ added
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
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

        if (!userRepository.existsById(userId)) {
            throw new RuntimeException("User does not exist");
        }

        // 1. Upload to Cloudinary
        Map<String, Object> uploadResult = cloudinaryService.uploadFile(file);

        // 2. Extract important fields
        String mediaUrl = uploadResult.get("secure_url").toString();
        String publicId = uploadResult.get("public_id").toString();
        String resourceType = uploadResult.get("resource_type").toString();

        // 3. Decide media type
        String mediaType = resourceType.equals("video") ? "VIDEO" : "IMAGE";

        // 4. Create Post object
        Post post = Post.builder()
                .caption(caption)
                .mediaUrl(mediaUrl)
                .mediaType(mediaType)
                .publicId(publicId)
                .userId(userId)
                .createdAt(LocalDateTime.now())
                .build();

        // 5. Save to DB
        return postRepository.save(post);
    }

    public List<Post> getAllPosts() {
        return postRepository.findAll();
    }
}