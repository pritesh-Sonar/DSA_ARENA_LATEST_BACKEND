package com.dsaarena.dsa_arena_backend.game.tictactoe.model;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class TicTacToeGame {
    private String roomId;
    private String[] board;
    private String currentTurn;
    private String status; // "PLAYING", "WON", "DRAW"
    private String winner;
    private String playerXUsername;
    private String playerOUsername;
    private String winReason;
    private Set<String> rematchRequests = new HashSet<>();
    private boolean rematchDeclined = false; // NEW

    public TicTacToeGame(String roomId, String playerXUsername, String playerOUsername) {
        this.roomId = roomId;
        this.playerXUsername = playerXUsername;
        this.playerOUsername = playerOUsername;
        this.board = new String[9];
        Arrays.fill(this.board, null);
        this.currentTurn = "X";
        this.status = "PLAYING";
        this.winner = null;
    }

    // Getters and Setters
    public String getRoomId() { return roomId; }
    public String[] getBoard() { return board; }
    public String getCurrentTurn() { return currentTurn; }
    public void setCurrentTurn(String currentTurn) { this.currentTurn = currentTurn; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getWinner() { return winner; }
    public void setWinner(String winner) { this.winner = winner; }
    public String getPlayerXUsername() { return playerXUsername; }
    public String getPlayerOUsername() { return playerOUsername; }
    public String getWinReason() { return winReason; }
    public void setWinReason(String winReason) { this.winReason = winReason; }
    public Set<String> getRematchRequests() { return rematchRequests; }

    // NEW: boolean getter/setter for the decline flag.
    // "isRematchDeclined" follows Java bean convention for booleans;
    // Jackson still serializes it to JSON as "rematchDeclined".
    public boolean isRematchDeclined() { return rematchDeclined; }
    public void setRematchDeclined(boolean rematchDeclined) { this.rematchDeclined = rematchDeclined; }

    public void makeMove(int index, String symbol) {
        if (index >= 0 && index < 9 && board[index] == null) {
            board[index] = symbol;
        }
    }
    public void resetGame() {
        Arrays.fill(this.board, null);
        this.currentTurn = "X";
        this.status = "PLAYING";
        this.winner = null;
        this.winReason = null;
        this.rematchRequests.clear();
        this.rematchDeclined = false; // NEW: clear the flag on an actual reset
    }
}