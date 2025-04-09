package com.duongw.chatapp.service.email;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import jakarta.annotation.PostConstruct;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    /**
     * Sends a verification email to a newly registered user
     *
     * @param to               Recipient email
     * @param name             Recipient name
     * @param verificationLink Verification link
     */
    public void sendVerificationEmail(String to, String name, String verificationLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Verify Your Email Address");

            String content = "<html><body>"
                    + "<h2>Hello, " + name + "!</h2>"
                    + "<p>Please click the link below to verify your email address:</p>"
                    + "<p><a href=\"" + verificationLink + "\">Verify Email</a></p>"
                    + "<p>This link will expire in 24 hours.</p>"
                    + "<p>If you did not create an account, please ignore this email.</p>"
                    + "</body></html>";

            helper.setText(content, true);

            mailSender.send(message);
            log.info("Verification email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send verification email", e);
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    /**
     * Sends a password reset email
     *
     * @param to        Recipient email
     * @param name      Recipient name
     * @param resetLink Password reset link
     */
    public void sendPasswordResetEmail(String to, String name, String resetLink) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(to);
            helper.setSubject("Reset Your Password");

            String content = "<html><body>"
                    + "<h2>Hello, " + name + "!</h2>"
                    + "<p>You have requested to reset your password.</p>"
                    + "<p>Please click the link below to set a new password:</p>"
                    + "<p><a href=\"" + resetLink + "\">Reset Password</a></p>"
                    + "<p>This link will expire in 1 hour.</p>"
                    + "<p>If you did not request a password reset, please ignore this email.</p>"
                    + "</body></html>";

            helper.setText(content, true);

            mailSender.send(message);
            log.info("Password reset email sent to: {}", to);
        } catch (Exception e) {
            log.error("Failed to send password reset email", e);
            throw new RuntimeException("Failed to send password reset email", e);
        }
    }
}