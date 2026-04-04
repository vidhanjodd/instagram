package com.instagram.clone.dto;

import lombok.Data;

/**
 * Request DTO for creating/updating comments.
 * A comment must belong to either a Post or a Reel (never both or neither).
 */
@Data
public class CommentRequest {
    private Long postId;        // Optional: ID of post being commented on
    private Long reelId;        // Optional: ID of reel being commented on
    private Long userId;        // Required: ID of user making the comment
    private String content;     // Required: Comment text
    private Long parentId;      // Optional: Parent comment ID for nested replies
    private String replyingToUsername;  // Optional: Username being replied to (@mention)

    /**
     * Validate that exactly one parent (post OR reel) is specified.
     * @throws IllegalArgumentException if validation fails
     */
    public void validate() {
        boolean hasPost = this.postId != null;
        boolean hasReel = this.reelId != null;

        if ((hasPost && hasReel) || (!hasPost && !hasReel)) {
            throw new IllegalArgumentException(
                    "Comment must belong to either a Post or a Reel, not both or neither"
            );
        }
    }

    /**
     * Check if this is a Post comment.
     */
    public boolean isPostComment() {
        return this.postId != null;
    }

    /**
     * Check if this is a Reel comment.
     */
    public boolean isReelComment() {
        return this.reelId != null;
    }
}

