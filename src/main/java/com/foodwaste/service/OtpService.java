package com.foodwaste.service;

import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class OtpService {

    private static final int OTP_LENGTH = 6;
    private static final int OTP_EXPIRY_MINUTES = 10;
    private final SecureRandom secureRandom = new SecureRandom();
    private final Map<String, OtpEntry> otpStore = new ConcurrentHashMap<>();

    public String generateOtpForEmail(String email) {
        String otp = String.format("%0" + OTP_LENGTH + "d", secureRandom.nextInt(1_000_000));
        String normalizedEmail = normalizeEmail(email);
        otpStore.put(normalizedEmail, new OtpEntry(otp, LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES)));
        System.out.println("✅ OTP Generated for " + normalizedEmail + ": " + otp + " (Valid for " + OTP_EXPIRY_MINUTES + " minutes)");
        return otp;
    }

    public boolean verifyOtp(String email, String otp) {
        String key = normalizeEmail(email);
        String trimmedOtp = otp == null ? "" : otp.trim();
        
        System.out.println("DEBUG: Verifying OTP for " + key + " | OTP provided: '" + trimmedOtp + "'");
        
        OtpEntry entry = otpStore.get(key);
        if (entry == null) {
            System.out.println("❌ No OTP found for email: " + key + " | Available keys: " + otpStore.keySet());
            return false;
        }

        if (LocalDateTime.now().isAfter(entry.expiresAt())) {
            System.out.println("❌ OTP Expired for " + key);
            return false;
        }

        boolean valid = entry.otp().equals(trimmedOtp);
        System.out.println("OTP Match: " + entry.otp() + " == " + trimmedOtp + " ? " + valid);
        
        if (valid) {
            System.out.println("✅ OTP Verified for " + key + " (NOT removed yet)");
        } else {
            System.out.println("❌ OTP Mismatch for " + key + " | Expected: " + entry.otp() + ", Got: " + trimmedOtp);
        }
        return valid;
    }

    public boolean consumeOtp(String email, String otp) {
        String key = normalizeEmail(email);
        String trimmedOtp = otp == null ? "" : otp.trim();
        
        System.out.println("DEBUG: Consuming OTP for " + key + " | OTP provided: '" + trimmedOtp + "'");
        
        OtpEntry entry = otpStore.get(key);
        if (entry == null) {
            System.out.println("❌ No OTP found for email: " + key);
            return false;
        }

        if (LocalDateTime.now().isAfter(entry.expiresAt())) {
            System.out.println("❌ OTP Expired for " + key);
            otpStore.remove(key);
            return false;
        }

        boolean valid = entry.otp().equals(trimmedOtp);
        if (valid) {
            otpStore.remove(key);
            System.out.println("✅ OTP Consumed and removed for " + key);
        } else {
            System.out.println("❌ OTP Mismatch during consumption");
        }
        return valid;
    }

    private String normalizeEmail(String email) {
        return email == null ? "" : email.trim().toLowerCase();
    }

    private record OtpEntry(String otp, LocalDateTime expiresAt) {}
}
