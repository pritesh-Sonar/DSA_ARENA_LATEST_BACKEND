package com.dsaarena.dsa_arena_backend.otp.repository;

import com.dsaarena.dsa_arena_backend.enums.OtpPurpose;
import com.dsaarena.dsa_arena_backend.otp.entity.OtpVerification;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface OtpRepository extends JpaRepository<OtpVerification, Long> {
    Optional<OtpVerification> findTopByIdentifierAndPurposeAndUsedFalseOrderByIdDesc(
            String identifier, OtpPurpose purpose
    );
}