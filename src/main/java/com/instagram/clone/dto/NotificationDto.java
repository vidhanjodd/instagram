package com.instagram.clone.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.instagram.clone.entity.Notification;
import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class NotificationDto {

    private Long id;

    private Long actorId;
    private String actorUsername;
    private String actorProfilePic;

    private String type;

    private String message;

    private Long postId;
    private Long reelId;
    private Long followId;

    @JsonProperty("isPending")
    private boolean isPending;

    @JsonProperty("isRead")
    private boolean isRead;

    private LocalDateTime createdAt;
    private String timeAgo;

    public static NotificationDto from(Notification n) {
        return NotificationDto.builder()
                .id(n.getId())
                .actorId(n.getActor().getId())
                .actorUsername(n.getActor().getUsername())
                .actorProfilePic(n.getActor().getProfilePicUrl())
                .type(n.getType().name())
                .message(buildMessage(n.getType()))
                .postId(n.getPostId())
                .reelId(n.getReelId())
                .followId(n.getFollowId())
                .isRead(n.isRead())
                .isPending(n.getType() == Notification.NotificationType.FOLLOW_REQUEST)
                .createdAt(n.getCreatedAt())
                .timeAgo(computeTimeAgo(n.getCreatedAt()))
                .build();
    }

    private static String buildMessage(Notification.NotificationType type) {
        return switch (type) {
            case FOLLOW       -> "started following you";
            case FOLLOW_REQUEST -> "requested to follow you";
            case FOLLOW_ACCEPT  -> "accepted your follow request";
            case POST_LIKE    -> "liked your post";
            case REEL_LIKE    -> "liked your reel";
            case POST_COMMENT -> "commented on your post";
            case REEL_COMMENT -> "commented on your reel";
        };
    }

    private static String computeTimeAgo(LocalDateTime createdAt) {
        if (createdAt == null) return "";

        long seconds = java.time.Duration.between(createdAt, LocalDateTime.now()).getSeconds();

        if (seconds < 60)        return seconds + "s";
        if (seconds < 3600)      return (seconds / 60) + "m";
        if (seconds < 86400)     return (seconds / 3600) + "h";
        if (seconds < 2592000)   return (seconds / 86400) + "d";
        return (seconds / 2592000) + "w";
    }
}