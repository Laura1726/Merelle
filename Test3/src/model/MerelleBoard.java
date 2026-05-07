package model;

import boardifier.model.ContainerElement;
import boardifier.model.ElementTypes;
import boardifier.model.GameStageModel;

import java.util.ArrayList;
import java.util.List;

/**
 * MerelleBoard represents the Nine Men's Morris game board.
 *
 * The board is modelled as a 7x7 ContainerElement, but only 24 specific
 * intersections are valid positions. The layout in the 7x7 grid is:
 *
 *   col: 0    1    2    3    4    5    6
 * row 0: [0]            [1]            [2]
 * row 1:      [3]       [4]       [5]
 * row 2:           [6]  [7]  [8]
 * row 3: [9] [10] [11]      [12] [13] [14]
 * row 4:           [15] [16] [17]
 * row 5:      [18]      [19]      [20]
 * row 6: [21]           [22]           [23]
 *
 * Position indices 0..23 map to (row, col) via POS_TO_ROWCOL.
 * Adjacency and mill definitions follow the standard Nine Men's Morris rules.
 */
public class MerelleBoard extends ContainerElement {

    public static final String BOARD_NAME = "merelboard";
    private static final int BOARD_TYPE_ID = 52;

    // Register element type once at class-loading time (same pattern as PuissanceXBoard)
    static {
        ElementTypes.register(BOARD_NAME, BOARD_TYPE_ID);
    }

    // -----------------------------------------------------------------------
    // Static board geometry
    // -----------------------------------------------------------------------

    /** Maps position index (0..23) to [row, col] in the 7x7 grid. */
    public static final int[][] POS_TO_ROWCOL = {
            {0,0}, {0,3}, {0,6},   //  0  1  2  outer top row
            {1,1}, {1,3}, {1,5},   //  3  4  5  middle top row
            {2,2}, {2,3}, {2,4},   //  6  7  8  inner top row
            {3,0}, {3,1}, {3,2},   //  9 10 11  left column
            {3,4}, {3,5}, {3,6},   // 12 13 14  right column
            {4,2}, {4,3}, {4,4},   // 15 16 17  inner bottom row
            {5,1}, {5,3}, {5,5},   // 18 19 20  middle bottom row
            {6,0}, {6,3}, {6,6}    // 21 22 23  outer bottom row
    };

    /** Adjacency list: directly connected neighbours for each position. */
    public static final int[][] ADJACENCY = {
            {1, 9},           //  0
            {0, 2, 4},        //  1
            {1, 14},          //  2
            {4, 10},          //  3
            {1, 3, 5, 7},     //  4
            {4, 13},          //  5
            {7, 11},          //  6
            {4, 6, 8, 16},    //  7
            {7, 12},          //  8
            {0, 10, 21},      //  9
            {3, 9, 11, 18},   // 10
            {6, 10, 15},      // 11
            {8, 13, 17},      // 12
            {5, 12, 14, 20},  // 13
            {2, 13, 23},      // 14
            {11, 16},         // 15
            {7, 15, 17, 19},  // 16
            {12, 16},         // 17
            {10, 19},         // 18
            {16, 18, 20, 22}, // 19
            {13, 19},         // 20
            {9, 22},          // 21
            {19, 21, 23},     // 22
            {14, 22}          // 23
    };

    /** All 16 possible mills (aligned triples of position indices). */
    public static final int[][] MILLS = {
            // Horizontal lines
            {0, 1, 2},
            {3, 4, 5},
            {6, 7, 8},
            {9, 10, 11},
            {12, 13, 14},
            {15, 16, 17},
            {18, 19, 20},
            {21, 22, 23},
            // Vertical lines
            {0, 9, 21},
            {3, 10, 18},
            {6, 11, 15},
            {1, 4, 7},
            {16, 19, 22},
            {8, 12, 17},
            {5, 13, 20},
            {2, 14, 23}
    };

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public MerelleBoard(int x, int y, GameStageModel gameStageModel) {
        super(BOARD_NAME, x, y, 7, 7, gameStageModel);
        this.type = ElementTypes.getType(BOARD_NAME);
    }

