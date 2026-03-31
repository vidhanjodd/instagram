
package com.instagram.clone.controller;

import com.instagram.clone.entity.Post;
import com.instagram.clone.service.PostService;
import lombok.RequiredArgsConstructor;
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

    // Feed — all posts
    @GetMapping
    public String getAllPosts(Model model) {
        List<Post> posts = postService.getAllPosts();
        model.addAttribute("posts", posts);
        return "posts/feed"; // templates/posts/feed.html
    }

    // Show create post form
    @GetMapping("/create")
    public String showCreateForm(Model model) {
        // Pass logged-in userId via session/security context in real app
        return "posts/create"; // templates/posts/create.html
    }

    // Handle post creation
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

    // Delete post
    @PostMapping("/{postId}/delete")
    public String deletePost(@PathVariable Long postId) {
        postService.deletePost(postId);
        return "redirect:/posts";
    }
}