package com.dsaarena.dsa_arena_backend.game.tictactoe.service;

import com.dsaarena.dsa_arena_backend.game.tictactoe.model.TicTacToeGame;
import com.dsaarena.dsa_arena_backend.game.tictactoe.util.TicTacToeRules;
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

        if (!game.getStatus().equals("PLAYING") || !game.getCurrentTurn().equals(symbol)) {
            return;
        }
        if (game.getBoard()[index] != null) {
            return;
        }

        game.makeMove(index, symbol);

        if (TicTacToeRules.checkWin(game.getBoard(), symbol)) {
            game.setStatus("WON");
            game.setWinner(symbol);
        } else if (TicTacToeRules.isBoardFull(game.getBoard())) {
            game.setStatus("DRAW");
        } else {
            game.setCurrentTurn(symbol.equals("X") ? "O" : "X");
        }

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

    public void handleRematch(String roomId, String username) {
        TicTacToeGame game = getGame(roomId);
        if (game == null) return;

        // If the opponent had previously declined and this player re-requests,
        // clear the stale decline flag so the UI doesn't show old state.
        if (game.isRematchDeclined()) {
            game.setRematchDeclined(false);
        }

        game.getRematchRequests().add(username);

        if (game.getRematchRequests().size() == 2) {
            game.resetGame();
        }

        messagingTemplate.convertAndSend("/topic/game/" + roomId, game);
    }

    // NEW: handle a player declining the rematch
    public void handleRematchDecline(String roomId, String username) {
        TicTacToeGame game = getGame(roomId);
        if (game == null) return;

        game.setRematchDeclined(true);

        System.out.println("❌ Player " + username + " declined rematch in room: " + roomId);

        messagingTemplate.convertAndSend("/topic/game/" + roomId, game);
    }
}