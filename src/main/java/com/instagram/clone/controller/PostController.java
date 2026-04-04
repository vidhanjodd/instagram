package com.instagram.clone.controller;

import com.instagram.clone.dto.CommentRequest;
import com.instagram.clone.dto.CommentResponse;
import com.instagram.clone.entity.Comment;
import com.instagram.clone.entity.Post;
import com.instagram.clone.entity.User;
import com.instagram.clone.repository.UserRepository;
import com.instagram.clone.service.CommentService;
import com.instagram.clone.service.PostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Controller
@RequestMapping("/posts")
public class PostController {

    @Autowired
    private PostService postService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CommentService commentService;

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

    @GetMapping("/{id}")
    public String getPostDetails(@PathVariable Long id, Model model, Principal principal) {
        Post post = postService.getPostById(id);

        model.addAttribute("post", post);

        List<Comment> topLevelComments = post.getComments().stream()
                .filter(comment -> comment.getParent() == null)
                .collect(Collectors.toList());

        model.addAttribute("comments", topLevelComments);

        model.addAttribute("currentUser", getCurrentUser(principal));

        return "homepage/post-details";
    }

    @GetMapping("/{id}/data")
    @ResponseBody
    public ResponseEntity<Map<String, Object>> getPostData(
            @PathVariable Long id,
            Authentication authentication) {

        Post post = postService.getPostById(id);
        User currentUser = userRepository.findByUsername(authentication.getName()).orElseThrow();

        boolean isLiked = post.getLikes() != null &&
                post.getLikes().stream().anyMatch(l -> l.getUser().getId().equals(currentUser.getId()));
        long likeCount = post.getLikes() != null ? post.getLikes().size() : 0;

        List<Map<String, Object>> mediaList = post.getCarouselMedia().stream().map(m -> {
            Map<String, Object> media = new HashMap<>();
            media.put("mediaUrl", m.getMediaUrl());
            media.put("mediaType", m.getMediaType());
            return media;
        }).toList();

        List<CommentResponse> comments = commentService.getCommentsForPost(id);

        // Post owner info
        Map<String, Object> owner = new HashMap<>();
        owner.put("id", post.getUser().getId());
        owner.put("username", post.getUser().getUsername());
        owner.put("profilePicUrl", post.getUser().getProfilePicUrl());

        // Current user info (for delete/reply button logic in JS)
        Map<String, Object> me = new HashMap<>();
        me.put("id", currentUser.getId());
        me.put("username", currentUser.getUsername());

        Map<String, Object> response = new HashMap<>();
        response.put("postId", post.getId());
        response.put("caption", post.getCaption());
        response.put("createdAt", post.getCreatedAt() != null ? post.getCreatedAt().toString() : null);
        response.put("liked", isLiked);
        response.put("likeCount", likeCount);
        response.put("media", mediaList);
        response.put("comments", comments);
        response.put("user", owner);
        response.put("currentUser", me);

        return ResponseEntity.ok(response);
    }

    @PostMapping("/comments/add")
    @ResponseBody
    public ResponseEntity<CommentResponse> addCommentApi(
            @RequestBody CommentRequest request,
            Authentication authentication) {

        // Always resolve userId from the session — never trust the request body
        User user = userRepository.findByUsername(authentication.getName()).orElseThrow();
        request.setUserId(user.getId());

        if (request.getParentId() == null) {
            commentService.createTopLevelComment(request);
            List<CommentResponse> all = commentService.getCommentsForPost(request.getPostId());
            return ResponseEntity.ok(all.get(all.size() - 1));
        } else {
            commentService.addReply(request);
            List<CommentResponse> replies = commentService.getRepliesToComment(request.getParentId());
            return ResponseEntity.ok(replies.get(replies.size() - 1));
        }
    }
}
