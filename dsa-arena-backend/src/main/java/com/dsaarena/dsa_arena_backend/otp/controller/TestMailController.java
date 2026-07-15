package com.dsaarena.dsa_arena_backend.otp.controller;

import com.dsaarena.dsa_arena_backend.otp.service.OtpService;
import com.dsaarena.dsa_arena_backend.enums.OtpPurpose;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class TestMailController {

//    private final OtpService otpService;
//
//    @GetMapping("/api/test/send-otp")
//    public String testSendOtp(@RequestParam String email) {
//        otpService.generateAndSendOtp(email, OtpPurpose.SIGNUP);
//        return "OTP sent to " + email;
//    }
}