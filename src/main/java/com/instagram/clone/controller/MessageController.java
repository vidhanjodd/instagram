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

    @MessageMapping("/chat.send")
    public void handleMessage(@Payload MessageRequest request, Principal principal) {

        // Debug logging for multi-machine chat
        System.out.println("📨 [CHAT] Message received from principal: " + principal.getName());
        System.out.println("📨 [CHAT] Receiver ID: " + request.getReceiverId());
        System.out.println("📨 [CHAT] Message content: " + request.getContent());

        User sender = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        System.out.println("📨 [CHAT] Sender ID: " + sender.getId());

        ChatMessage chatMessage = messageService.sendMessage(sender, request);

        // Send to receiver's personal topic
        messagingTemplate.convertAndSend(
                "/topic/messages/" + request.getReceiverId(), chatMessage);

        // Send to sender's personal topic for acknowledgment
        messagingTemplate.convertAndSend(
                "/topic/messages/" + sender.getId(), chatMessage);
        
        // Also send to a shared conversation topic so both can receive in real-time
        long conversationId = Math.min(sender.getId(), request.getReceiverId()) * 1000000 
                            + Math.max(sender.getId(), request.getReceiverId());
        messagingTemplate.convertAndSend(
                "/topic/conversation/" + conversationId, chatMessage);
    }

    @DeleteMapping("/{messageId}")
    @ResponseBody
    public ResponseEntity<?> deleteMessage(@PathVariable Long messageId,
                                           @AuthenticationPrincipal UserDetails userDetails) {
        User requester = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        try {
            ChatMessage deleted = messageService.deleteMessage(messageId, requester);

            // Broadcast deletion to both users' personal topics
            messagingTemplate.convertAndSend(
                    "/topic/delete/" + deleted.getReceiverId(), deleted.getId());
            messagingTemplate.convertAndSend(
                    "/topic/delete/" + deleted.getSenderId(), deleted.getId());
            
            // Also broadcast to shared conversation topic for cross-machine sync
            long conversationId = Math.min(deleted.getSenderId(), deleted.getReceiverId()) * 1000000 
                                + Math.max(deleted.getSenderId(), deleted.getReceiverId());
            messagingTemplate.convertAndSend(
                    "/topic/conversation/" + conversationId + "/delete", deleted.getId());

            return ResponseEntity.ok().build();
        } catch (RuntimeException e) {
            return ResponseEntity.status(403).body(e.getMessage());
        }
    }

    @MessageMapping("/chat.seen")
    public void markSeen(@Payload java.util.Map<String, Long> payload, Principal principal) {
        User viewer = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        Long senderId = payload.get("senderId");
        if (senderId == null) return;

        List<Long> deletedIds = messageService.markVanishMessagesSeen(viewer, senderId);

        for (Long msgId : deletedIds) {
            messagingTemplate.convertAndSend("/topic/delete/" + viewer.getId(), msgId);
            messagingTemplate.convertAndSend("/topic/delete/" + senderId, msgId);
        }
    }
}