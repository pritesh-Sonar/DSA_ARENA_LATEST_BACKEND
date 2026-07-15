package com.dsaarena.dsa_arena_backend.auth.service;

import com.dsaarena.dsa_arena_backend.auth.dto.request.LoginRequest;
import com.dsaarena.dsa_arena_backend.auth.dto.request.SendOtpRequest;
import com.dsaarena.dsa_arena_backend.auth.dto.request.VerifyOtpRequest;
import com.dsaarena.dsa_arena_backend.auth.dto.response.AuthResponse;
import com.dsaarena.dsa_arena_backend.auth.dto.request.ForgotPasswordRequest;
import com.dsaarena.dsa_arena_backend.auth.dto.request.ResetPasswordRequest;

public interface AuthService {
    void sendSignupOtp(SendOtpRequest request);
    AuthResponse verifySignupOtp(VerifyOtpRequest request);
    AuthResponse login(LoginRequest request);
    void forgotPassword(ForgotPasswordRequest request);
    void resetPassword(ResetPasswordRequest request);
}