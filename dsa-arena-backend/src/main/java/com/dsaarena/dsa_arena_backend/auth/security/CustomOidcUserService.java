package com.dsaarena.dsa_arena_backend.auth.security;

import com.dsaarena.dsa_arena_backend.enums.AuthProvider;
import com.dsaarena.dsa_arena_backend.enums.Role;
import com.dsaarena.dsa_arena_backend.user.entity.User;
import com.dsaarena.dsa_arena_backend.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserRequest;
import org.springframework.security.oauth2.client.oidc.userinfo.OidcUserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.oidc.user.OidcUser;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class CustomOidcUserService implements OAuth2UserService<OidcUserRequest, OidcUser> {

    private final UserRepository userRepository;
    private final OidcUserService delegate = new OidcUserService();

    @Override
    public OidcUser loadUser(OidcUserRequest request) throws OAuth2AuthenticationException {
        OidcUser oidcUser = delegate.loadUser(request);

        String googleId = oidcUser.getAttribute("sub");
        String email = oidcUser.getAttribute("email");
        String name = oidcUser.getAttribute("name");

        if (email == null || email.isBlank()) {
            throw new OAuth2AuthenticationException("Google account did not provide an email address");
        }

        User user = userRepository.findByEmail(email).orElse(null);

        if (user == null) {
            user = User.builder()
                    .username(generateUniqueUsername(name))
                    .email(email)
                    .provider(AuthProvider.GOOGLE)
                    .providerId(googleId)
                    .role(Role.USER)
                    .isVerified(true)
                    .build();
            userRepository.save(user);
        } else if (user.getProvider() == AuthProvider.LOCAL) {
            user.setProvider(AuthProvider.GOOGLE);
            user.setProviderId(googleId);
            user.setVerified(true);
            userRepository.save(user);
        }

        return oidcUser;
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