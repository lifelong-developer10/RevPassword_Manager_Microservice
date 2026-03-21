package com.revature.user.services;

import com.revature.user.models.MasterUser;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Map;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final NotificationClient notificationClient;

    /**
     * Delegates OTP generation and email sending to notification-service.
     * notification-service stores the OTP and sends the email.
     */
    public void generateAndSendOtp(MasterUser user) {
        try {
            notificationClient.generateOtp(Map.of("username", user.getUsername()));
            System.out.println("OTP generation delegated to notification-service for: " + user.getUsername());
        } catch (Exception e) {
            System.err.println("Failed to generate OTP via notification-service: " + e.getMessage());
            throw new RuntimeException("Failed to send OTP. Please check mail configuration.");
        }
    }

    /**
     * Delegates OTP verification to notification-service.
     * notification-service holds the OTP in memory.
     */
    public boolean verifyOtp(String username, String otp) {
        try {
            String result = notificationClient.verifyOtp(Map.of("username", username, "code", otp));
            return "OTP Verified".equals(result);
        } catch (Exception e) {
            System.err.println("OTP verification failed: " + e.getMessage());
            return false;
        }
    }
}
