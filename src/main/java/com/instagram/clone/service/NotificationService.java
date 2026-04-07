package com.instagram.clone.service;

import com.instagram.clone.dto.NotificationDto;
import com.instagram.clone.entity.Notification;
import com.instagram.clone.entity.Notification.NotificationType;
import com.instagram.clone.entity.User;
import com.instagram.clone.repository.NotificationRepository;
import com.instagram.clone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;


@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final UserRepository userRepository;

    @Transactional
    public void notifyFollow(Long actorId, Long recipientId) {

        if (actorId.equals(recipientId)) return;

        User actor     = userRepository.findById(actorId).orElseThrow();
        User recipient = userRepository.findById(recipientId).orElseThrow();

        if (notificationRepository.existsByActorAndRecipientAndType(actor, recipient, NotificationType.FOLLOW)) {
            return;
        }

        Notification notification = Notification.builder()
                .actor(actor)
                .recipient(recipient)
                .type(NotificationType.FOLLOW)
                .build();

        notificationRepository.save(notification);
    }


    @Transactional
    public void notifyPostLike(Long actorId, Long recipientId, Long postId) {

        if (actorId.equals(recipientId)) return;

        User actor     = userRepository.findById(actorId).orElseThrow();
        User recipient = userRepository.findById(recipientId).orElseThrow();

        if (notificationRepository.existsByActorAndRecipientAndTypeAndPostId(
                actor, recipient, NotificationType.POST_LIKE, postId)) {
            return;
        }

        notificationRepository.save(
                Notification.builder()
                        .actor(actor)
                        .recipient(recipient)
                        .type(NotificationType.POST_LIKE)
                        .postId(postId)
                        .build()
        );
    }

    @Transactional
    public void notifyReelLike(Long actorId, Long recipientId, Long reelId) {

        if (actorId.equals(recipientId)) return;

        User actor     = userRepository.findById(actorId).orElseThrow();
        User recipient = userRepository.findById(recipientId).orElseThrow();

        if (notificationRepository.existsByActorAndRecipientAndTypeAndReelId(
                actor, recipient, NotificationType.REEL_LIKE, reelId)) {
            return;
        }

        notificationRepository.save(
                Notification.builder()
                        .actor(actor)
                        .recipient(recipient)
                        .type(NotificationType.REEL_LIKE)
                        .reelId(reelId)
                        .build()
        );
    }


    @Transactional
    public void notifyPostComment(Long actorId, Long recipientId, Long postId) {

        if (actorId.equals(recipientId)) return;

        User actor     = userRepository.findById(actorId).orElseThrow();
        User recipient = userRepository.findById(recipientId).orElseThrow();

        notificationRepository.save(
                Notification.builder()
                        .actor(actor)
                        .recipient(recipient)
                        .type(NotificationType.POST_COMMENT)
                        .postId(postId)
                        .build()
        );
    }

    @Transactional
    public void notifyReelComment(Long actorId, Long recipientId, Long reelId) {

        if (actorId.equals(recipientId)) return;

        User actor     = userRepository.findById(actorId).orElseThrow();
        User recipient = userRepository.findById(recipientId).orElseThrow();

        notificationRepository.save(
                Notification.builder()
                        .actor(actor)
                        .recipient(recipient)
                        .type(NotificationType.REEL_COMMENT)
                        .reelId(reelId)
                        .build()
        );
    }
    @Transactional
    public void notifyFollowRequest(Long actorId, Long recipientId, Long followId) {
        if (actorId.equals(recipientId)) return;

        User actor     = userRepository.findById(actorId).orElseThrow();
        User recipient = userRepository.findById(recipientId).orElseThrow();

        if (notificationRepository.findByFollowId(followId).isPresent()) return;

        notificationRepository.save(
                Notification.builder()
                        .actor(actor)
                        .recipient(recipient)
                        .type(NotificationType.FOLLOW_REQUEST)
                        .followId(followId)
                        .build()
        );
    }
    @Transactional
    public void notifyFollowAccept(Long actorId, Long recipientId, Long followId) {
        if (actorId.equals(recipientId)) return;

        User actor     = userRepository.findById(actorId).orElseThrow();
        User recipient = userRepository.findById(recipientId).orElseThrow();

        notificationRepository.deleteByFollowId(followId);

        notificationRepository.save(
                Notification.builder()
                        .actor(actor)
                        .recipient(recipient)
                        .type(NotificationType.FOLLOW_ACCEPT)
                        .followId(followId)
                        .build()
        );
    }
    @Transactional
    public void deleteFollowRequestNotification(Long followId) {
        notificationRepository.deleteByFollowId(followId);
    }
    @Transactional(readOnly = true)
    public List<NotificationDto> getNotifications(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();

        return notificationRepository
                .findByRecipientOrderByCreatedAtDesc(user)
                .stream()
                .map(NotificationDto::from)
                .toList();
    }


    @Transactional(readOnly = true)
    public long getUnreadCount(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        return notificationRepository.countByRecipientAndIsReadFalse(user);
    }


    @Transactional
    public void markAllRead(String username) {
        User user = userRepository.findByUsername(username).orElseThrow();
        notificationRepository.markAllAsRead(user);
    }
}