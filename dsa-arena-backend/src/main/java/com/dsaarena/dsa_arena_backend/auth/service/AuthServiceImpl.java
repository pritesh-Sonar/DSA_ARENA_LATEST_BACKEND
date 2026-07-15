package com.dsaarena.dsa_arena_backend.auth.service;

import com.dsaarena.dsa_arena_backend.auth.dto.request.LoginRequest;
import com.dsaarena.dsa_arena_backend.auth.dto.request.SendOtpRequest;
import com.dsaarena.dsa_arena_backend.auth.dto.request.VerifyOtpRequest;
import com.dsaarena.dsa_arena_backend.auth.dto.response.AuthResponse;
import com.dsaarena.dsa_arena_backend.auth.jwt.JwtUtil;
import com.dsaarena.dsa_arena_backend.enums.OtpPurpose;
import com.dsaarena.dsa_arena_backend.enums.Role;
import com.dsaarena.dsa_arena_backend.exception.BadRequestException;
import com.dsaarena.dsa_arena_backend.otp.entity.OtpVerification;
import com.dsaarena.dsa_arena_backend.otp.service.OtpService;
import com.dsaarena.dsa_arena_backend.user.entity.User;
import com.dsaarena.dsa_arena_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.dsaarena.dsa_arena_backend.auth.dto.request.ForgotPasswordRequest;
import com.dsaarena.dsa_arena_backend.auth.dto.request.ResetPasswordRequest;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final AuthenticationManager authenticationManager;
    private final OtpService otpService;

    @Override
    public void sendSignupOtp(SendOtpRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new BadRequestException("Username already taken");
        }
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        String hashedPassword = passwordEncoder.encode(request.getPassword());
        otpService.generateAndSendSignupOtp(request.getEmail(), request.getUsername(), hashedPassword);
    }

    @Override
    public AuthResponse verifySignupOtp(VerifyOtpRequest request) {
        OtpVerification otp = otpService.verifyOtp(request.getEmail(), request.getOtpCode(), OtpPurpose.SIGNUP);

        // Re-check uniqueness here too — guards against a race where someone else
        // grabbed the same username/email while this OTP was pending
        if (userRepository.existsByUsername(otp.getPendingUsername())) {
            throw new BadRequestException("Username already taken");
        }
        if (userRepository.existsByEmail(otp.getIdentifier())) {
            throw new BadRequestException("Email already registered");
        }

        User user = User.builder()
                .username(otp.getPendingUsername())
                .email(otp.getIdentifier())
                .password(otp.getPendingPasswordHash())
                .role(Role.USER)
                .isVerified(true)
                .build();

        userRepository.save(user);

        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public AuthResponse login(LoginRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new BadRequestException("Invalid username or password"));

        if (!user.isVerified()) {
            throw new BadRequestException("Please verify your email before logging in");
        }

        String token = jwtUtil.generateToken(user);

        return AuthResponse.builder()
                .token(token)
                .username(user.getUsername())
                .role(user.getRole().name())
                .build();
    }

    @Override
    public void forgotPassword(ForgotPasswordRequest request) {
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("No account found with this email"));

        otpService.generateAndSendResetOtp(user.getEmail());
    }

    @Override
    public void resetPassword(ResetPasswordRequest request) {
        otpService.verifyOtp(request.getEmail(), request.getOtpCode(), OtpPurpose.RESET_PASSWORD);

        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new BadRequestException("No account found with this email"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
}