    // -----------------------------------------------------------------------
    // Position conversion utilities
    // -----------------------------------------------------------------------

    /** Converts a position index (0..23) to [row, col] in the 7x7 grid. */
    public static int[] posToRowCol(int posIndex) {
        return POS_TO_ROWCOL[posIndex];
    }

    /**
     * Converts a (row, col) in the 7x7 grid to a position index (0..23).
     * Returns -1 if the cell is not a valid board position.
     */
    public static int rowColToPos(int row, int col) {
        for (int i = 0; i < 24; i++) {
            if (POS_TO_ROWCOL[i][0] == row && POS_TO_ROWCOL[i][1] == col) return i;
        }
        return -1;
    }

    /** Returns true if (row, col) is a valid board position. */
    public static boolean isValidPosition(int row, int col) {
        return rowColToPos(row, col) != -1;
    }

    // -----------------------------------------------------------------------
    // Pawn access helpers
    // -----------------------------------------------------------------------

    /** Returns the Pawn at position index posIndex, or null if empty. */
    public Pawn getPawnAt(int posIndex) {
        int[] rc = POS_TO_ROWCOL[posIndex];
        if (isEmptyAt(rc[0], rc[1])) return null;
        return (Pawn) getElement(rc[0], rc[1]);
    }

    /** Returns true if the position at posIndex is empty. */
    public boolean isEmptyAtPos(int posIndex) {
        int[] rc = POS_TO_ROWCOL[posIndex];
        return isEmptyAt(rc[0], rc[1]);
    }

    // -----------------------------------------------------------------------
    // reachableCells setters (called by controller to highlight valid moves)
    // -----------------------------------------------------------------------

    /** PLACEMENT PHASE: marks all empty board positions as reachable. */
    public void setValidCellsForPlacement() {
        resetReachableCells(false);
        for (int i = 0; i < 24; i++) {
            if (isEmptyAtPos(i)) {
                int[] rc = POS_TO_ROWCOL[i];
                reachableCells[rc[0]][rc[1]] = true;
            }
        }
    }

    /**
     * MOVEMENT PHASE — select source:
     * marks all pawns of playerColor that have at least one valid move (or can fly).
     */
    public void setValidCellsForSelection(int playerColor, boolean canFly) {
        resetReachableCells(false);
        for (int i = 0; i < 24; i++) {
            Pawn p = getPawnAt(i);
            if (p != null && p.getColor() == playerColor) {
                if (canFly || hasValidMove(i)) {
                    int[] rc = POS_TO_ROWCOL[i];
                    reachableCells[rc[0]][rc[1]] = true;
                }
            }
        }
    }

    /**
     * MOVEMENT PHASE — select destination:
     * marks valid destinations for the pawn at fromPos.
     * If canFly, all empty cells are valid destinations.
     */
    public void setValidCellsForMove(int fromPos, boolean canFly) {
        resetReachableCells(false);
        if (canFly) {
            for (int i = 0; i < 24; i++) {
                if (isEmptyAtPos(i)) {
                    int[] rc = POS_TO_ROWCOL[i];
                    reachableCells[rc[0]][rc[1]] = true;
                }
            }
        } else {
            for (int neighbour : ADJACENCY[fromPos]) {
                if (isEmptyAtPos(neighbour)) {
                    int[] rc = POS_TO_ROWCOL[neighbour];
                    reachableCells[rc[0]][rc[1]] = true;
                }
            }
        }
    }

