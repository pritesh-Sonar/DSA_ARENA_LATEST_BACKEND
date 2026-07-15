package com.dsaarena.dsa_arena_backend.otp.service;

import com.dsaarena.dsa_arena_backend.enums.OtpPurpose;
import com.dsaarena.dsa_arena_backend.exception.BadRequestException;
import com.dsaarena.dsa_arena_backend.otp.entity.OtpVerification;
import com.dsaarena.dsa_arena_backend.otp.repository.OtpRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class OtpService {

    private final OtpRepository otpRepository;
    private final JavaMailSender mailSender;

    @Value("${app.otp.expiry-minutes}")
    private int expiryMinutes;

    @Value("${app.otp.max-attempts}")
    private int maxAttempts;

    private static final SecureRandom RANDOM = new SecureRandom();

    // Used for SIGNUP — carries pending user data until verified
    public void generateAndSendSignupOtp(String email, String username, String hashedPassword) {
        String otpCode = generateCode();

        OtpVerification otp = OtpVerification.builder()
                .identifier(email)
                .otpCode(otpCode)
                .purpose(OtpPurpose.SIGNUP)
                .pendingUsername(username)
                .pendingPasswordHash(hashedPassword)
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .build();

        otpRepository.save(otp);
        sendEmail(email, otpCode, OtpPurpose.SIGNUP);
    }

    // Used for RESET_PASSWORD — no pending user data needed
    public void generateAndSendResetOtp(String email) {
        String otpCode = generateCode();

        OtpVerification otp = OtpVerification.builder()
                .identifier(email)
                .otpCode(otpCode)
                .purpose(OtpPurpose.RESET_PASSWORD)
                .expiresAt(LocalDateTime.now().plusMinutes(expiryMinutes))
                .build();

        otpRepository.save(otp);
        sendEmail(email, otpCode, OtpPurpose.RESET_PASSWORD);
    }

    // Returns the validated record so the caller can read pending fields (signup) if needed
    public OtpVerification verifyOtp(String email, String otpCode, OtpPurpose purpose) {
        OtpVerification otp = otpRepository
                .findTopByIdentifierAndPurposeAndUsedFalseOrderByIdDesc(email, purpose)
                .orElseThrow(() -> new BadRequestException("No OTP found. Please request a new one."));

        if (otp.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("OTP has expired. Please request a new one.");
        }

        if (otp.getAttemptCount() >= maxAttempts) {
            throw new BadRequestException("Too many failed attempts. Please request a new OTP.");
        }

        if (!otp.getOtpCode().equals(otpCode)) {
            otp.setAttemptCount(otp.getAttemptCount() + 1);
            otpRepository.save(otp);
            throw new BadRequestException("Invalid OTP");
        }

        otp.setUsed(true);
        otpRepository.save(otp);
        return otp;
    }

    private String generateCode() {
        return String.format("%06d", RANDOM.nextInt(1_000_000));
    }

    private void sendEmail(String to, String otpCode, OtpPurpose purpose) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject(purpose == OtpPurpose.SIGNUP
                ? "Verify your DSA Arena account"
                : "Reset your DSA Arena password");
        message.setText("Your OTP code is: " + otpCode + "\nThis code expires in "
                + expiryMinutes + " minutes.\n\nIf you didn't request this, ignore this email.");

        mailSender.send(message);
    }
}