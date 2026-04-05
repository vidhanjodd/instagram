package com.instagram.clone.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessage {
    private Long id;
    private Long senderId;
    private String senderUsername;
    private String senderProfilePic;
    private Long receiverId;
    private String receiverUsername;
    private String content;
    private LocalDateTime createdAt;
}