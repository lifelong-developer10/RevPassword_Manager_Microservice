package com.revature.user.services;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.Map;

@FeignClient(name = "notification-service")
public interface NotificationClient {

    @PostMapping("/api/otp/generate")
    String generateOtp(@RequestBody Map<String, String> request);

    @PostMapping("/api/otp/verify")
    String verifyOtp(@RequestBody Map<String, String> request);
}
