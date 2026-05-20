package model;

import boardifier.model.GameStageModel;
import boardifier.model.Model;
import boardifier.model.StageElementsFactory;
import boardifier.model.TextElement;

/**
 * MerelleStageModel defines the complete state for a game of Merelle.
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

    public static final int PHASE_PLACEMENT = 0;
    public static final int PHASE_MOVEMENT  = 1;
    public static final int PHASE_CAPTURE   = 2;

    public static final int STATE_SELECT_PAWN = 0;
    public static final int STATE_SELECT_DEST = 1;


    private int phase;
    private int moveState;

    private int blackPawnsToPlace;
    private int whitePawnsToPlace;

    private int selectedPawnPos;

    private int phaseAfterCapture;


    private int[][] lastMillFormed;

    private int noCaptureTurns;

    public static final int DRAW_LIMIT = 50;


    private MerelleBoard board;
    private MerellePawnPot blackPot;
    private MerellePawnPot whitePot;
    private Pawn[] blackPawns;
    private Pawn[] whitePawns;
    private TextElement playerName;


    public MerelleStageModel(String name, Model model) {
        super(name, model);
        phase             = PHASE_PLACEMENT;
        moveState         = STATE_SELECT_PAWN;
        blackPawnsToPlace = 9;
        whitePawnsToPlace = 9;
        selectedPawnPos   = -1;
        phaseAfterCapture = PHASE_PLACEMENT;
        lastMillFormed    = new int[2][];
        noCaptureTurns    = 0;
        setupCallbacks();
    }


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


    public int[] getLastMillFormed(int playerIndex) {
        return lastMillFormed[playerIndex];
    }


    public void setBoard(MerelleBoard board) {
        this.board = board;
        addContainer(board);
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


    public void setPhase(int phase)             { this.phase = phase; }
    public void setMoveState(int state)         { this.moveState = state; }
    public void setSelectedPawnPos(int pos)     { this.selectedPawnPos = pos; }
    public void setPhaseAfterCapture(int phase) { this.phaseAfterCapture = phase; }

    public void setLastMillFormed(int playerIndex, int[] mill) {
        lastMillFormed[playerIndex] = mill;
    }

    public void incrementNoCaptureTurns()       { noCaptureTurns++; }
    public void resetNoCaptureTurns()           { noCaptureTurns = 0; }


    public void cancelSelection() {
        selectedPawnPos = -1;
        moveState       = STATE_SELECT_PAWN;
        board.resetReachableCells(false);
    }


    public boolean canFly(int color) {
        if (phase != PHASE_MOVEMENT) return false;
        return board.countPawns(color) == 3;
    }


    private void setupCallbacks() {
        onPutInContainer((element, container, row, col) -> {

            if (!(element instanceof Pawn)) return;
            if (container != board)         return;

            Pawn pawn = (Pawn) element;
            int posIndex = MerelleBoard.rowColToPos(row, col);
            if (posIndex == -1) return;

            if (phase == PHASE_PLACEMENT) {
                if (pawn.getColor() == Pawn.PAWN_BLACK) {
                    if (blackPawnsToPlace > 0) blackPawnsToPlace--;
                } else {
                    if (whitePawnsToPlace > 0) whitePawnsToPlace--;
                }
            }

            int playerIndex = (pawn.getColor() == Pawn.PAWN_BLACK) ? 0 : 1;
            int opponentColor = (pawn.getColor() == Pawn.PAWN_BLACK) ? Pawn.PAWN_WHITE : Pawn.PAWN_BLACK;

            if (board.isNewMill(posIndex, pawn.getColor(), lastMillFormed[playerIndex])) {
                lastMillFormed[playerIndex] = board.getFormedMill(posIndex, pawn.getColor());

                int opponentPawnsOnBoard = board.countPawns(opponentColor);
                if (opponentPawnsOnBoard > 0) {
                    phaseAfterCapture = (blackPawnsToPlace > 0 || whitePawnsToPlace > 0)
                            ? PHASE_PLACEMENT : PHASE_MOVEMENT;
                    phase = PHASE_CAPTURE;
                    return;
                }

            } else {
                lastMillFormed[playerIndex] = null;
            }

            if (phase == PHASE_PLACEMENT && blackPawnsToPlace == 0 && whitePawnsToPlace == 0) {
                phase     = PHASE_MOVEMENT;
                moveState = STATE_SELECT_PAWN;
            }

            checkEndConditions();
        });
    }

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

        int currentPlayer  = getModel().getIdPlayer();
        int opponentPlayer = 1 - currentPlayer;
        int opponentColor  = (opponentPlayer == 0) ? Pawn.PAWN_BLACK : Pawn.PAWN_WHITE;

        int opponentPawns = board.countPawns(opponentColor);

        int opponentPawnsToPlace = (opponentColor == Pawn.PAWN_BLACK) ? blackPawnsToPlace : whitePawnsToPlace;
        if (opponentPawnsToPlace == 0 && opponentPawns < 3) {
            getModel().setIdWinner(currentPlayer);
            getModel().stopStage();
            return;
        }

        if (phase == PHASE_MOVEMENT) {
            boolean opponentCanFly = (opponentPawns == 3);
            if (board.isBlocked(opponentColor, opponentCanFly)) {
                getModel().setIdWinner(currentPlayer);
                getModel().stopStage();
                return;
            }
        }

        if (noCaptureTurns >= DRAW_LIMIT) {
            getModel().setIdWinner(-1);
            getModel().stopStage();
        }
    }


    @Override
    public StageElementsFactory getDefaultElementFactory() {
        return new MerelleStageFactory(this);
    }
}