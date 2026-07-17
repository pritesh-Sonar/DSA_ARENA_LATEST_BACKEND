package com.dsaarena.dsa_arena_backend.game.websocket;

import com.dsaarena.dsa_arena_backend.game.tictactoe.service.TicTacToeService;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
public class WebSocketDisconnectListener {

    private final TicTacToeService ticTacToeService;

    public WebSocketDisconnectListener(TicTacToeService ticTacToeService) {
        this.ticTacToeService = ticTacToeService;
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        if (headerAccessor.getUser() != null) {
            String username = headerAccessor.getUser().getName();
            ticTacToeService.handlePlayerDisconnect(username);
        }
    }
}