package com.revature.user.services;

import com.revature.user.models.MasterUser;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.MailException;
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

    private final Map<String, String> otpStorage = new HashMap<>();
    private final Map<String, Instant> otpExpiry = new HashMap<>();
    private static final long OTP_VALIDITY_SECONDS = 300; // 5 minutes

    public void generateAndSendOtp(MasterUser user) {

        String otp = String.valueOf((int)(Math.random() * 900000) + 100000);

        // Store OTP before attempting to send — so verifyOtp works even if mail is slow
        otpStorage.put(user.getUsername(), otp);
        otpExpiry.put(user.getUsername(), Instant.now().plusSeconds(OTP_VALIDITY_SECONDS));

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom("shubhadahshingate@gmail.com");
            message.setTo(user.getEmail());
            message.setSubject("Your Login OTP");
            message.setText("Your OTP for login is: " + otp + "\nThis OTP is valid for 5 minutes.");
            mailSender.send(message);
            System.out.println("OTP email sent to: " + user.getEmail());
        } catch (MailException e) {
            // Log the error but don't crash login — OTP is still stored in memory
            // so if mail is configured correctly it will work; for dev, log the OTP
            System.err.println("WARNING: Failed to send OTP email: " + e.getMessage());
            System.out.println("DEV MODE - OTP for " + user.getUsername() + " is: " + otp);
            throw new RuntimeException("Failed to send OTP email. Please check mail configuration: " + e.getMessage());
        }
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
