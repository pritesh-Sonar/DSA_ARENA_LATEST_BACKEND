package com.dsaarena.dsa_arena_backend.game.tictactoe.dto;

public class BotMoveResponse {
    private String[] board;
    private int moveIndex;
    private String status; // "PLAYING", "WON", "DRAW"
    private String winner;

    public BotMoveResponse(String[] board, int moveIndex, String status, String winner) {
        this.board = board;
        this.moveIndex = moveIndex;
        this.status = status;
        this.winner = winner;
    }

    public String[] getBoard() { return board; }
    public int getMoveIndex() { return moveIndex; }
    public String getStatus() { return status; }
    public String getWinner() { return winner; }
}