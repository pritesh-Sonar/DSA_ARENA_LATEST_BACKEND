package com.dsaarena.dsa_arena_backend.common.ratelimit;

import com.dsaarena.dsa_arena_backend.common.response.ApiResponse;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimitFilter extends OncePerRequestFilter {

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, Bucket> bucketCache = new ConcurrentHashMap<>();

    private record RateRule(String path, int capacity, Duration refillPeriod) {}

    private static final List<RateRule> RULES = List.of(
            new RateRule("/api/auth/login", 5, Duration.ofMinutes(1)),
            new RateRule("/api/auth/send-otp", 3, Duration.ofMinutes(5)),
            new RateRule("/api/auth/verify-otp", 10, Duration.ofMinutes(5)),
            new RateRule("/api/auth/forgot-password", 3, Duration.ofMinutes(5)),
            new RateRule("/api/auth/reset-password", 5, Duration.ofMinutes(5))
    );

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {

        RateRule matchedRule = RULES.stream()
                .filter(rule -> request.getRequestURI().equals(rule.path()))
                .findFirst()
                .orElse(null);

        if (matchedRule == null) {
            filterChain.doFilter(request, response);
            return;
        }

        String clientKey = matchedRule.path() + ":" + resolveClientIp(request);
        Bucket bucket = bucketCache.computeIfAbsent(clientKey, k -> createBucket(matchedRule));

        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            sendTooManyRequests(response);
        }
    }

    private Bucket createBucket(RateRule rule) {
        Bandwidth limit = Bandwidth.classic(
                rule.capacity(),
                Refill.intervally(rule.capacity(), rule.refillPeriod())
        );
        return Bucket.builder().addLimit(limit).build();
    }

    private String resolveClientIp(HttpServletRequest request) {
        String forwardedFor = request.getHeader("X-Forwarded-For");
        if (forwardedFor != null && !forwardedFor.isBlank()) {
            return forwardedFor.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }

    private void sendTooManyRequests(HttpServletResponse response) throws IOException {
        response.setStatus(429); // HttpStatus.TOO_MANY_REQUESTS
        response.setContentType("application/json");
        ApiResponse<Void> body = ApiResponse.error("Too many requests. Please try again later.");
        response.getWriter().write(objectMapper.writeValueAsString(body));
    }
}