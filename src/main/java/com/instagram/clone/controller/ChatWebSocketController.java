package com.instagram.clone.controller;

import com.instagram.clone.dto.MessageRequest;
import com.instagram.clone.dto.MessageResponse;
import com.instagram.clone.service.MessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class ChatWebSocketController {

    private final MessageService messageService;
    private final SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/chat.send")
    public void sendMessage(@Payload MessageRequest request) {

        // Save to DB and get response
        MessageResponse response = messageService.sendMessage(request);

        // Send to receiver's topic
        messagingTemplate.convertAndSend(
                "/topic/messages/" + request.getReceiverId(),
                response
        );

        // Also send back to sender's topic so sender's other tabs get it
        messagingTemplate.convertAndSend(
                "/topic/messages/" + request.getSenderId(),
                response
        );
    }
}