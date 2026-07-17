package com.dsaarena.dsa_arena_backend.auth.security;

import com.dsaarena.dsa_arena_backend.enums.AuthProvider;
import com.dsaarena.dsa_arena_backend.enums.Role;
import com.dsaarena.dsa_arena_backend.user.entity.User;
import com.dsaarena.dsa_arena_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest request) throws OAuth2AuthenticationException {
        OAuth2User oAuth2User = super.loadUser(request);

        String googleId = oAuth2User.getAttribute("sub");
        String email = oAuth2User.getAttribute("email");
        String name = oAuth2User.getAttribute("name");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Google account did not provide an email address");
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            // New Google user — create an account
            user = User.builder()
                    .username(generateUniqueUsername(name))
                    .email(email)
                    .provider(AuthProvider.GOOGLE)
                    .providerId(googleId)
                    .role(Role.USER)
                    .isVerified(true) // Google already verified this email
                    .build();
            System.out.println("Creating Google user: " + email);
            userRepository.save(user);
            System.out.println("Saved user with id = " + user.getId());
        }

        else if (user.getProvider() == AuthProvider.LOCAL) {
            // Existing local account with the same email — link it to Google
            user.setProvider(AuthProvider.GOOGLE);
            user.setProviderId(googleId);
            user.setVerified(true);
            userRepository.save(user);
        }

        return oAuth2User;
    }

    private String generateUniqueUsername(String name) {
        String base = (name != null ? name.replaceAll("\\s+", "").toLowerCase() : "user");
        String candidate = base;
        int suffix = 0;

        while (userRepository.existsByUsername(candidate)) {
            suffix++;
            candidate = base + suffix;
        }
        return candidate;
    }
}