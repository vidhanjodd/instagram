package com.instagram.clone.controller;

import com.instagram.clone.dto.NotificationDto;
import com.instagram.clone.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;
import java.util.Map;


@RestController
@RequestMapping("/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;


    @GetMapping
    public ResponseEntity<List<NotificationDto>> getNotifications(Principal principal) {

        List<NotificationDto> notifications = notificationService.getNotifications(principal.getName());
        return ResponseEntity.ok(notifications);
    }


    @GetMapping("/count")
    public ResponseEntity<Map<String, Long>> getUnreadCount(Principal principal) {
        long count = notificationService.getUnreadCount(principal.getName());
        return ResponseEntity.ok(Map.of("count", count));
    }

    @PostMapping("/mark-read")
    public ResponseEntity<Map<String, Boolean>> markAllRead(Principal principal) {
        notificationService.markAllRead(principal.getName());
        return ResponseEntity.ok(Map.of("success", true));
    }
}