package com.dsaarena.dsa_arena_backend.auth.security;

import com.dsaarena.dsa_arena_backend.auth.jwt.JwtUtil;
import com.dsaarena.dsa_arena_backend.user.entity.User;
import com.dsaarena.dsa_arena_backend.user.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class OAuth2LoginSuccessHandler implements AuthenticationSuccessHandler {
    @Value("${frontend.url}")
    private static String frontendUrl;

    private final JwtUtil jwtUtil;
    private final UserRepository userRepository;

    private static final String FRONTEND_URL = frontendUrl;

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
                                        Authentication authentication) throws IOException {

        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found after OAuth login"));

        String token = jwtUtil.generateToken(user);

        String redirectUrl = FRONTEND_URL + "/oauth-success?token=" + token
                + "&username=" + user.getUsername()
                + "&role=" + user.getRole().name();

        response.sendRedirect(redirectUrl);
    }
}