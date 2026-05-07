package model;

import boardifier.model.GameStageModel;
import boardifier.model.Model;
import boardifier.model.StageElementsFactory;
import boardifier.model.TextElement;

/**
 * MerelleStageModel defines the complete state for a game of Merelle (Nine Men's Morris).
 *
 * RULES SUMMARY:
 *  - 2 players: BLACK (player index 0) and WHITE (player index 1)
 *  - Each player starts with 9 pawns in their pot
 *  - Phase 1 — PLACEMENT: players alternate placing one pawn per turn on any empty cell.
 *    Forming a mill (3 aligned same-colour pawns) lets the player capture one opponent pawn.
 *  - Phase 2 — MOVEMENT: players alternate moving one pawn one step along a line.
 *    Forming a mill again allows a capture.
 *    A player with exactly 3 pawns may "fly" (move to any empty cell).
 *  - Win: reduce the opponent to fewer than 3 pawns, or block all their pawns.
 *
 * PHASES (stored in field `phase`):
 *   PHASE_PLACEMENT — players place pawns from their pot onto the board
 *   PHASE_MOVEMENT  — players move pawns already on the board
 *   PHASE_CAPTURE   — after forming a mill, the current player captures one opponent pawn;
 *                     the phase then returns to PLACEMENT or MOVEMENT as appropriate.
 *
 * MOVEMENT SUB-STATES (stored in field `moveState`):
 *   STATE_SELECT_PAWN — player must select one of their pawns to move
 *   STATE_SELECT_DEST — player must select a valid destination cell
 */
public class MerelleStageModel extends GameStageModel {

    // ---- Phase constants ----
    public static final int PHASE_PLACEMENT = 0;
    public static final int PHASE_MOVEMENT  = 1;
    public static final int PHASE_CAPTURE   = 2;

    // ---- Movement sub-state constants ----
    public static final int STATE_SELECT_PAWN = 0;
    public static final int STATE_SELECT_DEST = 1;

    // -----------------------------------------------------------------------
    // State variables
    // -----------------------------------------------------------------------

    private int phase;
    private int moveState;

    /** Number of pawns each player still has to place (starts at 9, counts down to 0). */
    private int blackPawnsToPlace;
    private int whitePawnsToPlace;

    /** Position index (0..23) of the pawn selected for movement. -1 = none selected. */
    private int selectedPawnPos;

    /** Phase to return to after a capture is completed. */
    private int phaseAfterCapture;

    /**
     * Per-player last mill formed (index 0 = BLACK, index 1 = WHITE).
     * A player cannot break and immediately re-form the same mill on their next turn.
     */
    private int[][] lastMillFormed;

    /** Consecutive turns without a capture — used to detect a draw. */
    private int noCaptureTurns;

    /** Number of turns without capture after which a draw is declared. */
    public static final int DRAW_LIMIT = 50;

    // -----------------------------------------------------------------------
    // Game elements
    // -----------------------------------------------------------------------

    private MerelleBoard board;
    private MerellePawnPot blackPot;
    private MerellePawnPot whitePot;
    private Pawn[] blackPawns;
    private Pawn[] whitePawns;
    private TextElement playerName;

    // -----------------------------------------------------------------------
    // Constructor
    // -----------------------------------------------------------------------

    public MerelleStageModel(String name, Model model) {
        super(name, model);
        phase             = PHASE_PLACEMENT;
        moveState         = STATE_SELECT_PAWN;
        blackPawnsToPlace = 9;
        whitePawnsToPlace = 9;
        selectedPawnPos   = -1;
        phaseAfterCapture = PHASE_PLACEMENT;
        lastMillFormed    = new int[2][];  // [0]=BLACK, [1]=WHITE, both null initially
        noCaptureTurns    = 0;
        setupCallbacks();
    }

    // -----------------------------------------------------------------------
    // Getters
    // -----------------------------------------------------------------------

