package com.foodwaste.service;

import com.foodwaste.dto.*;
import com.foodwaste.entity.User;
import com.foodwaste.exception.BadRequestException;
import com.foodwaste.exception.ResourceNotFoundException;
import com.foodwaste.repository.UserRepository;
import com.foodwaste.security.JwtService;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.gson.GsonFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
public class AuthService {
    
    private static final Logger log = LoggerFactory.getLogger(AuthService.class);
    
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final EmailService emailService;
    private final OtpService otpService;
    
    public AuthService(UserRepository userRepository,
                      PasswordEncoder passwordEncoder,
                      JwtService jwtService,
                      AuthenticationManager authenticationManager,
                      EmailService emailService,
                      OtpService otpService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.emailService = emailService;
        this.otpService = otpService;
    }
    
    @Value("${google.client.id}")
    private String googleClientId;
    
    public AuthResponse register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        if ("ADMIN".equalsIgnoreCase(request.getRole())) {
            throw new BadRequestException("Admin registration is not allowed");
        }

        if (!otpService.consumeOtp(request.getEmail(), request.getOtp())) {
            throw new BadRequestException("Invalid or expired OTP");
        }
        
        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(User.UserRole.valueOf(request.getRole()));
        user.setPhone(request.getPhone());
        user.setStatus(User.UserStatus.ACTIVE);
        
        user = userRepository.save(user);
        
        // Send welcome email
        emailService.sendWelcomeEmail(user.getEmail(), user.getName());
        
        String jwtToken = jwtService.generateToken(user);
        
        return new AuthResponse(jwtToken, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }

    public void sendRegistrationOtp(String email) {
        if (email == null || email.isBlank()) {
            throw new BadRequestException("Email is required");
        }

        if (userRepository.existsByEmail(email)) {
            throw new BadRequestException("Email already registered");
        }

        String otp = otpService.generateOtpForEmail(email);
        emailService.sendOtpEmail(email, otp);
    }

    public boolean verifyRegistrationOtp(String email, String otp) {
        return otpService.verifyOtp(email, otp);
    }
    
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );
        
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
        
        String jwtToken = jwtService.generateToken(user);
        
        // Send login notification email (exclude admin)
        if (!user.getRole().name().equals("ADMIN")) {
            emailService.sendLoginNotification(user.getEmail(), user.getName(), user.getRole().name());
        }
        
        return new AuthResponse(jwtToken, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
    
    public AuthResponse googleLogin(GoogleLoginRequest request) {
        try {
            GoogleIdTokenVerifier verifier = new GoogleIdTokenVerifier.Builder(
                    new NetHttpTransport(), new GsonFactory())
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
            
            GoogleIdToken idToken = verifier.verify(request.getToken());
            
            if (idToken != null) {
                GoogleIdToken.Payload payload = idToken.getPayload();
                String email = payload.getEmail();
                String name = (String) payload.get("name");
                String googleId = payload.getSubject();
                
                User user = userRepository.findByEmail(email)
                        .orElseGet(() -> {
                            User newUser = new User();
                            newUser.setEmail(email);
                            newUser.setName(name);
                            newUser.setGoogleId(googleId);
                            newUser.setRole(User.UserRole.DONOR);
                            newUser.setStatus(User.UserStatus.ACTIVE);
                            
                            User savedUser = userRepository.save(newUser);
                            emailService.sendWelcomeEmail(email, name);
                            return savedUser;
                        });
                
                if (user.getGoogleId() == null) {
                    user.setGoogleId(googleId);
                    userRepository.save(user);
                }
                
                String jwtToken = jwtService.generateToken(user);
                
                // Send login notification email (exclude admin)
                if (!user.getRole().name().equals("ADMIN")) {
                    emailService.sendLoginNotification(user.getEmail(), user.getName(), user.getRole().name());
                }
                
                return new AuthResponse(jwtToken, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
            }
            
            throw new BadRequestException("Invalid Google token");
        } catch (Exception e) {
            log.error("Google login error: ", e);
            throw new BadRequestException("Google authentication failed: " + e.getMessage());
        }
    }
}
