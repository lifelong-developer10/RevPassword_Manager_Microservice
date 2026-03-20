package com.revature.notification.services;

import com.revature.notification.dtos.OtpRequest;
import com.revature.notification.repository.UserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

@Service
public class OtpService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserClient userClient;

    private final Map<String, String> otpStorage = new HashMap<>();
    private final Map<String, Instant> otpExpiry = new HashMap<>();
    private static final long OTP_VALIDITY_SECONDS = 300; // 5 minutes

    public String generateAndSendOtp(OtpRequest user) {

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

        otpStorage.put(user.getUsername(), otp);
        otpExpiry.put(user.getUsername(), Instant.now().plusSeconds(OTP_VALIDITY_SECONDS));

        // call user service to get email
        String email = userClient.getEmail(user.getUsername());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("shubhadahshingate@gmail.com");
        message.setTo(email);
        message.setSubject("Your Login OTP");
        message.setText("Your OTP for login is: " + otp + "\nThis OTP is valid for 5 minutes.");

        mailSender.send(message);

        return "OTP sent successfully to email";
    }

    public boolean verifyOtp(String username, String otp) {

        String storedOtp = otpStorage.get(username);
        Instant expiry = otpExpiry.get(username);

        if (storedOtp == null || expiry == null) {
            return false;
        }

        if (Instant.now().isAfter(expiry)) {
            otpStorage.remove(username);
            otpExpiry.remove(username);
            return false;
        }

        boolean valid = storedOtp.equals(otp);

        if (valid) {
            otpStorage.remove(username);
            otpExpiry.remove(username);
        }

        return valid;
    }
}
