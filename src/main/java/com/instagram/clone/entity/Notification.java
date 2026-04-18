package com.instagram.clone.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;


@Entity
@Table(name = "notifications")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "recipient_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User recipient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "actor_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User actor;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private NotificationType type;

    @Column(name = "post_id")
    private Long postId;

    @Column(name = "reel_id")
    private Long reelId;

    @Column(name = "follow_id")
    private Long followId;

    @Column(nullable = false)
    @Builder.Default
    private boolean isRead = false;

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
    }

    public enum NotificationType {
        FOLLOW,
        FOLLOW_REQUEST,
        FOLLOW_ACCEPT,
        POST_LIKE,
        REEL_LIKE,
        POST_COMMENT,
        REEL_COMMENT
    }
}