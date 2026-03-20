package com.revature.user.services;

import com.revature.user.dtos.RegisterRequest;
import com.revature.user.models.MasterUser;
import com.revature.user.repository.SecurityQuestionMasterRepository;
import com.revature.user.repository.SecurityQuestionRepository;
import com.revature.user.repository.UserRepository;
import com.revature.user.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private AuthenticationManager authManager;
    @Mock
    private JwtUtil jwtUtil;
    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private SecurityQuestionRepository userQuestionRepo;
    @Mock
    private SecurityQuestionMasterRepository masterRepo;
    @Mock
    private OtpService otpService;
    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password");
        registerRequest.setEmail("test@example.com");
        registerRequest.setSecurityAnswers(new ArrayList<>());
    }

    @Test
    void register_ShouldThrowException_WhenLessThenThreeQuestions() {
        assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
    }

    @Test
    void register_ShouldThrowException_WhenUsernameExists() {
        registerRequest.setSecurityAnswers(new ArrayList<>(java.util.List.of(new com.revature.user.dtos.UserQuestionAnswer(), new com.revature.user.dtos.UserQuestionAnswer(), new com.revature.user.dtos.UserQuestionAnswer())));
        lenient().when(userRepository.existsByUsername(anyString())).thenReturn(true);
        assertThrows(RuntimeException.class, () -> authService.register(registerRequest));
    }

    @Test
    void changePassword_ShouldThrowException_WhenPasswordsDoNotMatch() {
        MasterUser user = new MasterUser();
        user.setPasswordEncrypted("encodedPassword");
        
        com.revature.user.dtos.ChangePasswordRequest req = new com.revature.user.dtos.ChangePasswordRequest();
        req.setCurrentPassword("wrong");
        
        lenient().when(userRepository.findByUsername("testuser")).thenReturn(java.util.Optional.of(user));
        lenient().when(passwordEncoder.matches(anyString(), anyString())).thenReturn(false);

        assertThrows(RuntimeException.class, () -> authService.changePassword("testuser", req));
    }
}
