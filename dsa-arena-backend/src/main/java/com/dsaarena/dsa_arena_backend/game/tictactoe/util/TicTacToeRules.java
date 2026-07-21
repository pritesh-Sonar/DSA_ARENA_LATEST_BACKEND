package com.dsaarena.dsa_arena_backend.game.tictactoe.util;

public class TicTacToeRules {

    private static final int[][] WIN_PATTERNS = {
            {0, 1, 2}, {3, 4, 5}, {6, 7, 8},
            {0, 3, 6}, {1, 4, 7}, {2, 5, 8},
            {0, 4, 8}, {2, 4, 6}
    };

    public static boolean checkWin(String[] board, String symbol) {
        for (int[] pattern : WIN_PATTERNS) {
            if (symbol.equals(board[pattern[0]])
                    && symbol.equals(board[pattern[1]])
                    && symbol.equals(board[pattern[2]])) {
                return true;
            }
        }
        return false;
    }

    public static boolean isBoardFull(String[] board) {
        for (String cell : board) {
            if (cell == null) return false;
        }
        return true;
    }
}