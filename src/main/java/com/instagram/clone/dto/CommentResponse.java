package com.instagram.clone.dto;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
public class CommentResponse {
    private Long id;
    private String username;
    private Long userId;
    private String content;
    private LocalDateTime createdAt;
    private int replyCount;
    private List<CommentResponse> replies;
}
