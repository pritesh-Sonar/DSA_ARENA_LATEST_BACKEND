package com.dsaarena.dsa_arena_backend.game.websocket;

import com.dsaarena.dsa_arena_backend.auth.jwt.JwtUtil;
import com.dsaarena.dsa_arena_backend.auth.security.CustomUserDetailsService;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.messaging.support.MessageHeaderAccessor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE) // <-- Forces this to run first before Spring Security intercepts it
public class JwtChannelInterceptor implements ChannelInterceptor {

    private final JwtUtil jwtUtil;
    private final CustomUserDetailsService userDetailsService;

    public JwtChannelInterceptor(JwtUtil jwtUtil, CustomUserDetailsService userDetailsService) {
        this.jwtUtil = jwtUtil;
        this.userDetailsService = userDetailsService;
    }

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = MessageHeaderAccessor.getAccessor(message, StompHeaderAccessor.class);

        System.out.println("🕵️ Interceptor saw frame: " + (accessor != null ? accessor.getCommand() : "null"));

        if (accessor != null && StompCommand.CONNECT.equals(accessor.getCommand())) {
            System.out.println("📬 STOMP interceptor caught CONNECT frame.");

            String authHeader = accessor.getFirstNativeHeader("Authorization");
            if (authHeader != null && authHeader.startsWith("Bearer ")) {
                try {
                    String token = authHeader.substring(7);
                    String username = jwtUtil.extractUsername(token);
                    System.out.println("👤 Extracted username from STOMP token: " + username);

                    if (username != null && accessor.getUser() == null) {
                        UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                        if (jwtUtil.isTokenValid(token, userDetails)) {
                            UsernamePasswordAuthenticationToken authentication =
                                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());

                            accessor.setUser(authentication);
                            System.out.println("✅ STOMP Session successfully authenticated for user: " + username);
                        } else {
                            System.out.println("❌ STOMP Token validation failed for: " + username);
                        }
                    }
                } catch (Exception e) {
                    System.out.println("🚨 Exception caught during STOMP token authentication: " + e.getMessage());
                    e.printStackTrace();
                }
            } else {
                System.out.println("⚠️ Warning: CONNECT frame missing 'Authorization' native header.");
            }
        }
        return message;
    }
}