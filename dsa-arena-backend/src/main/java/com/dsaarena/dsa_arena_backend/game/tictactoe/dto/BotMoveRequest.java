package com.dsaarena.dsa_arena_backend.game.tictactoe.dto;

import com.dsaarena.dsa_arena_backend.enums.BotDifficulty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class BotMoveRequest {

    @NotNull
    @Size(min = 9, max = 9, message = "Board must have exactly 9 cells")
    private String[] board;

    @NotNull
    private String botSymbol;

    @NotNull
    private BotDifficulty difficulty;

    public String[] getBoard() { return board; }
    public void setBoard(String[] board) { this.board = board; }
    public String getBotSymbol() { return botSymbol; }
    public void setBotSymbol(String botSymbol) { this.botSymbol = botSymbol; }
    public BotDifficulty getDifficulty() { return difficulty; }
    public void setDifficulty(BotDifficulty difficulty) { this.difficulty = difficulty; }
}