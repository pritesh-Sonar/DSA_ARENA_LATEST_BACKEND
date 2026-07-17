package com.dsaarena.dsa_arena_backend.game.tictactoe.service;

import com.dsaarena.dsa_arena_backend.game.tictactoe.model.TicTacToeGame;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class TicTacToeService {

    private final SimpMessagingTemplate messagingTemplate;
    private final ConcurrentHashMap<String, TicTacToeGame> activeGames = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, String> userToRoom = new ConcurrentHashMap<>();

    public TicTacToeService(SimpMessagingTemplate messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    public void initializeGame(String roomId, String playerX, String playerO) {
        TicTacToeGame game = new TicTacToeGame(roomId, playerX, playerO);
        activeGames.put(roomId, game);
        userToRoom.put(playerX, roomId);
        userToRoom.put(playerO, roomId);
    }

    public TicTacToeGame getGame(String roomId) {
        return activeGames.get(roomId);
    }

    public void processMove(String roomId, int index, String symbol) {
        TicTacToeGame game = getGame(roomId);
        if (game == null) return;

        // Validate turn and cell availability
        if (!game.getStatus().equals("PLAYING") || !game.getCurrentTurn().equals(symbol)) {
            return;
        }
        if (game.getBoard()[index] != null) {
            return;
        }

        // Apply move
        game.makeMove(index, symbol);

        // Check for Win or Draw
        if (checkWin(game.getBoard(), symbol)) {
            game.setStatus("WON");
            game.setWinner(symbol);
        } else if (isBoardFull(game.getBoard())) {
            game.setStatus("DRAW");
        } else {
            // Switch turn
            game.setCurrentTurn(symbol.equals("X") ? "O" : "X");
        }

        // Broadcast updated state to all players subscribed to this room
        messagingTemplate.convertAndSend("/topic/game/" + roomId, game);
    }

    public void handlePlayerDisconnect(String username) {
        if (username == null) return;

        String roomId = userToRoom.remove(username);
        if (roomId != null) {
            TicTacToeGame game = activeGames.get(roomId);
            if (game != null && "PLAYING".equals(game.getStatus())) {
                game.setStatus("WON");
                game.setWinReason("DISCONNECT");
                if (username.equals(game.getPlayerXUsername())) {
                    game.setWinner("O");
                } else {
                    game.setWinner("X");
                }

                System.out.println("⚠️ Player " + username + " disconnected. Forfeiting match in room: " + roomId);

                messagingTemplate.convertAndSend("/topic/game/" + roomId, game);

                userToRoom.remove(game.getPlayerXUsername());
                userToRoom.remove(game.getPlayerOUsername());
                activeGames.remove(roomId);
            }
        }
    }

    private boolean checkWin(String[] b, String s) {
        int[][] winPatterns = {
                {0, 1, 2}, {3, 4, 5}, {6, 7, 8}, // Rows
                {0, 3, 6}, {1, 4, 7}, {2, 5, 8}, // Columns
                {0, 4, 8}, {2, 4, 6}             // Diagonals
        };

        for (int[] p : winPatterns) {
            if (s.equals(b[p[0]]) && s.equals(b[p[1]]) && s.equals(b[p[2]])) {
                return true;
            }
        }
        return false;
    }

    private boolean isBoardFull(String[] b) {
        for (String cell : b) {
            if (cell == null) return false;
        }
        return true;
    }
}