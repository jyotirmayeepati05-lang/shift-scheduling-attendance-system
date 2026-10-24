package com.shiftscheduling.notification.controller;

import com.shiftscheduling.notification.dto.*;
import com.shiftscheduling.notification.service.NotificationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notifications")
public class NotificationController {

    private final NotificationService service;

    public NotificationController(NotificationService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<NotificationResponse> create(@Valid @RequestBody NotificationRequest request) {
        return ResponseEntity.ok(service.createNotification(request));
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<Page<NotificationResponse>> getByUser(
            @PathVariable Long userId,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "20") int size) {
        PageRequest pr = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        return ResponseEntity.ok(service.getNotifications(userId, pr));
    }

    @GetMapping("/user/{userId}/unread")
    public ResponseEntity<List<NotificationResponse>> getUnread(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getUnreadNotifications(userId));
    }

    @GetMapping("/user/{userId}/count")
    public ResponseEntity<NotificationCountResponse> getUnreadCount(@PathVariable Long userId) {
        return ResponseEntity.ok(service.getUnreadCount(userId));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<NotificationResponse> markAsRead(@PathVariable Long id) {
        return ResponseEntity.ok(service.markAsRead(id));
    }

    @PutMapping("/user/{userId}/read-all")
    public ResponseEntity<Map<String, String>> markAllAsRead(@PathVariable Long userId) {
        service.markAllAsRead(userId);
        return ResponseEntity.ok(Map.of("message", "All notifications marked as read"));
    }

    @PostMapping("/schedule-published")
    public ResponseEntity<List<NotificationResponse>> schedulePublished(
            @Valid @RequestBody SchedulePublishedRequest request) {
        return ResponseEntity.ok(service.notifySchedulePublished(request));
    }

    @PostMapping("/leave-decision")
    public ResponseEntity<NotificationResponse> leaveDecision(
            @Valid @RequestBody LeaveDecisionRequest request) {
        return ResponseEntity.ok(service.notifyLeaveDecision(request));
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> health() {
        return ResponseEntity.ok(Map.of("status", "UP", "service", "notification-service"));
    }
}
