package com.instagram.clone.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "reels", indexes = {
        @Index(name = "idx_reel_user", columnList = "user_id"),
        @Index(name = "idx_reel_created_at", columnList = "createdAt")
})
@Getter
@Setter
@NoArgsConstructor
public class Reel {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String videoUrl;

    @Column(nullable = false)
    private String publicId;

    @Column(length = 2200)
    private String caption;

    @Column(nullable = false)
    private Long viewCount = 0L;

    @Column(nullable = false)
    private Long likeCount = 0L;

    @Column(nullable = false)
    private Long commentCount = 0L;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    @JsonIgnore
    @OneToMany(mappedBy = "reel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> comments = new ArrayList<>();

    @JsonIgnore
    @OneToMany(mappedBy = "reel", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<ReelLike> likes = new ArrayList<>();

    public void addComment(Comment comment) {
        comments.add(comment);
        comment.setReel(this);
        commentCount++;
    }

    public void removeComment(Comment comment) {
        if (comments.remove(comment)) {
            commentCount--;
        }
    }

    public void addLike(ReelLike like) {
        likes.add(like);
        like.setReel(this);
        likeCount++;
    }

    public void removeLike(ReelLike like) {
        if (likes.remove(like)) {
            likeCount--;
        }
    }

    public void incrementView() {
        this.viewCount++;
    }
}