package com.instagram.clone.controller;

import com.instagram.clone.dto.UserRegisterRequest;
import com.instagram.clone.entity.Follow;
import com.instagram.clone.entity.User;
import com.instagram.clone.repository.FollowRepository;
import com.instagram.clone.repository.UserRepository;
import com.instagram.clone.service.FollowService;
import com.instagram.clone.service.PostService;
import com.instagram.clone.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PostService postService;
    private final FollowService followService;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;


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
    // View user profile
    @GetMapping("/{id}/profile")
    public String viewProfile(@PathVariable Long id, Model model, Authentication authentication) {
        User profileUser = userService.getUserById(id);
        User loggedInUser = userService.getUserByUsername(authentication.getName());

        long followersCount = followService.getFollowersCount(profileUser);
        long followingCount = followService.getFollowingCount(profileUser);
        boolean isFollowing = followService.isFollowing(loggedInUser, profileUser);

        model.addAttribute("user", profileUser);
        model.addAttribute("currentUser", loggedInUser);
        model.addAttribute("posts", postService.getPostsByUserId(id));
        model.addAttribute("followersCount", followersCount);
        model.addAttribute("followingCount", followingCount);
        model.addAttribute("isFollowing", isFollowing);

        return "profilepage/profile";
    }

    @GetMapping("/{id}/edit")
    public String showEditBio(@PathVariable Long id, Model model) {
        User user = userService.getUserById(id);
        model.addAttribute("user", user);
        return "profilepage/edit-bio";
    }

    @PostMapping("/{id}/bio")
    public String updateBio(@PathVariable Long id,
                            @RequestParam String bio,
                            Authentication authentication) {
        User userToUpdate = userService.getUserById(id);

        if (!userToUpdate.getUsername().equals(authentication.getName())) {
            return "redirect:/users/" + id + "/profile?error=unauthorized";
        }

        userService.updateBio(id, bio);
        return "redirect:/users/" + id + "/profile";
    }

    @PostMapping("/{id}/delete")
    public String deleteUser(@PathVariable Long id, Authentication authentication) {
        User user = userService.getUserById(id);

        if (!user.getUsername().equals(authentication.getName())) {
            return "redirect:/login?error=unauthorized";
        }

        userService.deleteUser(id);
        return "redirect:/login";
    }
    @GetMapping("/search")
    @ResponseBody
    public ResponseEntity<List<Map<String, Object>>> searchUsers(
            @RequestParam String query,
            Authentication authentication) {

        if (query == null || query.trim().isEmpty()) {
            return ResponseEntity.ok(List.of());
        }

        List<User> users = userRepository.findByUsernameContainingIgnoreCase(query.trim());
        User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();

        List<Map<String, Object>> result = users.stream()
                .filter(u -> !u.getId().equals(currentUser.getId())) // don't show yourself
                .map(u -> {
                    Map<String, Object> map = new HashMap<>();
                    map.put("id", u.getId());
                    map.put("username", u.getUsername());
                    return map;
                })
                .toList();

        return ResponseEntity.ok(result);
    }
    @GetMapping("/{id}/followers")
    public String viewFollowers(@PathVariable Long id, Model model, Authentication authentication) {
        User profileUser = userService.getUserById(id);
        User loggedInUser = userService.getUserByUsername(authentication.getName());

        // All Follow records where this user is being followed
        List<Follow> followers = followRepository.findAllByFollowing(profileUser);

        model.addAttribute("user", profileUser);
        model.addAttribute("currentUser", loggedInUser);
        model.addAttribute("followers", followers);
        model.addAttribute("isOwnProfile", loggedInUser.getId().equals(profileUser.getId()));

        return "profilepage/followers";
    }

    @GetMapping("/{id}/following")
    public String viewFollowing(@PathVariable Long id, Model model, Authentication authentication) {
        User profileUser = userService.getUserById(id);
        User loggedInUser = userService.getUserByUsername(authentication.getName());

        // All Follow records where this user is the follower
        List<Follow> following = followRepository.findAllByFollower(profileUser);

        model.addAttribute("user", profileUser);
        model.addAttribute("currentUser", loggedInUser);
        model.addAttribute("following", following);
        model.addAttribute("isOwnProfile", loggedInUser.getId().equals(profileUser.getId()));

        return "profilepage/following";
    }
}