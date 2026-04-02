package com.instagram.clone.controller;

import com.instagram.clone.dto.CommentResponse;
import com.instagram.clone.entity.Post;
import com.instagram.clone.entity.User;
import com.instagram.clone.repository.UserRepository;
import com.instagram.clone.service.CommentService;
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
    private final UserRepository userRepository;
    private final CommentService commentService;

    @GetMapping
    public String getAllPosts(Model model, Authentication authentication) {
        List<Post> posts = postService.getAllPosts();
        model.addAttribute("posts", posts);

        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("currentUser", user);

        return "homepage/feed";
    }

    @GetMapping("/create")
    public String showCreateForm(Model model, Authentication authentication) {
        User user = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));
        model.addAttribute("currentUserId", user.getId());
        return "homepage/create";
    }

    @PostMapping("/{postId}/delete")
    public String deletePost(@PathVariable Long postId, Authentication authentication) {
        Post post = postService.getPostById(postId);

        if (!post.getUser().getUsername().equals(authentication.getName())) {
            return "redirect:/posts?error=unauthorized";
        }

        postService.deletePost(postId);
        return "redirect:/posts";
    }

    @PostMapping("/create")
    public String createPost(@RequestParam("file") MultipartFile file,
                             @RequestParam("caption") String caption,
                             Authentication authentication,
                             Model model) {
        try {
            User user = userRepository.findByUsername(authentication.getName())
                    .orElseThrow(() -> new RuntimeException("User not found"));

            postService.createPost(file, caption, user.getId());
            return "redirect:/posts";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "homepage/create";
        }
    }

    @GetMapping("/{postId}")
    public String getPostDetails(@PathVariable Long postId,
                                 Model model,
                                 Authentication authentication) {

        Post post = postService.getPostById(postId);

        User currentUser = userRepository.findByUsername(authentication.getName())
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CommentResponse> comments = commentService.getCommentsForPost(postId);

        model.addAttribute("post", post);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("comments", comments);

        return "homepage/post-details";
    }
}