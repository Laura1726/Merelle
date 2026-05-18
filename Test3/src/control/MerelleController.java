package control;

import boardifier.control.ActionFactory;
import boardifier.control.ActionPlayer;
import boardifier.control.Controller;
import boardifier.model.GameElement;
import boardifier.model.Model;
import boardifier.model.Player;
import boardifier.model.action.ActionList;
import boardifier.view.View;
import model.MerelleBoard;
import model.MerelleStageModel;
import model.MerellePawnPot;
import model.Pawn;
import view.MerelleStageView;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class MerelleController extends Controller {

    private BufferedReader consoleIn;
    private final int strategy;

    public MerelleController(Model model, View view) {
        this(model, view, MerelleDecider.STRATEGY_SMART);
    }

    public MerelleController(Model model, View view, int strategy) {
        super(model, view);
        this.strategy = strategy;
        this.consoleIn = new BufferedReader(new InputStreamReader(System.in));
    }



    public void stageLoop() {
        printBoard();
        while (!model.isEndStage()) {
            playTurn();
            if (!model.isEndStage()) {
                endOfTurn();
                printBoard();
            }
        }
        endGame();
    }

    private void printBoard() {
        MerelleStageView stageView = (MerelleStageView) view.getGameStageView();
        if (stageView != null) {
            stageView.print();
        }
    }


    private void playTurn() {
        Player p = model.getCurrentPlayer();

        if (p.getType() == Player.COMPUTER) {
            System.out.println("\n" + p.getName() + " (COMPUTER) is thinking...");
            MerelleStageModel stageModel = (MerelleStageModel) model.getGameStage();
            MerelleDecider decider = new MerelleDecider(model, this, strategy);

            ActionPlayer play = new ActionPlayer(model, this, decider, null);
            play.run();

            if (stageModel.getPhase() == MerelleStageModel.PHASE_CAPTURE) {
                int bBefore = stageModel.getBoard().countPawns(Pawn.PAWN_BLACK);
                int wBefore = stageModel.getBoard().countPawns(Pawn.PAWN_WHITE);
                System.out.println("DEBUG before capture: B=" + bBefore + " W=" + wBefore);
                ActionPlayer capturePlay = new ActionPlayer(model, this, decider, null);
                capturePlay.run();
                int bAfter = stageModel.getBoard().countPawns(Pawn.PAWN_BLACK);
                int wAfter = stageModel.getBoard().countPawns(Pawn.PAWN_WHITE);
                System.out.println("DEBUG after capture: B=" + bAfter + " W=" + wAfter);
                stageModel.resetNoCaptureTurns();
                stageModel.setPhase(stageModel.getPhaseAfterCapture());
                stageModel.setMoveState(MerelleStageModel.STATE_SELECT_PAWN);
                stageModel.getBoard().resetReachableCells(false);
                System.out.println("DEBUG calling checkEndConditions, currentPlayer=" + model.getIdPlayer());
                stageModel.checkEndConditions();
                System.out.println("DEBUG after checkEnd, isEndStage=" + model.isEndStage());
            }
        } else {
            MerelleStageModel stageModel = (MerelleStageModel) model.getGameStage();
            boolean ok = false;
            while (!ok) {
                printPrompt(p, stageModel);
                try {
                    String line = consoleIn.readLine();
                    if (line == null) continue;
                    line = line.trim();
                    if (line.equalsIgnoreCase("stop")) {
                        System.out.println("Game stopped by player.");
                        model.setIdWinner(-1);
                        model.stopStage();
                        return;
                    }
                    ok = analyseAndPlay(line, stageModel);
                    if (!ok) System.out.println("  >> Invalid instruction, please retry.");
                } catch (IOException e) {
                    System.err.println("Read error: " + e.getMessage());
                }
            }
        }
    }


    public void endOfTurn() {
        MerelleStageModel stageModel = (MerelleStageModel) model.getGameStage();
        if (stageModel.getPhase() != MerelleStageModel.PHASE_CAPTURE) {
            model.setNextPlayer();
        }
        Player p = model.getCurrentPlayer();
        stageModel.getPlayerName().setText(p.getName());
    }


    public void endGame() {
        System.out.println("\n===========================");
        if (model.getIdWinner() == -1) {
            System.out.println("DRAW - no winner.");
        } else {
            String winner = model.getPlayers().get(model.getIdWinner()).getName();
            System.out.println("GAME OVER - " + winner + " wins!");
        }
        System.out.println("===========================\n");
    }


    private void printPrompt(Player p, MerelleStageModel stageModel) {
        String name = p.getName();
        switch (stageModel.getPhase()) {
            case MerelleStageModel.PHASE_PLACEMENT:
                System.out.print(name + " [PLACEMENT] position (0-23) > ");
                break;
            case MerelleStageModel.PHASE_MOVEMENT:
                if (stageModel.getMoveState() == MerelleStageModel.STATE_SELECT_PAWN) {
                    System.out.print(name + " [MOVEMENT] select your pawn (0-23) > ");
                } else {
                    int sel = stageModel.getSelectedPawnPos();
                    System.out.print(name + " [MOVEMENT] move pawn from " + sel
                            + " -> destination (0-23) or C to cancel > ");
                }
                break;
            case MerelleStageModel.PHASE_CAPTURE:
                System.out.print(name + " [CAPTURE] opponent pawn to remove (0-23) > ");
                break;
        }
        System.out.flush();
    }


    private boolean analyseAndPlay(String line, MerelleStageModel stageModel) {
        switch (stageModel.getPhase()) {
            case MerelleStageModel.PHASE_PLACEMENT: return handlePlacement(line, stageModel);
            case MerelleStageModel.PHASE_MOVEMENT:  return handleMovement(line, stageModel);
            case MerelleStageModel.PHASE_CAPTURE:   return handleCapture(line, stageModel);
            default: return false;
        }
    }


    private boolean handlePlacement(String line, MerelleStageModel stageModel) {
        int pos = parsePosition(line);
        if (pos < 0) {
            System.out.println("  >> Error: enter a number between 0 and 23.");
            return false;
        }

        MerelleBoard board = stageModel.getBoard();
        if (!board.isEmptyAtPos(pos)) {
            System.out.println("  >> Error: position " + pos + " is already occupied.");
            return false;
        }

        int[] rc = MerelleBoard.posToRowCol(pos);

        MerellePawnPot pot = (model.getIdPlayer() == 0)
                ? stageModel.getBlackPot()
                : stageModel.getWhitePot();

        GameElement pawn = null;
        for (int col = 0; col < 9; col++) {
            if (!pot.isEmptyAt(0, col)) {
                pawn = pot.getElement(0, col);
                break;
            }
        }
        if (pawn == null) {
            System.out.println("  >> Error: no pawn left in pot.");
            return false;
        }

        ActionList actions = ActionFactory.generatePutInContainer(
                this, model, pawn, MerelleBoard.BOARD_NAME, rc[0], rc[1]);

        ActionPlayer play = new ActionPlayer(model, this, actions);
        play.run();

        if (stageModel.getPhase() == MerelleStageModel.PHASE_CAPTURE) {
            handleCaptureLoop(stageModel);
        }

        return true;
    }


    private boolean handleMovement(String line, MerelleStageModel stageModel) {
        if (stageModel.getMoveState() == MerelleStageModel.STATE_SELECT_PAWN) {
            return handleMovementSelectPawn(line, stageModel);
        } else {
            return handleMovementSelectDest(line, stageModel);
        }
    }

    private boolean handleMovementSelectPawn(String line, MerelleStageModel stageModel) {
        int from = parsePosition(line);
        if (from < 0) {
            System.out.println("  >> Error: enter a number between 0 and 23.");
            return false;
        }

        int currentColor = (model.getIdPlayer() == 0) ? Pawn.PAWN_BLACK : Pawn.PAWN_WHITE;
        boolean canFly = stageModel.canFly(currentColor);
        MerelleBoard board = stageModel.getBoard();

        Pawn p = board.getPawnAt(from);
        if (p == null || p.getColor() != currentColor) {
            System.out.println("  >> Error: no pawn of yours at position " + from + ".");
            return false;
        }
        if (!canFly && !board.hasValidMove(from)) {
            System.out.println("  >> Error: pawn at " + from + " is blocked. Choose another.");
            return false;
        }

        stageModel.setSelectedPawnPos(from);
        stageModel.setMoveState(MerelleStageModel.STATE_SELECT_DEST);
        board.setValidCellsForMove(from, canFly);

        boolean ok = false;
        while (!ok && !model.isEndStage()) {
            printPrompt(model.getCurrentPlayer(), stageModel);
            try {
                String dest = consoleIn.readLine();
                if (dest == null) continue;
                dest = dest.trim();
                if (dest.equalsIgnoreCase("stop")) {
                    model.setIdWinner(-1);
                    model.stopStage();
                    return true;
                }
                if (dest.equalsIgnoreCase("C")) {
                    stageModel.cancelSelection();
                    System.out.println("  Selection cancelled.");
                    return false;
                }
                ok = handleMovementSelectDest(dest, stageModel);
                if (!ok) System.out.println("  >> Invalid destination, please retry.");
            } catch (IOException e) {
                System.err.println("Read error: " + e.getMessage());
            }
        }
        return true;
    }

    private boolean handleMovementSelectDest(String line, MerelleStageModel stageModel) {
        int dest = parsePosition(line);
        if (dest < 0) {
            System.out.println("  >> Error: enter a number between 0 and 23, or C to cancel.");
            return false;
        }

        MerelleBoard board = stageModel.getBoard();
        if (!board.isEmptyAtPos(dest)) {
            System.out.println("  >> Error: position " + dest + " is already occupied.");
            return false;
        }

        int[] rc = MerelleBoard.posToRowCol(dest);
        if (!board.canReachCell(rc[0], rc[1])) {
            System.out.println("  >> Error: cannot move there (not adjacent or reachable).");
            return false;
        }

        int from = stageModel.getSelectedPawnPos();
        int[] fromRc = MerelleBoard.posToRowCol(from);
        GameElement pawn = board.getElement(fromRc[0], fromRc[1]);
        if (pawn == null) return false;

        stageModel.incrementNoCaptureTurns();

        ActionList actions = ActionFactory.generatePutInContainer(
                this, model, pawn, MerelleBoard.BOARD_NAME, rc[0], rc[1]);

        ActionPlayer play = new ActionPlayer(model, this, actions);
        play.run();

        stageModel.setSelectedPawnPos(-1);
        stageModel.setMoveState(MerelleStageModel.STATE_SELECT_PAWN);

        if (stageModel.getPhase() == MerelleStageModel.PHASE_CAPTURE) {
            stageModel.resetNoCaptureTurns();
            handleCaptureLoop(stageModel);
        }

        return true;
    }


    private boolean handleCapture(String line, MerelleStageModel stageModel) {
        int pos = parsePosition(line);
        if (pos < 0) {
            System.out.println("  >> Error: enter a number between 0 and 23.");
            return false;
        }

        MerelleBoard board = stageModel.getBoard();
        int[] rc = MerelleBoard.posToRowCol(pos);

        if (!board.canReachCell(rc[0], rc[1])) {
            System.out.println("  >> Error: cannot capture pawn at " + pos
                    + " (protected by mill, or no pawn there).");
            return false;
        }

        Pawn target = board.getPawnAt(pos);
        if (target == null) {
            System.out.println("  >> Error: no pawn at position " + pos + ".");
            return false;
        }

        ActionList actions = ActionFactory.generateRemoveFromStage(model, target);

        ActionPlayer play = new ActionPlayer(model, this, actions);
        play.run();

        stageModel.resetNoCaptureTurns();
        stageModel.setPhase(stageModel.getPhaseAfterCapture());
        stageModel.setMoveState(MerelleStageModel.STATE_SELECT_PAWN);
        board.resetReachableCells(false);

        stageModel.checkEndConditions();
        return true;
    }

    public void handleCaptureLoop(MerelleStageModel stageModel) {
        int opponentColor = (model.getIdPlayer() == 0) ? Pawn.PAWN_WHITE : Pawn.PAWN_BLACK;
        stageModel.getBoard().setValidCellsForCapture(opponentColor);
        System.out.println("  *** MILL formed! Remove one opponent pawn. ***");

        boolean ok = false;
        while (!ok && !model.isEndStage()) {
            printPrompt(model.getCurrentPlayer(), stageModel);
            try {
                String line = consoleIn.readLine();
                if (line == null) continue;
                line = line.trim();
                if (line.equalsIgnoreCase("stop")) {
                    model.setIdWinner(-1);
                    model.stopStage();
                    return;
                }
                ok = handleCapture(line, stageModel);
                if (!ok) System.out.println("  >> Invalid capture, please retry.");
            } catch (IOException e) {
                System.err.println("Read error: " + e.getMessage());
            }
        }
    }

    private int parsePosition(String s) {
        try {
            int pos = Integer.parseInt(s.trim());
            return (pos >= 0 && pos <= 23) ? pos : -1;
        } catch (NumberFormatException e) {
            return -1;
        }
    }
}