package com.instagram.clone.controller;

import com.instagram.clone.entity.Post;
import com.instagram.clone.entity.User;
import com.instagram.clone.repository.UserRepository;
import com.instagram.clone.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Arrays;
import java.util.List;

@Controller
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    private User getCurrentUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Logged in user not found in database"));
    }

    @GetMapping
    public String showFeed(Model model, Principal principal) {
        List<Post> posts = postService.getAllPosts();

        model.addAttribute("posts", posts);
        model.addAttribute("currentUser", getCurrentUser(principal));

        return "homepage/feed";
    }

    @GetMapping("/new")
    public String showCreatePostForm(Model model, Principal principal) {
        model.addAttribute("currentUserId", getCurrentUser(principal).getId());
        return "homepage/create";
    }

    @PostMapping("/create")
    public String createPost(
            @RequestParam("files") MultipartFile[] files,
            @RequestParam(value = "caption", required = false) String caption,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        if (files == null || files.length==0 || files[0].isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "At least one media file is required.");
            return "redirect:/posts/new";
        }

        if (files.length  > 10) {
            redirectAttributes.addFlashAttribute("error", "You can only upload a maximum of 10 media files.");
            return "redirect:/posts/new";
        }

        try {
            User user = getCurrentUser(principal);
            postService.createPost(Arrays.asList(files), caption, user);
            redirectAttributes.addFlashAttribute("success", "Post created successfully!");
            return "redirect:/posts";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating post: " + e.getMessage());
            return "redirect:/posts/new";
        }
    }

    @GetMapping("/user/{userId}")
    public String showUserProfile(@PathVariable Long userId, Model model, Principal principal) {
        List<Post> posts = postService.getPostsByUserId(userId);
        model.addAttribute("posts", posts);

        model.addAttribute("currentUser", getCurrentUser(principal));

        return "profilepage/profile";
    }

    @PostMapping("/{postId}/delete")
    public String deletePost(@PathVariable Long postId, RedirectAttributes redirectAttributes) {
        try {
            postService.deletePost(postId);
            redirectAttributes.addFlashAttribute("success", "Post deleted successfully.");
        } catch (RuntimeException e) {
            redirectAttributes.addFlashAttribute("error", "Error deleting post: " + e.getMessage());
        }
        return "redirect:/posts";
    }
}