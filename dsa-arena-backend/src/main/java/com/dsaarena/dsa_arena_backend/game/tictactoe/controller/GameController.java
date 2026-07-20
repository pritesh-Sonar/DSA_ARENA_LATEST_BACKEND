package com.dsaarena.dsa_arena_backend.game.tictactoe.controller;

import com.dsaarena.dsa_arena_backend.game.tictactoe.dto.MoveRequest;
import com.dsaarena.dsa_arena_backend.game.tictactoe.service.TicTacToeService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
public class GameController {

    private final TicTacToeService ticTacToeService;

    public GameController(TicTacToeService ticTacToeService) {
        this.ticTacToeService = ticTacToeService;
    }

    @MessageMapping("/game.move")
    public void handleMove(MoveRequest request) {
        System.out.println("🎯 Move received for Room: " + request.getRoomId() + " at index " + request.getIndex() + " by " + request.getSymbol());
        ticTacToeService.processMove(request.getRoomId(), request.getIndex(), request.getSymbol());
    }

    @MessageMapping("/game.rematch")
    public void handleRematch(MoveRequest request, Principal principal) {
        if (principal == null) return;
        ticTacToeService.handleRematch(request.getRoomId(), principal.getName());
    }

    // NEW
    @MessageMapping("/game.rematch.decline")
    public void handleRematchDecline(MoveRequest request, Principal principal) {
        if (principal == null) return;
        ticTacToeService.handleRematchDecline(request.getRoomId(), principal.getName());
    }
}