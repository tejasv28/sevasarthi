package com.sevasarthi.backend.services;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender mailSender;

    @Value("${spring.mail.username:noreply@sevasarthi.in}")
    private String fromEmail;

    public void sendOtpEmail(String to, String otp, String subject) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom("Seva Sarthi <" + fromEmail + ">");
            helper.setTo(to);
            helper.setSubject(subject);

            String htmlContent = "<div style=\"font-family:sans-serif;max-width:400px;margin:auto;padding:30px;border:1px solid #e2e8f0;border-radius:16px;\">"
                    + "<h2 style=\"color:#0F172A;\">Seva Sarthi</h2>"
                    + "<p>Your OTP is:</p>"
                    + "<div style=\"background:#f8fafc;padding:20px;text-align:center;border-radius:12px;margin:20px 0;\">"
                    + "<span style=\"font-size:36px;font-weight:900;letter-spacing:8px;color:#0F172A;\">" + otp + "</span>"
                    + "</div>"
                    + "<p style=\"color:#94a3b8;font-size:13px;\">Valid for 10 minutes. Do not share.</p>"
                    + "</div>";

            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            System.err.println("Failed to send email to " + to + ": " + e.getMessage());
            
        }
    }
}
