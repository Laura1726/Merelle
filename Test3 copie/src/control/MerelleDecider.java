package control;

import boardifier.control.ActionFactory;
import boardifier.control.Controller;
import boardifier.control.Decider;
import boardifier.model.Model;
import boardifier.model.action.ActionList;
import model.MerelleBoard;
import model.MerelleStageModel;
import model.MerellePawnPot;
import model.Pawn;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Random;

/**
 * MerelleDecider handles all computer decisions for the Merelle game.
 *
 * TWO STRATEGIES:
 *
 *   STRATEGY_RANDOM (0) — picks a completely random valid move every turn.
 *
 *   STRATEGY_SMART  (1) — scores every possible move and picks the best:
 *       1. Win immediately (form a mill that would end the game)
 *       2. Block opponent from winning next turn
 *       3. Form any new mill (triggers a capture)
 *       4. Maximise alignment score, minimise opponent's
 *
 * Handles all three phases: PLACEMENT, MOVEMENT, CAPTURE.
 */
public class MerelleDecider extends Decider {

    public static final int STRATEGY_RANDOM = 0;
    public static final int STRATEGY_SMART  = 1;

    private final int strategy;
    private static final Random rng = new Random(Calendar.getInstance().getTimeInMillis());

    public MerelleDecider(Model model, Controller control) {
        this(model, control, STRATEGY_SMART);
    }

    public MerelleDecider(Model model, Controller control, int strategy) {
        super(model, control);
        this.strategy = strategy;
    }


    @Override
    public ActionList decide() {
        MerelleStageModel stageModel = (MerelleStageModel) model.getGameStage();
        switch (stageModel.getPhase()) {
            case MerelleStageModel.PHASE_PLACEMENT: return decidePlacement(stageModel);
            case MerelleStageModel.PHASE_MOVEMENT:  return decideMovement(stageModel);
            case MerelleStageModel.PHASE_CAPTURE:   return decideCapture(stageModel);
            default: return null;
        }
    }


    private ActionList decidePlacement(MerelleStageModel stageModel) {
        int currentColor = (model.getIdPlayer() == 0) ? Pawn.PAWN_BLACK : Pawn.PAWN_WHITE;
        MerelleBoard board = stageModel.getBoard();

        MerellePawnPot pot = (model.getIdPlayer() == 0)
                ? stageModel.getBlackPot()
                : stageModel.getWhitePot();

        Pawn pawn = null;
        for (int col = 0; col < 9; col++) {
            if (!pot.isEmptyAt(0, col)) {
                pawn = (Pawn) pot.getElement(0, col);
                break;
            }
        }
        if (pawn == null) return null;

        List<Integer> emptyPositions = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            if (board.isEmptyAtPos(i)) emptyPositions.add(i);
        }
        if (emptyPositions.isEmpty()) return null;

        int chosenPos = (strategy == STRATEGY_RANDOM)
                ? emptyPositions.get(rng.nextInt(emptyPositions.size()))
                : chooseBestPlacement(board, emptyPositions, currentColor, 1 - currentColor, stageModel);


        int[] rc = MerelleBoard.posToRowCol(chosenPos);
        ActionList actions = ActionFactory.generatePutInContainer(control, model, pawn, MerelleBoard.BOARD_NAME, rc[0], rc[1]);

