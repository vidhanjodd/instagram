package com.instagram.clone.controller;

import com.instagram.clone.dto.CommentRequest;
import com.instagram.clone.dto.CommentResponse;
import com.instagram.clone.entity.Comment;
import com.instagram.clone.entity.Reel;
import com.instagram.clone.entity.User;
import com.instagram.clone.repository.UserRepository;
import com.instagram.clone.service.CommentService;
import com.instagram.clone.service.ReelLikeService;
import com.instagram.clone.service.ReelService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;

@Controller
@RequestMapping("/reels")
@RequiredArgsConstructor
public class ReelController {

    private final ReelService reelService;
    private final ReelLikeService reelLikeService;
    private final UserRepository userRepository;
    private final CommentService commentService;

    private User getCurrentUser(Principal principal) {
        return userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new RuntimeException("Logged in user not found"));
    }


    @GetMapping("/create")
    public String showCreateForm(Model model, Principal principal) {
        model.addAttribute("currentUserId", getCurrentUser(principal).getId());
        return "homepage/reel-create";
    }

    @PostMapping("/create")
    public String createReel(
            @RequestParam("file") MultipartFile file,
            @RequestParam(value = "caption", required = false) String caption,
            Principal principal,
            RedirectAttributes redirectAttributes) {

        try {
            if (file == null || file.isEmpty()) {
                redirectAttributes.addFlashAttribute("error", "A video file is required.");
                return "redirect:/reels/create";
            }

            if (file.getContentType() == null || !file.getContentType().startsWith("video/")) {
                redirectAttributes.addFlashAttribute("error", "Please upload a valid video file.");
                return "redirect:/reels/create";
            }

            if (file.getSize() > 50 * 1024 * 1024) {
                redirectAttributes.addFlashAttribute("error", "Video file is too large. Max 50MB.");
                return "redirect:/reels/create";
            }

            User user = getCurrentUser(principal);
            Reel reel = reelService.createReel(file, caption, user);

            redirectAttributes.addFlashAttribute("success", "Reel uploaded successfully!");
            return "redirect:/reels/" + reel.getId();

        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", "Error creating reel: " + e.getMessage());
            return "redirect:/reels/create";
        }
    }


    @GetMapping
    public String showReelFeed(Model model, Principal principal) {
        List<Reel> reels = reelService.getAllReels();
        model.addAttribute("reels", reels);

        if (principal != null) {
            model.addAttribute("currentUser", getCurrentUser(principal));
        }

        return "homepage/reel-list";
    }

    @GetMapping("/{reelId}/comments")
    @ResponseBody
    public List<CommentResponse> getComments(@PathVariable Long reelId) {
        return commentService.getCommentsByReelId(reelId);
    }

    @GetMapping("/{reelId}")
    public String viewReel(@PathVariable Long reelId, Model model, Principal principal) {
        Reel reel = reelService.getReelById(reelId);
        model.addAttribute("reel", reel);

        if (principal != null) {
            model.addAttribute("currentUser", getCurrentUser(principal));
        }

        return "homepage/reel-detail";
    }


    @PostMapping("/{reelId}/view")
    @ResponseBody
    public long incrementView(@PathVariable Long reelId) {
        return reelService.incrementViewCount(reelId);
    }


    @PostMapping("/{reelId}/like")
    @ResponseBody
    public long likeReel(@PathVariable Long reelId, Principal principal) {
        User user = getCurrentUser(principal);
        return reelLikeService.likeReel(reelId, user);
    }


    @PostMapping("/{reelId}/unlike")
    @ResponseBody
    public long unlikeReel(@PathVariable Long reelId, Principal principal) {
        User user = getCurrentUser(principal);
        return reelLikeService.unlikeReel(reelId, user);
    }


    @GetMapping("/{reelId}/liked")
    @ResponseBody
    public boolean hasLiked(@PathVariable Long reelId, Principal principal) {
        User user = getCurrentUser(principal);
        return reelLikeService.hasUserLiked(reelId, user);
    }



    @DeleteMapping("/{reelId}")
    public String deleteReel(@PathVariable Long reelId,
                             Principal principal,
                             RedirectAttributes redirectAttributes) {

        try {
            User user = getCurrentUser(principal);
            reelService.deleteReel(reelId, user);

            redirectAttributes.addFlashAttribute("success", "Reel deleted successfully");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("error", e.getMessage());
        }

        return "redirect:/reels";
    }

    @PostMapping(value = "/{reelId}/comments", consumes = "application/json", produces = "application/json")
    @ResponseBody
    public CommentResponse addComment(
            @PathVariable Long reelId,
            @RequestBody CommentRequest request,
            Principal principal
    ) {
        System.out.println(">>> Received request: " + request);
        try {
            User user = getCurrentUser(principal);
            request.setReelId(reelId);
            request.setUserId(user.getId());
            return commentService.createComment(request);
        } catch (Exception e) {
            e.printStackTrace();
            throw e;
        }
    }




}