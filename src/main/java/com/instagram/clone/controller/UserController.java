package com.instagram.clone.controller;

import com.instagram.clone.dto.UserRegisterRequest;
import com.instagram.clone.entity.User;
import com.instagram.clone.service.PostService;
import com.instagram.clone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PostService postService;

    // Show registration form
    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserRegisterRequest());
        return "profilepage/register";
    }

    // Handle registration submission
    @PostMapping("/register")
    public String register(@ModelAttribute UserRegisterRequest request, Model model) {
        try {
            userService.register(request);
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "users/register";
        }
    }

    // View user profile
    @GetMapping("/{id}/profile")
    public String viewProfile(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        model.addAttribute("posts", postService.getPostsByUserId(id));
        return "profilepage/profile";
    }

    // Show edit bio form
    @GetMapping("/{id}/edit")
    public String showEditBio(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "profilepage/edit-bio";
    }

    // Handle bio update
    @PostMapping("/{id}/bio")
    public String updateBio(@PathVariable Long id,
                            @RequestParam String bio) {
        userService.updateBio(id, bio);
        return "redirect:/users/" + id + "/profile";
    }

    // Delete user
    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return "redirect:/login";
    }
}