    public MerelleBoard getBoard()          { return board; }
    public MerellePawnPot getBlackPot()     { return blackPot; }
    public MerellePawnPot getWhitePot()     { return whitePot; }
    public Pawn[] getBlackPawns()           { return blackPawns; }
    public Pawn[] getWhitePawns()           { return whitePawns; }
    public TextElement getPlayerName()      { return playerName; }

    public int getPhase()                   { return phase; }
    public int getMoveState()               { return moveState; }
    public int getSelectedPawnPos()         { return selectedPawnPos; }
    public int getPhaseAfterCapture()       { return phaseAfterCapture; }
    public int getBlackPawnsToPlace()       { return blackPawnsToPlace; }
    public int getWhitePawnsToPlace()       { return whitePawnsToPlace; }
    public int getNoCaptureTurns()          { return noCaptureTurns; }

    /**
     * Returns the last mill formed by the given player (0=BLACK, 1=WHITE),
     * or null if no mill was formed on their previous turn.
     */
    public int[] getLastMillFormed(int playerIndex) {
        return lastMillFormed[playerIndex];
    }

    // -----------------------------------------------------------------------
    // Setters — register elements with boardifier
    // Confirmed pattern from PuissanceXStageModel:
    //   ContainerElement (board, pot) → addContainer()
    //   GameElement (pawn, text)      → addElement()
    // -----------------------------------------------------------------------

    public void setBoard(MerelleBoard board) {
        this.board = board;
        addContainer(board);   // ContainerElement → addContainer() (confirmed by PuissanceX)
    }

    public void setBlackPot(MerellePawnPot pot) {
        this.blackPot = pot;
        addContainer(pot);
    }

    public void setWhitePot(MerellePawnPot pot) {
        this.whitePot = pot;
        addContainer(pot);
    }

    public void setBlackPawns(Pawn[] pawns) {
        this.blackPawns = pawns;
        for (Pawn p : pawns) addElement(p);  // GameElement → addElement()
    }

    public void setWhitePawns(Pawn[] pawns) {
        this.whitePawns = pawns;
        for (Pawn p : pawns) addElement(p);
    }

    public void setPlayerName(TextElement text) {
        this.playerName = text;
        addElement(text);
    }

    // -----------------------------------------------------------------------
    // State modifiers
    // -----------------------------------------------------------------------

    public void setPhase(int phase)             { this.phase = phase; }
    public void setMoveState(int state)         { this.moveState = state; }
    public void setSelectedPawnPos(int pos)     { this.selectedPawnPos = pos; }
    public void setPhaseAfterCapture(int phase) { this.phaseAfterCapture = phase; }

    public void setLastMillFormed(int playerIndex, int[] mill) {
        lastMillFormed[playerIndex] = mill;
    }

    public void incrementNoCaptureTurns()       { noCaptureTurns++; }
    public void resetNoCaptureTurns()           { noCaptureTurns = 0; }

    /**
     * Cancels the current pawn selection during PHASE_MOVEMENT.
     * Resets board highlights and returns to STATE_SELECT_PAWN.
     */
    public void cancelSelection() {
        selectedPawnPos = -1;
        moveState       = STATE_SELECT_PAWN;
        board.resetReachableCells(false);
    }

    /**
     * Returns true if the player of given color is allowed to "fly"
     * (has exactly 3 pawns on the board, only meaningful during MOVEMENT).
     */
    public boolean canFly(int color) {
        if (phase != PHASE_MOVEMENT) return false;
        return board.countPawns(color) == 3;
    }

    // -----------------------------------------------------------------------
    // Callbacks
    // -----------------------------------------------------------------------

