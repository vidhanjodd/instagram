package com.instagram.clone.service;

import com.instagram.clone.dto.MessageRequest;
import com.instagram.clone.dto.MessageResponse;
import com.instagram.clone.entity.Message;
import com.instagram.clone.entity.User;
import com.instagram.clone.repository.MessageRepository;
import com.instagram.clone.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MessageService {

    private final MessageRepository messageRepository;
    private final UserRepository userRepository;

    public MessageResponse sendMessage(MessageRequest request) {

        User sender = userRepository.findById(request.getSenderId())
                .orElseThrow(() -> new RuntimeException("Sender not found: " + request.getSenderId()));

        User receiver = userRepository.findById(request.getReceiverId())
                .orElseThrow(() -> new RuntimeException("Receiver not found: " + request.getReceiverId()));

        Message message = Message.builder()
                .sender(sender)
                .receiver(receiver)
                .content(request.getContent())
                .build();

        Message saved = messageRepository.save(message);

        return mapToResponse(saved);
    }

    public List<MessageResponse> getChat(Long senderId, Long receiverId) {

        List<Message> messages =
                messageRepository.findBySenderIdAndReceiverIdOrSenderIdAndReceiverIdOrderByCreatedAtAsc(
                        senderId, receiverId,
                        receiverId, senderId
                );

        return messages.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    private MessageResponse mapToResponse(Message message) {
        return MessageResponse.builder()
                .id(message.getId())
                .senderId(message.getSender().getId())
                .senderUsername(message.getSender().getUsername())
                .receiverId(message.getReceiver().getId())
                .receiverUsername(message.getReceiver().getUsername())
                .content(message.getContent())
                .createdAt(message.getCreatedAt())
                .build();
    }
}