    /**
     * CAPTURE PHASE: marks opponent pawns that can be captured.
     * Pawns inside a mill are protected unless ALL opponent pawns are in mills,
     * in which case any opponent pawn may be captured.
     */
    public void setValidCellsForCapture(int opponentColor) {
        resetReachableCells(false);
        List<Integer> notInMill = new ArrayList<>();
        List<Integer> inMill    = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            Pawn p = getPawnAt(i);
            if (p != null && p.getColor() == opponentColor) {
                if (isInMill(i)) inMill.add(i);
                else              notInMill.add(i);
            }
        }
        List<Integer> capturable = notInMill.isEmpty() ? inMill : notInMill;
        for (int idx : capturable) {
            int[] rc = POS_TO_ROWCOL[idx];
            reachableCells[rc[0]][rc[1]] = true;
        }
    }

    // -----------------------------------------------------------------------
    // Game-logic helpers
    // -----------------------------------------------------------------------

    /** Returns true if the pawn at posIndex has at least one adjacent empty cell. */
    public boolean hasValidMove(int posIndex) {
        for (int neighbour : ADJACENCY[posIndex]) {
            if (isEmptyAtPos(neighbour)) return true;
        }
        return false;
    }

    /** Returns true if the pawn at posIndex is part of a completed mill. */
    public boolean isInMill(int posIndex) {
        Pawn p = getPawnAt(posIndex);
        if (p == null) return false;
        int color = p.getColor();
        for (int[] mill : MILLS) {
            boolean containsPos = false;
            for (int pos : mill) {
                if (pos == posIndex) { containsPos = true; break; }
            }
            if (!containsPos) continue;
            boolean allSame = true;
            for (int pos : mill) {
                Pawn mp = getPawnAt(pos);
                if (mp == null || mp.getColor() != color) { allSame = false; break; }
            }
            if (allSame) return true;
        }
        return false;
    }

    /**
     * Returns the 3 position indices of the mill completed by placing a pawn
     * of the given color at posIndex, or null if no mill would be formed.
     */
    public int[] getFormedMill(int posIndex, int color) {
        for (int[] mill : MILLS) {
            boolean containsPos = false;
            for (int pos : mill) {
                if (pos == posIndex) { containsPos = true; break; }
            }
            if (!containsPos) continue;
            boolean allSame = true;
            for (int pos : mill) {
                if (pos == posIndex) continue;
                Pawn mp = getPawnAt(pos);
                if (mp == null || mp.getColor() != color) { allSame = false; break; }
            }
            if (allSame) return mill;
        }
        return null;
    }

    /**
     * Returns true if placing/moving a pawn of the given color to posIndex forms
     * a mill DIFFERENT from lastMill (the mill this player formed last turn).
     * Enforces the rule: a player cannot break and immediately re-form the same mill.
     *
     * @param posIndex  destination position
     * @param color     color of the pawn being placed/moved
     * @param lastMill  the mill this player formed last turn, or null if none
     */
    public boolean isNewMill(int posIndex, int color, int[] lastMill) {
        int[] formedMill = getFormedMill(posIndex, color);
        if (formedMill == null) return false;
        if (lastMill == null)   return true;
        int matches = 0;
        for (int a : formedMill) {
            for (int b : lastMill) {
                if (a == b) { matches++; break; }
            }
        }
        return matches < 3; // true = different mill = allowed
    }

    /**
     * Returns true if placing/moving a pawn of the given color to posIndex
     * would complete any mill (ignoring the same-mill restriction).
     */
    public boolean formsMill(int posIndex, int color) {
        return getFormedMill(posIndex, color) != null;
    }

    /** Counts the number of pawns of the given color currently on the board. */
    public int countPawns(int color) {
        int count = 0;
        for (int i = 0; i < 24; i++) {
            Pawn p = getPawnAt(i);
            if (p != null && p.getColor() == color) count++;
        }
        return count;
    }

    /**
     * Returns true if every pawn of the given color is blocked (no adjacent empty cell).
     * If canFly is true, the player can move anywhere so they are never blocked.
     */
    public boolean isBlocked(int color, boolean canFly) {
        if (canFly) return false;
        for (int i = 0; i < 24; i++) {
            Pawn p = getPawnAt(i);
            if (p != null && p.getColor() == color && hasValidMove(i)) return false;
        }
        return true;
    }
}