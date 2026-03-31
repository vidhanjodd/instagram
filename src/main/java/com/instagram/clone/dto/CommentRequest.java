package com.instagram.clone.dto;

import lombok.Data;

@Data
public class CommentRequest {
    private Long postId;
    private Long userId;
    private String content;
    private Long parentId;
}
