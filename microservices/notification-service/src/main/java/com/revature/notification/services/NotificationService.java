package com.revature.notification.services;


import com.revature.notification.dtos.NotificationRequest;
import com.revature.notification.dtos.NotificationResponse;
import com.revature.notification.dtos.SecurityAlertRequest;
import com.revature.notification.models.Notification;
import com.revature.notification.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository repo;
    private final JavaMailSender mailSender;

    public void sendNotification(NotificationRequest request) {

        Notification notification = new Notification();

        notification.setUsername(request.getUsername());
        notification.setMessage(request.getMessage());
        notification.setType(request.getType());
        notification.setCreatedAt(LocalDateTime.now());
        notification.setReadStatus(false);
        repo.save(notification);
    }

    public void sendSecurityAlert(SecurityAlertRequest request) {

        Notification notification = new Notification();

        notification.setUsername(request.getUsername());
        notification.setMessage(request.getAlertType());
        notification.setType("SECURITY_ALERT");
        notification.setCreatedAt(LocalDateTime.now());
        notification.setReadStatus(false);

        repo.save(notification);
    }
    public List<NotificationResponse> getNotifications(String username) {

        List<Notification> notifications =
                repo.findByUsernameAndReadStatusFalse(username);

        return notifications.stream().map(n -> {
            NotificationResponse dto = new NotificationResponse();

            dto.setId(n.getId());
            dto.setUsername(n.getUsername());
            dto.setMessage(n.getMessage());
            dto.setType(n.getType());
            dto.setReadStatus(n.isReadStatus());
            dto.setCreatedAt(n.getCreatedAt());
            return dto;
        }).toList();
    }
    public void markAsRead(Long id) {

        Notification notification = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Notification not found"));

        notification.setReadStatus(true);

        repo.save(notification);
    }
    public void deleteNotification(Long id) {
        repo.deleteById(id);
    }

}