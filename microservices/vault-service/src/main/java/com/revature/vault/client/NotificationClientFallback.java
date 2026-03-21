package com.revature.vault.client;

import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

/**
 * Fallback for NotificationClient — if notification-service is down,
 * vault operations continue without failing.
 */
@Component
public class NotificationClientFallback implements FallbackFactory<NotificationClient> {

    @Override
    public NotificationClient create(Throwable cause) {
        return new NotificationClient() {
            @Override
            public String sendNotification(Map<String, String> request) {
                System.err.println("NotificationClient fallback triggered: " + cause.getMessage());
                return "Notification skipped (service unavailable)";
            }
        };
    }
}