    private void setupCallbacks() {
        /*
         * Triggered by boardifier whenever a GameElement is placed into a ContainerElement.
         * We react only when a Pawn lands on the main board.
         *
         * Responsibilities:
         *  1. Decrement the pawn-to-place counter (done here, not in the controller,
         *     so the transition check below always sees the correct value).
         *  2. Detect new mill formation → switch to PHASE_CAPTURE.
         *  3. Detect end of placement phase → switch to PHASE_MOVEMENT.
         *  4. Check end-of-game conditions after a non-capturing placement/move.
         */
        onPutInContainer((element, container, row, col) -> {

            if (!(element instanceof Pawn)) return;
            if (container != board)         return;

            Pawn pawn = (Pawn) element;
            int posIndex = MerelleBoard.rowColToPos(row, col);
            if (posIndex == -1) return;

            // Decrement pawn-to-place counter inside the callback
            if (phase == PHASE_PLACEMENT) {
                if (pawn.getColor() == Pawn.PAWN_BLACK) {
                    if (blackPawnsToPlace > 0) blackPawnsToPlace--;
                } else {
                    if (whitePawnsToPlace > 0) whitePawnsToPlace--;
                }
            }

            int playerIndex = (pawn.getColor() == Pawn.PAWN_BLACK) ? 0 : 1;

            // Check for a new mill (per-player restriction)
            if (board.isNewMill(posIndex, pawn.getColor(), lastMillFormed[playerIndex])) {
                lastMillFormed[playerIndex] = board.getFormedMill(posIndex, pawn.getColor());
                phaseAfterCapture = (blackPawnsToPlace > 0 || whitePawnsToPlace > 0)
                        ? PHASE_PLACEMENT : PHASE_MOVEMENT;
                phase = PHASE_CAPTURE;
                return; // wait for the capture before checking end conditions
            }

            // No new mill: clear this player's mill memory
            lastMillFormed[playerIndex] = null;

            // Transition from placement to movement when all 18 pawns are placed
            if (phase == PHASE_PLACEMENT && blackPawnsToPlace == 0 && whitePawnsToPlace == 0) {
                phase     = PHASE_MOVEMENT;
                moveState = STATE_SELECT_PAWN;
            }

            checkEndConditions();
        });
    }

    // -----------------------------------------------------------------------
    // End-of-game logic
    // -----------------------------------------------------------------------

    /**
     * Checks whether the game is over.
     * Called by the callback after every non-capturing move,
     * and by the controller after every capture.
     *
     * Win conditions:
     *  (a) The opponent has fewer than 3 pawns on the board (placement phase over).
     *  (b) All opponent pawns are blocked and the opponent cannot fly.
     * Draw condition:
     *  (c) DRAW_LIMIT consecutive turns without a capture.
     */
    public void checkEndConditions() {
        if (blackPawnsToPlace > 0 || whitePawnsToPlace > 0) return;

        // Confirmed from PuissanceXStageModel: model is accessible via getModel()
        int currentPlayer  = getModel().getIdPlayer();
        int opponentPlayer = 1 - currentPlayer;
        int opponentColor  = (opponentPlayer == 0) ? Pawn.PAWN_BLACK : Pawn.PAWN_WHITE;

        int opponentPawns = board.countPawns(opponentColor);

        // Win (a): fewer than 3 pawns
        if (opponentPawns < 3) {
            getModel().setIdWinner(currentPlayer);
            getModel().stopStage();
            return;
        }

        // Win (b): all pawns blocked
        boolean opponentCanFly = (opponentPawns == 3);
        if (board.isBlocked(opponentColor, opponentCanFly)) {
            getModel().setIdWinner(currentPlayer);
            getModel().stopStage();
            return;
        }

        // Draw (c): too many turns without a capture
        if (noCaptureTurns >= DRAW_LIMIT) {
            getModel().setIdWinner(-1); // -1 = draw
            getModel().stopStage();
        }
    }

    // -----------------------------------------------------------------------
    // Factory
    // -----------------------------------------------------------------------

    @Override
    public StageElementsFactory getDefaultElementFactory() {
        return new MerelleStageFactory(this);
    }
}