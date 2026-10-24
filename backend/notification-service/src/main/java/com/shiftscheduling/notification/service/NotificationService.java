package com.shiftscheduling.notification.service;

import com.shiftscheduling.notification.dto.*;
import com.shiftscheduling.notification.entity.Notification;
import com.shiftscheduling.notification.entity.NotificationType;
import com.shiftscheduling.notification.repository.NotificationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class NotificationService {

    private final NotificationRepository repository;

    public NotificationService(NotificationRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public NotificationResponse createNotification(NotificationRequest request) {
        Notification notification = new Notification();
        notification.setUserId(request.userId());
        notification.setTitle(request.title());
        notification.setMessage(request.message());
        notification.setNotificationType(request.type());
        notification.setReferenceId(request.referenceId());

        Notification saved = repository.save(notification);
        return toResponse(saved);
    }

    public Page<NotificationResponse> getNotifications(Long userId, Pageable pageable) {
        return repository.findByUserIdOrderByCreatedAtDesc(userId, pageable)
                .map(this::toResponse);
    }

    public List<NotificationResponse> getUnreadNotifications(Long userId) {
        return repository.findByUserIdAndIsReadFalseOrderByCreatedAtDesc(userId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public NotificationCountResponse getUnreadCount(Long userId) {
        long count = repository.countByUserIdAndIsReadFalse(userId);
        return new NotificationCountResponse(count);
    }

    @Transactional
    public NotificationResponse markAsRead(Long notificationId) {
        Notification notification = repository.findById(notificationId)
                .orElseThrow(() -> new RuntimeException("Notification not found with id: " + notificationId));
        notification.setRead(true);
        Notification saved = repository.save(notification);
        return toResponse(saved);
    }

    @Transactional
    public void markAllAsRead(Long userId) {
        repository.markAllAsReadByUserId(userId);
    }

    @Transactional
    public List<NotificationResponse> notifySchedulePublished(SchedulePublishedRequest request) {
        List<NotificationResponse> responses = new ArrayList<>();
        String scheduleName = request.scheduleName() != null ? request.scheduleName() : "Schedule";

        for (Long userId : request.userIds()) {
            Notification notification = new Notification();
            notification.setUserId(userId);
            notification.setTitle("Schedule Published");
            notification.setMessage("A new schedule has been published: " + scheduleName + ". Please check your upcoming shifts.");
            notification.setNotificationType(NotificationType.SCHEDULE_PUBLISHED);
            notification.setReferenceId(request.scheduleId());

            Notification saved = repository.save(notification);
            responses.add(toResponse(saved));
        }

        return responses;
    }

    @Transactional
    public NotificationResponse notifyLeaveDecision(LeaveDecisionRequest request) {
        Notification notification = new Notification();
        notification.setUserId(request.userId());

        if (Boolean.TRUE.equals(request.approved())) {
            notification.setTitle("Leave Request Approved");
            String msg = "Your leave request has been approved.";
            if (request.reason() != null && !request.reason().isBlank()) {
                msg += " Note: " + request.reason();
            }
            notification.setMessage(msg);
            notification.setNotificationType(NotificationType.LEAVE_APPROVED);
        } else {
            notification.setTitle("Leave Request Rejected");
            String msg = "Your leave request has been rejected.";
            if (request.reason() != null && !request.reason().isBlank()) {
                msg += " Reason: " + request.reason();
            }
            notification.setMessage(msg);
            notification.setNotificationType(NotificationType.LEAVE_REJECTED);
        }

        notification.setReferenceId(request.leaveId());

        Notification saved = repository.save(notification);
        return toResponse(saved);
    }

    private NotificationResponse toResponse(Notification n) {
        return new NotificationResponse(
                n.getId(),
                n.getUserId(),
                n.getTitle(),
                n.getMessage(),
                n.getNotificationType(),
                n.getReferenceId(),
                n.isRead(),
                n.getCreatedAt()
        );
    }
}
