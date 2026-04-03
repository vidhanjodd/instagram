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
import java.util.List;

@Controller
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    // We need the UserRepository to fetch the full user details from the logged-in username
    @Autowired
    private UserRepository userRepository;

    // Helper method to fetch the logged-in user
    private User getCurrentUser(Principal principal) {
        // Assuming your repository has findByUsername. If you log in with email, change this to findByEmail
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Logged in user not found in database"));
    }

    // 1. Show the main feed
    @GetMapping
    public String showFeed(Model model, Principal principal) {
        List<Post> posts = postService.getAllPosts();

        // Pass the posts and the missing currentUser to Thymeleaf
        model.addAttribute("posts", posts);
        model.addAttribute("currentUser", getCurrentUser(principal));

        return "homepage/feed";
    }

    // 2. Show the page/form to create a new post
    @GetMapping("/new")
    public String showCreatePostForm(Model model, Principal principal) {
        // Your create.html expects ${currentUserId} for the hidden input field
        model.addAttribute("currentUserId", getCurrentUser(principal).getId());
        return "homepage/create";
    }

    // 3. Handle the form submission from the create post page
    @PostMapping("/create")
    public String createPost(
            @RequestParam("files") List<MultipartFile> files,
            @RequestParam(value = "caption", required = false) String caption,
            @RequestParam("userId") Long userId,
            RedirectAttributes redirectAttributes) {

        if (files == null || files.isEmpty() || files.get(0).isEmpty()) {
            redirectAttributes.addFlashAttribute("error", "At least one media file is required.");
            return "redirect:/posts/new";
        }

        if (files.size() > 10) {
            redirectAttributes.addFlashAttribute("error", "You can only upload a maximum of 10 media files.");
            return "redirect:/posts/new";
        }

        try {
            postService.createPost(files, caption, userId);
            redirectAttributes.addFlashAttribute("success", "Post created successfully!");
            return "redirect:/posts";
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating post: " + e.getMessage());
            return "redirect:/posts/new";
        }
    }

    // 4. Show a specific user's profile with their posts
    @GetMapping("/user/{userId}")
    public String showUserProfile(@PathVariable Long userId, Model model, Principal principal) {
        List<Post> posts = postService.getPostsByUserId(userId);
        model.addAttribute("posts", posts);

        // Passing currentUser here as well, in case your profile.html needs it for follow/unfollow logic
        model.addAttribute("currentUser", getCurrentUser(principal));

        return "profilepage/profile";
    }

    // 5. Delete a post
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