        return actions;
    }

    private int chooseBestPlacement(MerelleBoard board, List<Integer> candidates, int myColor, int opponentColor, MerelleStageModel stageModel) {
        int bestScore = Integer.MIN_VALUE;
        int bestPos = candidates.get(0);
        int myIdx = (myColor == Pawn.PAWN_BLACK) ? 0 : 1;
        int[] myLastMill = stageModel.getLastMillFormed(myIdx);
        int[] opLastMill = stageModel.getLastMillFormed(1 - myIdx);

        for (int pos : candidates) {
            int score = 0;

            if (board.isNewMill(pos, myColor, myLastMill)) {
                score += 100;

                if (board.countPawns(opponentColor) <= 3) score += 1000;
            }

            if (board.isNewMill(pos, opponentColor, opLastMill)) score += 500;

            score += alignmentScore(board, pos, myColor) * 2;
            score -= alignmentScore(board, pos, opponentColor);
            if (score > bestScore) { bestScore = score; bestPos = pos; }
        }
        return bestPos;
    }


    private ActionList decideMovement(MerelleStageModel stageModel) {
        int currentColor = (model.getIdPlayer() == 0) ? Pawn.PAWN_BLACK : Pawn.PAWN_WHITE;
        int opponentColor = 1 - currentColor;
        MerelleBoard board = stageModel.getBoard();
        boolean canFly = stageModel.canFly(currentColor);

        List<int[]> moves = new ArrayList<>();
        for (int from = 0; from < 24; from++) {
            Pawn p = board.getPawnAt(from);
            if (p == null || p.getColor() != currentColor) continue;
            if (canFly) {
                for (int dest = 0; dest < 24; dest++) {
                    if (board.isEmptyAtPos(dest)) moves.add(new int[]{from, dest});
                }
            } else {
                for (int neighbour : MerelleBoard.ADJACENCY[from]) {
                    if (board.isEmptyAtPos(neighbour)) moves.add(new int[]{from, neighbour});
                }
            }
        }
        if (moves.isEmpty()) return null;

        int[] chosen = (strategy == STRATEGY_RANDOM) ? moves.get(rng.nextInt(moves.size())) : chooseBestMove(board, moves, currentColor, opponentColor, stageModel);

        stageModel.incrementNoCaptureTurns();

        int[] destRc = MerelleBoard.posToRowCol(chosen[1]);
        Pawn pawn = board.getPawnAt(chosen[0]);
        if (pawn == null) return null;

        ActionList actions = ActionFactory.generatePutInContainer(control, model, pawn, MerelleBoard.BOARD_NAME, destRc[0], destRc[1]);

        return actions;
    }

    private int[] chooseBestMove(MerelleBoard board, List<int[]> moves,
                                 int myColor, int opponentColor,
                                 MerelleStageModel stageModel) {
        int bestScore = Integer.MIN_VALUE;
        List<int[]> bestMoves = new ArrayList<>();
        int myIdx = (myColor == Pawn.PAWN_BLACK) ? 0 : 1;
        int[] myLastMill = stageModel.getLastMillFormed(myIdx);
        int[] opLastMill = stageModel.getLastMillFormed(1 - myIdx);

        for (int[] move : moves) {
            int from = move[0];
            int dest = move[1];
            int score = 0;

            if (board.isNewMill(dest, myColor, myLastMill)) {
                score += 100;
                if (board.countPawns(opponentColor) <= 3) score += 1000;
            }

            if (board.isNewMill(dest, opponentColor, opLastMill)) score += 50;

            score += alignmentScore(board, dest, myColor) * 2;
            score -= alignmentScore(board, dest, opponentColor);

            score -= alignmentScore(board, from, myColor);

            score += rng.nextInt(5);

            if (score > bestScore) {
                bestScore = score;
                bestMoves.clear();
                bestMoves.add(move);
            } else if (score == bestScore) {
                bestMoves.add(move);
            }
        }
        return bestMoves.get(rng.nextInt(bestMoves.size()));
    }


    private ActionList decideCapture(MerelleStageModel stageModel) {
        int opponentColor = (model.getIdPlayer() == 0) ? Pawn.PAWN_WHITE : Pawn.PAWN_BLACK;
        MerelleBoard board = stageModel.getBoard();

        List<Integer> notInMill = new ArrayList<>();
        List<Integer> inMill    = new ArrayList<>();
        for (int i = 0; i < 24; i++) {
            Pawn p = board.getPawnAt(i);
            if (p != null && p.getColor() == opponentColor) {
                if (board.isInMill(i)) inMill.add(i);
                else                    notInMill.add(i);
            }
        }
        List<Integer> capturable = notInMill.isEmpty() ? inMill : notInMill;
        if (capturable.isEmpty()) return null;

        int chosen = (strategy == STRATEGY_RANDOM)
                ? capturable.get(rng.nextInt(capturable.size()))
                : chooseBestCapture(board, capturable, opponentColor);

        Pawn target = board.getPawnAt(chosen);
        if (target == null) return null;

        ActionList actions = ActionFactory.generateRemoveFromStage(model, target);
        return actions;
    }

    private int chooseBestCapture(MerelleBoard board, List<Integer> candidates, int opponentColor) {
        int bestScore = Integer.MIN_VALUE;
        int bestPos = candidates.get(0);
        for (int pos : candidates) {
            int score = alignmentScore(board, pos, opponentColor);
            if (score > bestScore) { bestScore = score; bestPos = pos; }
        }
        return bestPos;
    }


    /**
     * Returns a score reflecting how many mills the given position contributes to
     * for the given color. Higher = more dangerous / more useful.
     */
    private int alignmentScore(MerelleBoard board, int posIndex, int color) {
        int score = 0;
        for (int[] mill : MerelleBoard.MILLS) {
            boolean containsPos = false;
            for (int pos : mill) {
                if (pos == posIndex) { containsPos = true; break; }
            }
            if (!containsPos) continue;
            int sameColor = 0;
            boolean hasOpponent = false;
            for (int pos : mill) {
                if (pos == posIndex) continue;
                Pawn p = board.getPawnAt(pos);
                if (p != null) {
                    if (p.getColor() == color) sameColor++;
                    else hasOpponent = true;
                }
            }
            if (!hasOpponent) score += (sameColor + 1);
        }
        return score;
    }
}