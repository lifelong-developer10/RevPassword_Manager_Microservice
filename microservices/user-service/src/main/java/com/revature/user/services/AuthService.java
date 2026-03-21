package com.revature.user.services;

import com.revature.user.dtos.*;
import com.revature.user.models.MasterUser;
import com.revature.user.models.SecurityQuestionMaster;
import com.revature.user.models.SecurityQuestions;
import com.revature.user.repository.SecurityQuestionMasterRepository;
import com.revature.user.repository.SecurityQuestionRepository;
import com.revature.user.repository.UserRepository;
import com.revature.user.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;

@Service
@RequiredArgsConstructor

public class AuthService {

        private final AuthenticationManager authManager;
        private final JwtUtil jwtUtil;

        private final UserRepository userRepository;
        private final PasswordEncoder passwordEncoder;
        private final SecurityQuestionRepository userQuestionRepo;
        private final SecurityQuestionMasterRepository masterRepo;
        private final OtpService otpService;

        @Transactional
        public String register(RegisterRequest request) {

                if (request.getSecurityAnswers() == null ||
                                request.getSecurityAnswers().size() < 3) {
                        throw new RuntimeException("At least 3 security questions must be provided");
                }

                if (userRepository.existsByUsername(request.getUsername())) {
                        throw new RuntimeException("Username already exists");
                }

                if (userRepository.findByEmail(request.getEmail()).isPresent()) {
                        throw new RuntimeException("Email already exists");
                }

                MasterUser user = new MasterUser();

                user.setUsername(request.getUsername());
                user.setEmail(request.getEmail());
                user.setPhone(request.getPhone());
                user.setPasswordEncrypted(
                                passwordEncoder.encode(request.getPassword()));

                userRepository.save(user);

                for (UserQuestionAnswer dto : request.getSecurityAnswers()) {

                        if (dto.getQuestionId() == null) {
                                throw new RuntimeException("Question ID cannot be null");
                        }

                        SecurityQuestionMaster master = masterRepo.findById(dto.getQuestionId())
                                        .orElseThrow(() -> new RuntimeException(
                                                        "Invalid Question ID: "
                                                                        + dto.getQuestionId()));

                        SecurityQuestions uq = new SecurityQuestions();
                        System.out.println("Question ID: " + dto.getQuestionId());
                        System.out.println("Answer: " + dto.getAnswer());
                        uq.setUser(user);
                        uq.setQuestion(master);
                        if (dto.getAnswer() == null || dto.getAnswer().isBlank()) {
                                throw new RuntimeException("Security answer cannot be empty");
                        }
                        uq.setAnswerHash(
                                        passwordEncoder.encode(dto.getAnswer()));

                        userQuestionRepo.save(uq);
                }

                return "User Registered Successfully";
        }
//
//        public String login(LoginRequest request) {
//
//                Authentication authentication = authManager.authenticate(
//                                new UsernamePasswordAuthenticationToken(
//                                                request.getUsername(),
//                                                request.getPassword()));
//
//                return jwtUtil.generateToken(authentication.getName());
//        }


        public String login(LoginRequest request) {
                System.out.println("LOGIN ATTEMPT: " + request.getUsername());

                Authentication authentication = authManager.authenticate(
                        new UsernamePasswordAuthenticationToken(
                                request.getUsername(),
                                request.getPassword()));

                MasterUser user =
                        userRepository.findByUsername(authentication.getName()).orElseThrow();
                System.out.println("AUTH SUCCESS");
                System.out.println("2FA ENABLED: " + user.isTwoFactorEnabled());

                if (user.isTwoFactorEnabled()) {
                        otpService.generateAndSendOtp(user);
                        return "OTP_REQUIRED";
                }

                return jwtUtil.generateToken(authentication.getName());
        }
        public String changePassword(String username,
                        ChangePasswordRequest req) {

                MasterUser user = userRepository.findByUsername(username).orElseThrow();

                if (!passwordEncoder.matches(req.getCurrentPassword(),
                                user.getPasswordEncrypted())) {

                        throw new RuntimeException("Wrong current password");
                }

                user.setPasswordEncrypted(
                                passwordEncoder.encode(req.getNewPassword()));

                userRepository.save(user);

                return "Password Updated";
        }

        @Transactional
        public MasterUser updateProfile(String username,
                        UpdateProfileRequest req) {

                MasterUser user = userRepository.findByUsername(username)
                                .orElseThrow();


                if (req.getEmail() != null && !req.getEmail().isBlank()) {
                        user.setEmail(req.getEmail());
                }
                if (req.getPhone() != null && !req.getPhone().isBlank()) {
                        user.setPhone(req.getPhone());
                }
//                user.setEmail(req.getEmail());
//                user.setPhone(req.getPhone());

                return userRepository.save(user);
        }

        public List<SecurityQuestionMaster> getAllQuestions() {
                return masterRepo.findAll();
        }

        public boolean verifyMasterPassword(String username, String rawPassword) {
                MasterUser user = userRepository.findByUsername(username).orElseThrow();
                return passwordEncoder.matches(rawPassword, user.getPasswordEncrypted());
        }
}
