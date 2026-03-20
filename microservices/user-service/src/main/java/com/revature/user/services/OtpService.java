package com.revature.user.services;

import com.revature.user.models.MasterUser;
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

    private Map<String,String> otpStorage = new HashMap<>();

    public void generateAndSendOtp(MasterUser user) {

        String otp = String.valueOf((int)(Math.random()*900000)+100000);

        otpStorage.put(user.getUsername(), otp);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("soumya20901@gmail.com");
        message.setTo(user.getEmail());
        message.setSubject("Your Login OTP");
        message.setText("Your OTP for login is: " + otp);

        mailSender.send(message);
    }

    public boolean verifyOtp(String username,String otp){

        String storedOtp = otpStorage.get(username);

        return storedOtp != null && storedOtp.equals(otp);
    }


}