package com.revature.user.controllers;

import com.revature.user.models.MasterUser;
import com.revature.user.repository.UserRepository;
import com.revature.user.security.JwtUtil;
import com.revature.user.services.*;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(ProfileController.class)
@AutoConfigureMockMvc(addFilters = false)
public class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private UserRepository userRepository;
    @MockBean
    private AuthService authService;
    @MockBean
    private SecurityQuestionService security;
    @MockBean
    private TwoFactorService twoFactorService;
    @MockBean
    private ForgotPasswordService forgotPasswordService;
    @MockBean
    private OtpService otpService;
    @MockBean
    private JwtUtil jwtUtil;
    @MockBean
    private com.revature.user.security.CustomUserDetailsService userDetailsService;
    @MockBean
    private com.revature.user.security.JwtFilter jwtFilter;

    @Test
    void getProfile_ShouldReturnUser_WhenAuthenticated() throws Exception {
        MasterUser user = new MasterUser();
        user.setUsername("testuser");
        user.setEmail("test@example.com");
        user.setPhone("1234567890");

        when(userRepository.findByUsername("testuser")).thenReturn(java.util.Optional.of(user));

        mockMvc.perform(get("/api/profile")
                        .principal(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken("testuser", null, new java.util.ArrayList<>())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.email").value("test@example.com"));
    }
}
