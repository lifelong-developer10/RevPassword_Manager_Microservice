package com.revature.notification.services;

import com.revature.notification.dtos.OtpRequest;
import com.revature.notification.repository.UserClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class OtpService {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private UserClient userClient;

    private Map<String, String> otpStorage = new HashMap<>();

    public String generateAndSendOtp(OtpRequest user) {

        String otp = String.valueOf((int) (Math.random() * 900000) + 100000);

        otpStorage.put(user.getUsername(), otp);

        // call user service to get email
        String email = userClient.getEmail(user.getUsername());

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("soumya20901@gmail.com");
        message.setTo(email);
        message.setSubject("Your Login OTP");
        message.setText("Your OTP for login is: " + otp);

        mailSender.send(message);

        return "OTP sent successfully to email";
    }

    public boolean verifyOtp(String username, String otp) {

        String storedOtp = otpStorage.get(username);

        return storedOtp != null && storedOtp.equals(otp);
    }
}