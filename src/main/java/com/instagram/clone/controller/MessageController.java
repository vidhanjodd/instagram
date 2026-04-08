package com.instagram.clone.controller;

import com.instagram.clone.dto.ChatMessage;
import com.instagram.clone.dto.MessageRequest;
import com.instagram.clone.dto.MessageResponse;
import com.instagram.clone.entity.User;
import com.instagram.clone.repository.UserRepository;
import com.instagram.clone.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import org.springframework.http.ResponseEntity;
import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    @GetMapping
    public String inbox(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        List<MessageResponse> inbox = messageService.getInbox(currentUser);
        model.addAttribute("inbox", inbox);
        model.addAttribute("currentUser", currentUser);
        return "homepage/chat";
    }

    @GetMapping("/{userId}")
    public String conversation(@PathVariable Long userId,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {

        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        User otherUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Receiver user not found with id: " + userId));

        List<MessageResponse> messages = messageService.getConversation(currentUser, userId);
        List<MessageResponse> inbox = messageService.getInbox(currentUser);

        model.addAttribute("messages", messages);
        model.addAttribute("inbox", inbox);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("otherUser", otherUser);
        return "homepage/chat";
    }

    @DeleteMapping("/{messageId}")
    @ResponseBody
    public ResponseEntity<?> deleteMessage(@PathVariable Long messageId,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        User requester = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        try {
            ChatMessage deleted = messageService.deleteMessage(messageId, requester);

            messagingTemplate.convertAndSend(
                    "/topic/delete/" + deleted.getReceiverId(), deleted.getId());
            messagingTemplate.convertAndSend(
                    "/topic/delete/" + deleted.getSenderId(), deleted.getId());
            
            long conversationId = Math.min(deleted.getSenderId(), deleted.getReceiverId()) * 1000000
                                + Math.max(deleted.getSenderId(), deleted.getReceiverId());
            messagingTemplate.convertAndSend(
                    "/topic/conversation/" + conversationId + "/delete", deleted.getId());

            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }



}