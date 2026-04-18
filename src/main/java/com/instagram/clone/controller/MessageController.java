package com.instagram.clone.controller;

import com.instagram.clone.dto.ChatMessage;
import com.instagram.clone.dto.MessageRequest;
import com.instagram.clone.dto.MessageResponse;
import com.instagram.clone.entity.User;
import com.instagram.clone.repository.MessageRepository;
import com.instagram.clone.repository.UserRepository;
import com.instagram.clone.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
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
import java.util.Map;

@Controller
@RequiredArgsConstructor
@RequestMapping("/messages")
public class MessageController {

    private final MessageService messageService;
    private final UserRepository userRepository;
    private final MessageRepository messageRepository;
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

    @GetMapping("/{username}")
    public String conversation(@PathVariable String username,
                               @AuthenticationPrincipal UserDetails userDetails,
                               Model model) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));
        User otherUser = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<MessageResponse> messages = messageService.getConversation(currentUser, otherUser.getId());
        List<MessageResponse> inbox    = messageService.getInbox(currentUser);

        model.addAttribute("messages",    messages);
        model.addAttribute("inbox",       inbox);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("otherUser",   otherUser);
        return "homepage/chat";
    }

    @MessageMapping("/chat.send")
    public void handleMessage(@Payload MessageRequest request, Principal principal) {
        if (principal == null) throw new IllegalStateException("User not authenticated");

        User sender = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        ChatMessage chatMessage = messageService.sendMessage(sender, request);

        messagingTemplate.convertAndSend("/topic/messages/" + request.getReceiverId(), chatMessage);
        messagingTemplate.convertAndSend("/topic/messages/" + sender.getId(), chatMessage);
    }

    @MessageMapping("/chat.seen")
    public void handleSeen(@Payload Map<String, Long> payload, Principal principal) {
        Long messageId = payload.get("messageId");
        if (messageId == null) return;

        User viewer = userRepository.findByUsername(principal.getName())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        try {
            Long deletedId = messageService.markSeenAndDeleteIfVanish(messageId, viewer);
            if (deletedId != null) {
                messagingTemplate.convertAndSend("/topic/delete/" + viewer.getId(), deletedId);
                messagingTemplate.convertAndSend("/topic/delete/" + payload.get("senderId"), deletedId);
            }
        } catch (Exception e) {
        }
    }

    @DeleteMapping("/{msgId}")
    @ResponseBody
    public ResponseEntity<Void> deleteMessage(@PathVariable Long msgId,
                                              @AuthenticationPrincipal UserDetails userDetails) {
        User currentUser = userRepository.findByUsername(userDetails.getUsername())
                .orElseThrow(() -> new IllegalStateException("Authenticated user not found"));

        messageRepository.findById(msgId).ifPresent(msg -> {
            if (msg.getSender().getId().equals(currentUser.getId())) {
                messageRepository.deleteById(msgId);
                messagingTemplate.convertAndSend("/topic/delete/" + currentUser.getId(), msgId);
                messagingTemplate.convertAndSend("/topic/delete/" + msg.getReceiver().getId(), msgId);
            }
        });

        return ResponseEntity.ok().build();
    }
}