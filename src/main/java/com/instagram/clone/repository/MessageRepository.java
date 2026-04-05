package com.instagram.clone.repository;

import com.instagram.clone.entity.Message;
import com.instagram.clone.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MessageRepository extends JpaRepository<Message, Long> {

    // Get full conversation between two users, ordered by time
    @Query("SELECT m FROM Message m WHERE " +
            "(m.sender = :user1 AND m.receiver = :user2) OR " +
            "(m.sender = :user2 AND m.receiver = :user1) " +
            "ORDER BY m.createdAt ASC")
    List<Message> findConversation(@Param("user1") User user1, @Param("user2") User user2);

    // Get latest message with each unique conversation partner
    @Query("SELECT m FROM Message m WHERE m.id IN (" +
            "SELECT MAX(m2.id) FROM Message m2 WHERE m2.sender = :user OR m2.receiver = :user " +
            "GROUP BY CASE WHEN m2.sender = :user THEN m2.receiver.id ELSE m2.sender.id END" +
            ") ORDER BY m.createdAt DESC")
    List<Message> findLatestMessagePerConversation(@Param("user") User user);
}