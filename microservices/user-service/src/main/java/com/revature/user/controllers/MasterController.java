package com.revature.user.controllers;

import com.revature.user.dtos.*;
import com.revature.user.models.MasterUser;
import com.revature.user.models.SecurityQuestionMaster;
import com.revature.user.repository.UserRepository;
import com.revature.user.services.AuthService;
import com.revature.user.services.ForgotPasswordService;
import lombok.RequiredArgsConstructor;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class MasterController {

    private final AuthService service;
    private final UserRepository userRepo;
    private final ForgotPasswordService forgotPasswordService;

    @PostMapping("/register")
    public ResponseEntity<?> register(@Valid @RequestBody RegisterRequest req) {
        System.out.println("RECEIVING REGISTRATION REQUEST: " + req.getUsername());
        if(req.getSecurityAnswers() != null) {
            System.out.println("SECURITY QUESTIONS RECEIVED: " + req.getSecurityAnswers().size());
        } else {
            System.out.println("SECURITY QUESTIONS LIST IS NULL");
        }
        String message = service.register(req);

        return ResponseEntity.ok().body(
                Map.of("message", message)
        );
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest req) {

        String result = service.login(req);

        if(result.equals("OTP_REQUIRED")){
            return ResponseEntity.ok(
                    new AuthResponse("OTP_REQUIRED", "OTP Required")
            );
        }

        return ResponseEntity.ok(
                new AuthResponse(result, "Login Successful")
        );
    }

    @GetMapping
    public ProfileResponse getProfile(Authentication auth) {

        String username = auth.getName();

        MasterUser user =
                userRepo.findByUsername(username)
                        .orElseThrow();
        System.out.println("PROFILE USER: " + username);
        return new ProfileResponse(
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.isTwoFactorEnabled()
        );

    }
    @GetMapping("/security-questions")
    public List<SecurityQuestionMaster> getAllQuestions() {

        return service.getAllQuestions();
    }








}



