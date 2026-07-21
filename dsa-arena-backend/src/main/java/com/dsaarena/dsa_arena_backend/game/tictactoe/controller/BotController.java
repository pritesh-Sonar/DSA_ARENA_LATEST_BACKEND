package com.dsaarena.dsa_arena_backend.game.tictactoe.controller;

import com.dsaarena.dsa_arena_backend.game.tictactoe.bot.TicTacToeBotEngine;
import com.dsaarena.dsa_arena_backend.game.tictactoe.dto.BotMoveRequest;
import com.dsaarena.dsa_arena_backend.game.tictactoe.dto.BotMoveResponse;
import com.dsaarena.dsa_arena_backend.game.tictactoe.util.TicTacToeRules;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/game/tictactoe")
public class BotController {

    private final TicTacToeBotEngine botEngine;

    public BotController(TicTacToeBotEngine botEngine) {
        this.botEngine = botEngine;
    }

    @PostMapping("/bot-move")
    public BotMoveResponse getBotMove(@Valid @RequestBody BotMoveRequest request) {
        String[] board = request.getBoard();
        String botSymbol = request.getBotSymbol();

        int moveIndex = botEngine.decideMove(board, botSymbol, request.getDifficulty());
        board[moveIndex] = botSymbol;

        String status = "PLAYING";
        String winner = null;

        if (TicTacToeRules.checkWin(board, botSymbol)) {
            status = "WON";
            winner = botSymbol;
        } else if (TicTacToeRules.isBoardFull(board)) {
            status = "DRAW";
        }

        return new BotMoveResponse(board, moveIndex, status, winner);
    }
}