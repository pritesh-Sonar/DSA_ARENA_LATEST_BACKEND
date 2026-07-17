package com.dsaarena.dsa_arena_backend.game.matchmaking;

import com.dsaarena.dsa_arena_backend.game.tictactoe.service.TicTacToeService; // <-- Make sure this import is present
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;

@Service
public class MatchmakingService {

    private final SimpMessagingTemplate messagingTemplate;
    private final TicTacToeService ticTacToeService; // <-- The field causing the error

    // Thread-safe FIFO Queue to store usernames waiting for a game
    private final ConcurrentLinkedQueue<String> playerQueue = new ConcurrentLinkedQueue<>();

    // Updated constructor injecting both dependencies
    public MatchmakingService(SimpMessagingTemplate messagingTemplate, TicTacToeService ticTacToeService) {
        this.messagingTemplate = messagingTemplate;
        this.ticTacToeService = ticTacToeService; // <-- Properly initialized here
    }

    public synchronized void addToQueue(String username) {
        if (playerQueue.contains(username)) {
            return;
        }

        playerQueue.add(username);
        System.out.println("Player added to matchmaking queue: " + username + " | Queue Size: " + playerQueue.size());

        if (playerQueue.size() >= 2) {
            String player1 = playerQueue.poll();
            String player2 = playerQueue.poll();

            if (player1 != null && player2 != null) {
                initiateMatch(player1, player2);
            }
        }
    }

    public void removeFromQueue(String username) {
        playerQueue.remove(username);
        System.out.println("Player removed from queue: " + username);
    }

    private void initiateMatch(String p1, String p2) {
        String roomId = UUID.randomUUID().toString();
        System.out.println("Match found! Creating room: " + roomId + " between " + p1 + " (X) and " + p2 + " (O)");

        // Initialize the game state on the server
        ticTacToeService.initializeGame(roomId, p1, p2);

        // Notify Player 1 privately
        messagingTemplate.convertAndSendToUser(p1, "/queue/match", new MatchFoundResponse(roomId, p2, "X"));
        // Notify Player 2 privately
        messagingTemplate.convertAndSendToUser(p2, "/queue/match", new MatchFoundResponse(roomId, p1, "O"));
    }

    public record MatchFoundResponse(String roomId, String opponent, String playerSymbol) {}
}