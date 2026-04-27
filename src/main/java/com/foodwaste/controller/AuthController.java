package com.foodwaste.controller;

import com.foodwaste.dto.AuthResponse;
import com.foodwaste.dto.EmailOtpRequest;
import com.foodwaste.dto.GoogleLoginRequest;
import com.foodwaste.dto.LoginRequest;
import com.foodwaste.dto.RegisterRequest;
import com.foodwaste.dto.VerifyOtpRequest;
import com.foodwaste.service.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin
public class AuthController {
    
    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }
    
    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.ok(authService.register(request));
    }
    
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
    
    @PostMapping("/google")
    public ResponseEntity<AuthResponse> googleLogin(@RequestBody GoogleLoginRequest request) {
        return ResponseEntity.ok(authService.googleLogin(request));
    }

    @PostMapping("/send-otp")
    public ResponseEntity<Map<String, String>> sendOtp(@Valid @RequestBody EmailOtpRequest request) {
        authService.sendRegistrationOtp(request.getEmail());
        return ResponseEntity.ok(Map.of("message", "OTP sent to your email"));
    }

    @PostMapping("/verify-otp")
    public ResponseEntity<Map<String, String>> verifyOtp(@Valid @RequestBody VerifyOtpRequest request) {
        String email = request.getEmail() == null ? "" : request.getEmail().trim().toLowerCase();
        String otp = request.getOtp() == null ? "" : request.getOtp().trim();
        
        System.out.println("📨 /verify-otp request: email=" + email + ", otp=" + otp);
        
        boolean valid = authService.verifyRegistrationOtp(email, otp);
        if (!valid) {
            System.out.println("❌ OTP verification failed for " + email);
            return ResponseEntity.badRequest().body(Map.of("message", "Invalid or expired OTP"));
        }
        System.out.println("✅ OTP verification successful for " + email);
        return ResponseEntity.ok(Map.of("message", "OTP verified"));
    }
}
