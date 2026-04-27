package com.foodwaste.service;

import com.foodwaste.exception.BadRequestException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailService {
    
    private final JavaMailSender mailSender;
    
    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }
    
    @Value("${spring.mail.username}")
    private String fromEmail;
    
    @Async
    public void sendWelcomeEmail(String to, String name) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(to);
            message.setSubject("Welcome to Food Donation System");
            message.setText("Dear " + name + ",\n\n" +
                    "Welcome to the Food Donation and Waste Management System!\n\n" +
                    "Thank you for joining our mission to reduce food waste and help those in need.\n\n" +
                    "Best regards,\nFood Donation Team");
            
            mailSender.send(message);
            System.out.println("Welcome email sent to " + to);
        } catch (Exception e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
        }
    }

    @Async
    public void sendOtpEmail(String to, String otp) {
        String normalizedFrom = fromEmail == null ? "" : fromEmail.trim().toLowerCase();
        if (normalizedFrom.isEmpty() || normalizedFrom.contains("your-email@gmail.com")) {
            System.err.println("Email service is not configured. Please set spring.mail.username and spring.mail.password in backend application.properties");
            return;
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(to);
        message.setSubject("Your OTP for Food Donation System");
        message.setText("Hello,\n\n" +
                "Your OTP code is: " + otp + "\n" +
                "This OTP is valid for 10 minutes.\n\n" +
                "If you did not request this, please ignore this message.\n\n" +
                "Best regards,\nFood Donation Team");

        try {
            mailSender.send(message);
            System.out.println("✅ OTP email sent successfully to " + to);
        } catch (Exception e) {
            String reason = e.getMessage() == null ? "Unknown SMTP error" : e.getMessage();
            System.err.println("❌ Failed to send OTP email to " + to + ": " + reason + 
                    ". Check MAIL_USERNAME, MAIL_PASSWORD (Gmail App Password), and internet connectivity");
        }
    }
    
    @Async
    public void sendRequestNotification(String donorEmail, String ngoName, String foodName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(donorEmail);
            message.setSubject("New Food Request Received");
            message.setText("Hello,\n\n" +
                    ngoName + " has requested your food donation: " + foodName + "\n\n" +
                    "Please log in to approve or reject this request.\n\n" +
                    "Best regards,\nFood Donation Team");
            
            mailSender.send(message);
            System.out.println("Request notification sent to " + donorEmail);
        } catch (Exception e) {
            System.err.println("Failed to send request notification: " + e.getMessage());
        }
    }
    
    @Async
    public void sendApprovalNotification(String ngoEmail, String donorName, String foodName) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(ngoEmail);
            message.setSubject("Food Request Approved!");
            message.setText("Hello,\n\n" +
                    "Great news! " + donorName + " has approved your request for: " + foodName + "\n\n" +
                    "Please arrange pickup at your earliest convenience.\n\n" +
                    "Best regards,\nFood Donation Team");
            
            mailSender.send(message);
            System.out.println("Approval notification sent to " + ngoEmail);
        } catch (Exception e) {
            System.err.println("Failed to send approval notification: " + e.getMessage());
        }
    }
    
    @Async
    public void sendDeliveryConfirmation(String donorEmail, String ngoEmail, String foodName) {
        try {
            SimpleMailMessage donorMessage = new SimpleMailMessage();
            donorMessage.setFrom(fromEmail);
            donorMessage.setTo(donorEmail);
            donorMessage.setSubject("Delivery Completed");
            donorMessage.setText("Hello,\n\n" +
                    "Your food donation (" + foodName + ") has been successfully delivered!\n\n" +
                    "Thank you for your contribution to reducing food waste.\n\n" +
                    "Best regards,\nFood Donation Team");
            
            mailSender.send(donorMessage);
            
            SimpleMailMessage ngoMessage = new SimpleMailMessage();
            ngoMessage.setFrom(fromEmail);
            ngoMessage.setTo(ngoEmail);
            ngoMessage.setSubject("Delivery Confirmed");
            ngoMessage.setText("Hello,\n\n" +
                    "The food delivery (" + foodName + ") has been confirmed.\n\n" +
                    "Thank you for your service!\n\n" +
                    "Best regards,\nFood Donation Team");
            
            mailSender.send(ngoMessage);
            System.out.println("Delivery confirmation sent");
        } catch (Exception e) {
            System.err.println("Failed to send delivery confirmation: " + e.getMessage());
        }
    }

    @Async
    public void sendLoginNotification(String userEmail, String userName, String userRole) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(userEmail);
            message.setSubject("Login Notification - Food Donation System");
            message.setText("Hello " + userName + ",\n\n" +
                    "You have successfully logged into your " + userRole + " account on the Food Donation System.\n\n" +
                    "If this wasn't you, please secure your account immediately.\n\n" +
                    "Login Time: " + java.time.LocalDateTime.now() + "\n" +
                    "Role: " + userRole + "\n\n" +
                    "Best regards,\nFood Donation Team");
            
            mailSender.send(message);
            System.out.println("Login notification sent to " + userEmail);
        } catch (Exception e) {
            System.err.println("Failed to send login notification: " + e.getMessage());
        }
    }
}
