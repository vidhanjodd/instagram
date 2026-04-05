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

import java.security.Principal;
import java.util.List;

@Controller
@RequiredArgsConstructor
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserRepository userRepository;
    private final SimpMessagingTemplate messagingTemplate;

    // Inbox
    @GetMapping
    public String inbox(@AuthenticationPrincipal UserDetails userDetails, Model model) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        List<MessageResponse> inbox = messageService.getInbox(currentUser);
        model.addAttribute("inbox", inbox);
        model.addAttribute("currentUser", currentUser);
        return "homepage/chat";
    }

    // Conversation
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

    // WebSocket
    @MessageMapping("/chat.send")
    public void handleMessage(@Payload MessageRequest request, Principal principal) {

        User sender = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        ChatMessage chatMessage = messageService.sendMessage(sender, request);

        messagingTemplate.convertAndSend(
                "/topic/messages/" + request.getReceiverId(), chatMessage);

        messagingTemplate.convertAndSend(
                "/topic/messages/" + sender.getId(), chatMessage);
    }
}