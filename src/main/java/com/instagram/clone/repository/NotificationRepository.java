package com.instagram.clone.repository;

import com.instagram.clone.entity.Notification;
import com.instagram.clone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {

    List<Notification> findByRecipientOrderByCreatedAtDesc(User recipient);

    long countByRecipientAndIsReadFalse(User recipient);

    @Modifying
    @Transactional
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipient = :recipient AND n.isRead = false")
    void markAllAsRead(@Param("recipient") User recipient);

    boolean existsByActorAndRecipientAndTypeAndPostId(
            User actor, User recipient,
            Notification.NotificationType type, Long postId);

    boolean existsByActorAndRecipientAndTypeAndReelId(
            User actor, User recipient,
            Notification.NotificationType type, Long reelId);

    boolean existsByActorAndRecipientAndType(
            User actor, User recipient,
            Notification.NotificationType type);
    @Modifying
    @Transactional
    @Query("DELETE FROM Notification n WHERE n.followId = :followId")
    void deleteByFollowId(@Param("followId") Long followId);

    Optional<Notification> findByFollowId(Long followId);
}