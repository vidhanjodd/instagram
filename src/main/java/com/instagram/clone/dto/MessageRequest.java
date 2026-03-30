package com.instagram.clone.dto;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class MessageRequest {
    private Long senderId;
    private Long receiverId;
    private String content;
}