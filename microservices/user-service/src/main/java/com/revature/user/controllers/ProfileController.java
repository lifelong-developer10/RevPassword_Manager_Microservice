package com.revature.user.controllers;


import com.revature.user.dtos.*;
import com.revature.user.models.MasterUser;
import com.revature.user.repository.UserRepository;
import com.revature.user.security.JwtUtil;
import com.revature.user.services.OtpService;
import com.revature.user.services.AuthService;
import com.revature.user.services.ForgotPasswordService;
import com.revature.user.services.SecurityQuestionService;
import com.revature.user.services.TwoFactorService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
@RestController
@RequestMapping("/api/profile")
@RequiredArgsConstructor
public class  ProfileController {

    private final UserRepository userRepository;
    private final AuthService authService;
    private final SecurityQuestionService security;
    private final TwoFactorService twoFactorService;
    private final ForgotPasswordService forgotPasswordService;
    private final OtpService otpService;
    private final JwtUtil jwtUtil;

    // ================= PROFILE =================

    @GetMapping
    public ProfileResponse getProfile(Authentication auth) {

        String username = auth.getName();

        MasterUser user =
                userRepository.findByUsername(username)
                        .orElseThrow();

        return new ProfileResponse(
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.isTwoFactorEnabled()
        );
    }

    @PutMapping
    public ResponseEntity<?> updateProfile(
            Authentication auth,
            @RequestBody UpdateProfileRequest request) {

        String username = auth.getName();

        MasterUser updatedUser = authService.updateProfile(username, request);

        return ResponseEntity.ok(
                Map.of("message", "Profile updated successfully",
                        "email", updatedUser.getEmail(),
                        "phone", updatedUser.getPhone())
        );
    }
    // ================= PASSWORD =================

    @PostMapping("/change-password")
    public Map<String, String> changePassword(
            Authentication auth,
            @RequestBody ChangePasswordRequest req) {
        String username = auth.getName();

        authService.changePassword(username, req);

        return Map.of("message", "Password Updated");
    }

    // ================= SECURITY QUESTIONS =================

    @GetMapping("/security-questions")
    public List<UserQuestionAnswer> getUserSecurityQuestions(
            Authentication auth) {

        String username = auth.getName();

        return forgotPasswordService.getUserQuestionsWithMask(username);
    }

    @PutMapping("/security-questions")
    public Map<String, String> updateQuestions(
            Authentication auth,
            @RequestBody List<UserQuestionAnswer> list) {

        String username = auth.getName();

        return security.updateQuestions(username, list);
    }

    // ================= VERIFY MASTER PASSWORD =================

    /**
     * Verifies the master password without changing it.
     * Used by the frontend to gate access to sensitive vault operations.
     */
    @PostMapping("/verify-master-password")
    public ResponseEntity<?> verifyMasterPassword(
            Authentication auth,
            @RequestBody Map<String, String> body) {

        String username = auth.getName();
        String masterPassword = body.get("masterPassword");

        if (masterPassword == null || masterPassword.isBlank()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Master password is required"));
        }

        boolean valid = authService.verifyMasterPassword(username, masterPassword);

        if (!valid) {
            return ResponseEntity.status(org.springframework.http.HttpStatus.UNAUTHORIZED)
                    .body(Map.of("error", "Invalid master password"));
        }

        return ResponseEntity.ok(Map.of("message", "Verified"));
    }

    // ================= 2FA =================

    @PostMapping("/2fa")
    public String update2FA(
            Authentication auth,
            @RequestBody TwoFactorRequest request) {

        String username = auth.getName();

        return twoFactorService.updateTwoFactor(
                username,
                request.isEnabled());
    }


    @PostMapping("/verify-otp")
    public ResponseEntity<AuthResponse> verifyOtp(@RequestBody OtpRequest request) {

        boolean valid =
                otpService.verifyOtp(request.getUsername(), request.getOtp());

        if (!valid) {
            return ResponseEntity.ok(
                    new AuthResponse(null, "INVALID_OTP")
            );
        }

        String token = jwtUtil.generateToken(request.getUsername());

        return ResponseEntity.ok(
                new AuthResponse(token, "OTP Verified")
        );
    }
}

