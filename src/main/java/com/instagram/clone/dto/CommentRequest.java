package com.instagram.clone.dto;

import lombok.Data;


@Data
public class CommentRequest {
    private Long postId;
    private Long reelId;
    private Long userId;
    private String content;
    private Long parentId;
    private String replyingToUsername;


    public void validate() {
        boolean hasPost = this.postId != null;
        boolean hasReel = this.reelId != null;

        if ((hasPost && hasReel) || (!hasPost && !hasReel)) {
            throw new IllegalArgumentException(
                    "Comment must belong to either a Post or a Reel, not both or neither"
            );
        }
    }


    public boolean isPostComment() {
        return this.postId != null;
    }


    public boolean isReelComment() {
        return this.reelId != null;
    }
}

