package com.instagram.clone.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "comments", indexes = {
        @Index(name = "idx_comment_reel", columnList = "reel_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Post relationship (leave unchanged)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Post post;

    // Reel relationship (important for reels)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reel_id", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private Reel reel;

    // User who made the comment
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    @OnDelete(action = OnDeleteAction.CASCADE)
    private User user;

    // Comment text (fixed: added constraints)
    @Column(nullable = false, length = 1000)
    private String content;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id", nullable = true)
    @OnDelete(action = OnDeleteAction.CASCADE) // ADD THIS LINE
    private Comment parent;

    // Replies (fixed: initialized list)
    @JsonIgnore
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Comment> replies = new ArrayList<>();

    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        this.createdAt = LocalDateTime.now();
        validateCommentAssociation();
    }

    @PreUpdate
    protected void onUpdate() {
        validateCommentAssociation();
    }

    private void validateCommentAssociation() {
        if (this.parent != null) return;

        boolean hasPost = this.post != null;
        boolean hasReel = this.reel != null;

        if ((hasPost && hasReel) || (!hasPost && !hasReel)) {
            throw new IllegalArgumentException(
                    "Comment must belong to either a Post or a Reel, not both or neither"
            );
        }
    }

    public boolean isPostComment() {
        return this.post != null;
    }

    public boolean isReelComment() {
        return this.reel != null;
    }
}