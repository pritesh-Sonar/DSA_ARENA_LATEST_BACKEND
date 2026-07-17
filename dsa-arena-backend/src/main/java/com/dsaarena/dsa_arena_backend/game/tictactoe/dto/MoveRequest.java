package com.dsaarena.dsa_arena_backend.game.tictactoe.dto;

public class MoveRequest {
    private String roomId;
    private int index;
    private String symbol;

    // Getters and Setters
    public String getRoomId() { return roomId; }
    public void setRoomId(String roomId) { this.roomId = roomId; }
    public int getIndex() { return index; }
    public void setIndex(int index) { this.index = index; }
    public String getSymbol() { return symbol; }
    public void setSymbol(String symbol) { this.symbol = symbol; }
}