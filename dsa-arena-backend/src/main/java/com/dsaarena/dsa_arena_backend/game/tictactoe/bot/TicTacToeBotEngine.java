package com.dsaarena.dsa_arena_backend.game.tictactoe.bot;

import com.dsaarena.dsa_arena_backend.enums.BotDifficulty;
import com.dsaarena.dsa_arena_backend.game.tictactoe.util.TicTacToeRules;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Component
public class TicTacToeBotEngine {

    private final Random random = new Random();

    public int decideMove(String[] board, String botSymbol, BotDifficulty difficulty) {
        return switch (difficulty) {
            case EASY -> randomMove(board);
            case MEDIUM -> heuristicMove(board, botSymbol);
            case HARD -> minimaxMove(board, botSymbol);
        };
    }

    // ---------- EASY: pure random ----------
    private int randomMove(String[] board) {
        List<Integer> available = availableIndices(board);
        return available.get(random.nextInt(available.size()));
    }

    // ---------- MEDIUM: one-ply lookahead (win > block > random) ----------
    private int heuristicMove(String[] board, String botSymbol) {
        String opponentSymbol = opponentOf(botSymbol);

        // 1. Can the bot win right now?
        Integer winningMove = findImmediateWin(board, botSymbol);
        if (winningMove != null) return winningMove;

        // 2. Can the opponent win next turn? Block it.
        Integer blockingMove = findImmediateWin(board, opponentSymbol);
        if (blockingMove != null) return blockingMove;

        // 3. No immediate tactic — play randomly.
        return randomMove(board);
    }

    private Integer findImmediateWin(String[] board, String symbol) {
        for (int index : availableIndices(board)) {
            board[index] = symbol;
            boolean wins = TicTacToeRules.checkWin(board, symbol);
            board[index] = null; // undo — this is a probe, not a real move
            if (wins) return index;
        }
        return null;
    }

    // ---------- HARD: Minimax with alpha-beta pruning ----------
    private int minimaxMove(String[] board, String botSymbol) {
        String opponentSymbol = opponentOf(botSymbol);
        int bestScore = Integer.MIN_VALUE;
        int bestMove = -1;

        for (int index : availableIndices(board)) {
            board[index] = botSymbol;
            int score = minimax(board, 0, false, botSymbol, opponentSymbol,
                    Integer.MIN_VALUE, Integer.MAX_VALUE);
            board[index] = null;

            if (score > bestScore) {
                bestScore = score;
                bestMove = index;
            }
        }
        return bestMove;
    }

    /**
     * Standard minimax with alpha-beta pruning.
     * Score is biased by depth so the bot prefers the FASTEST win
     * and the SLOWEST loss, not just any win/loss.
     */
    private int minimax(String[] board, int depth, boolean isMaximizing,
                        String botSymbol, String opponentSymbol,
                        int alpha, int beta) {

        if (TicTacToeRules.checkWin(board, botSymbol)) return 10 - depth;
        if (TicTacToeRules.checkWin(board, opponentSymbol)) return depth - 10;
        if (TicTacToeRules.isBoardFull(board)) return 0;

        if (isMaximizing) {
            int best = Integer.MIN_VALUE;
            for (int index : availableIndices(board)) {
                board[index] = botSymbol;
                best = Math.max(best, minimax(board, depth + 1, false,
                        botSymbol, opponentSymbol, alpha, beta));
                board[index] = null;

                alpha = Math.max(alpha, best);
                if (beta <= alpha) break; // prune
            }
            return best;
        } else {
            int best = Integer.MAX_VALUE;
            for (int index : availableIndices(board)) {
                board[index] = opponentSymbol;
                best = Math.min(best, minimax(board, depth + 1, true,
                        botSymbol, opponentSymbol, alpha, beta));
                board[index] = null;

                beta = Math.min(beta, best);
                if (beta <= alpha) break; // prune
            }
            return best;
        }
    }

    private List<Integer> availableIndices(String[] board) {
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < board.length; i++) {
            if (board[i] == null) indices.add(i);
        }
        return indices;
    }

    private String opponentOf(String symbol) {
        return symbol.equals("X") ? "O" : "X";
    }
}