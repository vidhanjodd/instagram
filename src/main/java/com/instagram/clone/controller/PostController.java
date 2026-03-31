package com.instagram.clone.controller;

import com.instagram.clone.entity.Post;
import com.instagram.clone.entity.User;
import com.instagram.clone.repository.UserRepository;
import com.instagram.clone.service.PostService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@Controller
@RequestMapping("/posts")
@RequiredArgsConstructor
public class PostController {

    private final PostService postService;
    private final UserRepository userRepository; // ← to fetch logged-in user

    @GetMapping
    public String getAllPosts(Model model, Authentication authentication) {
        List<Post> posts = postService.getAllPosts();
        model.addAttribute("posts", posts);

        // Pass logged-in user to view
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("currentUser", user);

        return "posts/feed";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, Authentication authentication) {
        // Get logged-in user and pass their ID to the view
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("currentUserId", user.getId());
        return "posts/create";
    }

    @PostMapping("/create")
    public String createPost(@RequestParam("file") MultipartFile file,
                             @RequestParam("caption") String caption,
                             @RequestParam("userId") Long userId,
                             Model model) {
        try {
            postService.createPost(file, caption, userId);
            return "redirect:/posts";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "posts/create";
        }
    }

    @PostMapping("/{postId}/delete")
    public String deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return "redirect:/posts";
    }
}