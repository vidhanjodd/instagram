package com.instagram.clone.controller;

import com.instagram.clone.dto.UserRegisterRequest;
import com.instagram.clone.entity.Follow;
import com.instagram.clone.entity.User;
import com.instagram.clone.repository.FollowRepository;
import com.instagram.clone.repository.UserRepository;
import com.instagram.clone.service.FollowService;
import com.instagram.clone.service.PostService;
import com.instagram.clone.service.ReelService;
import com.instagram.clone.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Controller
@RequestMapping("/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final PostService postService;
    private final ReelService reelService;
    private final FollowService followService;
    private final UserRepository userRepository;
    private final FollowRepository followRepository;


    @GetMapping("/register")
    public String showRegisterForm(Model model) {
        model.addAttribute("user", new UserRegisterRequest());
        return "profilepage/register";
    }

    @PostMapping("/register")
    public String register(@ModelAttribute UserRegisterRequest request, Model model) {
        try {
            userService.register(request);
            return "redirect:/login";
        } catch (Exception e) {
            model.addAttribute("error", e.getMessage());
            return "profilepage/register";
        }
    }

    @GetMapping("/{username}/profile")
    public String viewProfile(@PathVariable String username, Model model, Authentication authentication,
                              HttpServletRequest request, HttpServletResponse response) {

        Optional<User> loggedInOpt = userRepository.findByUsername(authentication.getName());
        if (loggedInOpt.isEmpty()) {
            new SecurityContextLogoutHandler().logout(request, response,
                    SecurityContextHolder.getContext().getAuthentication());
            return "redirect:/login";
        }

        Optional<User> profileUserOpt = userRepository.findByUsername(username);
        if (profileUserOpt.isEmpty()) {
            return "redirect:/posts";
        }

        User profileUser = profileUserOpt.get();
        User loggedInUser = loggedInOpt.get();

        boolean isOwnProfile   = loggedInUser.getId().equals(profileUser.getId());
        boolean isFollowing    = followService.isFollowing(loggedInUser, profileUser);
        boolean hasPendingRequest = !isOwnProfile && !isFollowing
                && followService.hasPendingRequest(loggedInUser, profileUser);

        long followersCount = followService.getFollowersCount(profileUser);
        long followingCount = followService.getFollowingCount(profileUser);



        model.addAttribute("user", profileUser);
        model.addAttribute("currentUser", loggedInUser);
        model.addAttribute("followersCount", followersCount);
        model.addAttribute("followingCount", followingCount);
        model.addAttribute("isFollowing", isFollowing);
        model.addAttribute("hasPendingRequest", hasPendingRequest);
        model.addAttribute("isPrivateProfile", profileUser.isPrivate());

        boolean canSeePosts = isOwnProfile || isFollowing || !profileUser.isPrivate();
        if (canSeePosts) {
            model.addAttribute("posts", postService.getPostsByUserId(profileUser.getId()));
            model.addAttribute("reels", reelService.getReelsByUserId(profileUser.getId()));
        } else {
            model.addAttribute("posts", List.of());
            model.addAttribute("reels", List.of());
        }

        return "profilepage/profile";
    }

    @GetMapping("/{username}/edit")
    public String showEditBio(@PathVariable String username, Model model) {
        User user = userService.getUserByUsername(username);
        model.addAttribute("user", user);
        return "profilepage/edit-bio";
    }

    @PostMapping("/{username}/bio")
    public String updateBio(@PathVariable String username,
                            @RequestParam String bio,
                            @RequestParam(required = false) String websiteUrl,
                            @RequestParam(defaultValue = "false") boolean isPrivate, // NEW
                            Authentication authentication,
                            org.springframework.ui.Model model) {

        User userToUpdate = userService.getUserByUsername(username);
        if (!userToUpdate.getUsername().equals(authentication.getName())) {
            return "redirect:/users/" + username + "/profile?error=unauthorized";
        }

        try {
            userService.updateBio(userToUpdate.getId(), bio, websiteUrl);
            userToUpdate = userService.getUserByUsername(username);
            userToUpdate.setPrivate(isPrivate);
            userRepository.save(userToUpdate);
        } catch (RuntimeException e) {
            User user = userService.getUserByUsername(username);
            model.addAttribute("user", user);
            model.addAttribute("urlError", e.getMessage());
            return "profilepage/edit-bio";
        }

        return "redirect:/users/" + username + "/profile";
    }

    @PostMapping("/{username}/delete")
    public String deleteUser(@PathVariable String username, Authentication authentication) {
        User user = userService.getUserByUsername(username);

        if (!user.getUsername().equals(authentication.getName())) {
            return "redirect:/login?error=unauthorized";
        }

        userService.deleteUser(user.getId());
        return "redirect:/login";
    }
    @PostMapping("/{username}/profile-picture")
    public String uploadProfilePicture(@PathVariable String username,
                                       @RequestParam("profilePic") MultipartFile file,
                                       Authentication authentication,
                                       Model model) {

        User userToUpdate = userService.getUserByUsername(username);
        if (!userToUpdate.getUsername().equals(authentication.getName())) {
            return "redirect:/users/" + username + "/profile?error=unauthorized";
        }

        if (file == null || file.isEmpty()) {
            return "redirect:/users/" + username + "/edit";
        }

        try {
            userService.uploadProfilePicture(userToUpdate.getId(), file);
            return "redirect:/users/" + username + "/edit?success=true";
        } catch (Exception e) {
            User user = userService.getUserByUsername(username);
            model.addAttribute("user", user);
            model.addAttribute("picError", e.getMessage());
            return "profilepage/edit-bio";
        }
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
    @GetMapping("/{username}/followers")
    public String viewFollowers(@PathVariable String username, Model model, Authentication authentication) {
        User profileUser = userService.getUserByUsername(username);
        User loggedInUser = userService.getUserByUsername(authentication.getName());
        if (profileUser.isPrivate() && !loggedInUser.getId().equals(profileUser.getId())) {
            boolean isFollowing = followRepository.findByFollowerAndFollowing(loggedInUser, profileUser)
                    .map(f -> f.isAccepted())
                    .orElse(false);

            if (!isFollowing) {
                return "redirect:/users/" + username + "/profile";
            }
        }

        List<Follow> followers = followRepository.findAllByFollowing(profileUser);

        model.addAttribute("user", profileUser);
        model.addAttribute("currentUser", loggedInUser);
        model.addAttribute("followers", followers);
        model.addAttribute("isOwnProfile", loggedInUser.getId().equals(profileUser.getId()));

        return "profilepage/followers";
    }

    @GetMapping("/{username}/following")
    public String viewFollowing(@PathVariable String username, Model model, Authentication authentication) {
        User profileUser = userService.getUserByUsername(username);
        User loggedInUser = userService.getUserByUsername(authentication.getName());
        if (profileUser.isPrivate() && !loggedInUser.getId().equals(profileUser.getId())) {
            boolean isFollowing = followRepository.findByFollowerAndFollowing(loggedInUser, profileUser)
                    .map(f -> f.isAccepted())
                    .orElse(false);

            if (!isFollowing) {
                return "redirect:/users/" + username + "/profile";
            }
        }

        List<Follow> following = followRepository.findAllByFollower(profileUser);

        model.addAttribute("user", profileUser);
        model.addAttribute("currentUser", loggedInUser);
        model.addAttribute("following", following);
        model.addAttribute("isOwnProfile", loggedInUser.getId().equals(profileUser.getId()));

        return "profilepage/following";
    }
}