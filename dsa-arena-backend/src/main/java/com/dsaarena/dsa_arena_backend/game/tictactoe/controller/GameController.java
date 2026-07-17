package com.dsaarena.dsa_arena_backend.game.tictactoe.controller;

import com.dsaarena.dsa_arena_backend.game.tictactoe.dto.MoveRequest;
import com.dsaarena.dsa_arena_backend.game.tictactoe.service.TicTacToeService;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.stereotype.Controller;

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
}