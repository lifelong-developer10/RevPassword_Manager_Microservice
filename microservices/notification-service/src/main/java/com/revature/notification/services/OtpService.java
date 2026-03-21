package com.revature.notification.services;

import com.revature.notification.dtos.OtpRequest;
import com.revature.notification.models.OTPGenerater;
import com.revature.notification.repository.OtpRepository;
import com.revature.notification.repository.UserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
public class OtpService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserClient userClient;

    @Autowired
    private OtpRepository otpRepository;

    private static final int OTP_VALIDITY_MINUTES = 5;

    public String generateAndSendOtp(OtpRequest request) {

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

        // Persist OTP to DB (replaces in-memory HashMap)
        OTPGenerater entity = new OTPGenerater();
        entity.setOwnerUsername(request.getUsername());
        entity.setCode(otp);
        entity.setExpiryTime(LocalDateTime.now().plusMinutes(OTP_VALIDITY_MINUTES));
        entity.setUsed(false);
        otpRepository.save(entity);

        // Fetch email from user-service via Feign
        String email = userClient.getEmail(request.getUsername());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("shubhadahshingate@gmail.com");
        message.setTo(email);
        message.setSubject("Your Login OTP");
        message.setText("Your OTP for login is: " + otp
                + "\nThis OTP is valid for " + OTP_VALIDITY_MINUTES + " minutes.");

        mailSender.send(message);

        return "OTP sent successfully to email";
    }

    public boolean verifyOtp(String username, String otp) {

        OTPGenerater entity = otpRepository
                .findTopByOwnerUsernameOrderByExpiryTimeDesc(username)
                .orElse(null);

        if (entity == null || entity.isUsed()) {
            return false;
        }

        if (LocalDateTime.now().isAfter(entity.getExpiryTime())) {
            return false;
        }

        if (!entity.getCode().equals(otp)) {
            return false;
        }

        // Mark as used so it cannot be replayed
        entity.setUsed(true);
        otpRepository.save(entity);

        return true;
    }
}
