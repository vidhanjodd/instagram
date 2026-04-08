package com.instagram.clone.service;

import com.instagram.clone.dto.ChatMessage;
import com.instagram.clone.dto.MessageRequest;
import com.instagram.clone.dto.MessageResponse;
import com.instagram.clone.entity.Message;
import com.instagram.clone.entity.User;
import com.instagram.clone.repository.MessageRepository;
import com.instagram.clone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    /** Save message to DB and return ChatMessage DTO for WebSocket broadcast. */
    public ChatMessage sendMessage(User sender, MessageRequest request) {
        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found"));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(request.getContent())
                .vanish(request.isVanish())
                .seen(false)
                .deleted(false)
                .build();

        Message saved = messageRepository.save(message);
        return toChatMessage(saved, sender, receiver);
    }

    /**
     * Called when receiver sees a vanish message.
     * Deletes from DB immediately and returns the message ID
     * so the WebSocket can notify both users to remove the bubble.
     */
    @Transactional
    public Long markSeenAndDeleteIfVanish(Long messageId, User viewer) {
        Message msg = messageRepository.findById(messageId)
                .orElseThrow(() -> new RuntimeException("Message not found"));

        if (!msg.getReceiver().getId().equals(viewer.getId())) {
            throw new RuntimeException("Not authorized");
        }

        if (msg.isVanish() && !msg.isSeen()) {
            messageRepository.deleteById(messageId);
            return messageId;
        }
        return null;
    }

    /** Get full conversation between two users. */
    public List<MessageResponse> getConversation(User currentUser, Long otherUserId) {
        User otherUser = userRepository.findById(otherUserId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return messageRepository.findConversation(currentUser, otherUser)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /** Get latest message per conversation for inbox. */
    public List<MessageResponse> getInbox(User currentUser) {
        return messageRepository.findLatestMessagePerConversation(currentUser)
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    private ChatMessage toChatMessage(Message m, User sender, User receiver) {
        return ChatMessage.builder()
                .id(m.getId())
                .senderId(sender.getId())
                .senderUsername(sender.getUsername())
                .senderProfilePic(sender.getProfilePicUrl())
                .receiverId(receiver.getId())
                .receiverUsername(receiver.getUsername())
                .content(m.getContent())
                .createdAt(m.getCreatedAt())
                .vanish(m.isVanish())
                .seen(m.isSeen())
                .deleted(m.isDeleted())
                .build();
    }

    private MessageResponse toResponse(Message m) {
        return MessageResponse.builder()
                .id(m.getId())
                .senderId(m.getSender().getId())
                .senderUsername(m.getSender().getUsername())
                .senderProfilePic(m.getSender().getProfilePicUrl())
                .receiverId(m.getReceiver().getId())
                .receiverUsername(m.getReceiver().getUsername())
                .content(m.getContent())
                .createdAt(m.getCreatedAt())
                .vanish(m.isVanish())
                .seen(m.isSeen())
                .deleted(m.isDeleted())
                .build();
    }
}