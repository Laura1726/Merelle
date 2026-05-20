package view;

import boardifier.model.GameException;
import boardifier.model.GameStageModel;
import boardifier.view.GameStageView;
import model.MerelleStageModel;

/**
 * MerelleStageView assembles all the looks for a Merelle game stage and
 * provides the print() method that renders the full game state to the console.
 *
 * In boardifier-console, GameStageView.createLooks() registers every look
 * with addLook(). The framework calls render() on each look automatically.
 *
 * The main output, print(), is called by MerelleController.update() at the
 * start of each turn to refresh the terminal display.
 *
 * Layout printed:
 * ┌─────────────────────────────────────────┐
 * │  Current player: p1                     │
 * │                                         │
 * │  Black reserve: [B][B][B][ ][ ]...      │
 * │  White reserve: [W][W][W][ ][ ]...      │
 * │                                         │
 * │   0-----------1-----------2             │
 * │   |  ...  (board) ...  |                │
 * │  21----------22----------23             │
 * │                                         │
 * │  B = Black pawn   W = White pawn        │
 * │  [n] = reachable position               │
 * └─────────────────────────────────────────┘
 */
public class MerelleStageView extends GameStageView {

    private MerelleBoardLook boardLook;
    private MerellePawnPotLook blackPotLook;
    private MerellePawnPotLook whitePotLook;

    public MerelleStageView(String name, GameStageModel gameStageModel) {
        super(name, gameStageModel);
    }

    /**
     * Creates and registers all looks with the framework.
     */
    @Override
    public void createLooks() throws GameException {
        MerelleStageModel stageModel = (MerelleStageModel) gameStageModel;

        boardLook = new MerelleBoardLook(stageModel.getBoard());
        addLook(boardLook);

        blackPotLook = new MerellePawnPotLook(stageModel.getBlackPot(), "Black reserve");
        addLook(blackPotLook);

        whitePotLook = new MerellePawnPotLook(stageModel.getWhitePot(), "White reserve");
        addLook(whitePotLook);

        for (model.Pawn p : stageModel.getBlackPawns()) {
            addLook(new PawnLook(p));
        }
        for (model.Pawn p : stageModel.getWhitePawns()) {
            addLook(new PawnLook(p));
        }

        addLook(new boardifier.view.TextLook(14, "0x000000", stageModel.getPlayerName()));
    }

    /**
     * Prints the complete game state to stdout.
     */
    public void print() {
        MerelleStageModel stageModel = (MerelleStageModel) gameStageModel;

        System.out.println();
        System.out.println("==========================================");
        System.out.println("  Current player: " + stageModel.getModel().getCurrentPlayerName());
        System.out.println("==========================================");
        System.out.println();


        System.out.println("  " + blackPotLook.toText());
        System.out.println("  " + whitePotLook.toText());
        System.out.println();


        String phaseStr;
        switch (stageModel.getPhase()) {
            case MerelleStageModel.PHASE_PLACEMENT:
                int bLeft = stageModel.getBlackPawnsToPlace();
                int wLeft = stageModel.getWhitePawnsToPlace();
                phaseStr = "PLACEMENT  (Black to place: " + bLeft + "  |  White to place: " + wLeft + ")";
                break;
            case MerelleStageModel.PHASE_MOVEMENT:
                boolean bFly = stageModel.canFly(model.Pawn.PAWN_BLACK);
                boolean wFly = stageModel.canFly(model.Pawn.PAWN_WHITE);
                phaseStr = "MOVEMENT";
                if (bFly) phaseStr += "  [Black can fly]";
                if (wFly) phaseStr += "  [White can fly]";
                break;
            case MerelleStageModel.PHASE_CAPTURE:
                phaseStr = "CAPTURE — remove one opponent pawn!";
                break;
            default:
                phaseStr = "?";
        }
        System.out.println("  Phase: " + phaseStr);
        System.out.println();

        // Board
        System.out.println(boardLook.toText());

        // Pawn count summary
        int bOnBoard = stageModel.getBoard().countPawns(model.Pawn.PAWN_BLACK);
        int wOnBoard = stageModel.getBoard().countPawns(model.Pawn.PAWN_WHITE);
        System.out.println("  Pawns on board — Black: " + bOnBoard + "  |  White: " + wOnBoard);
        System.out.println();
        System.out.println("  B = Black pawn    W = White pawn    [n] = reachable position");
        System.out.println("  Type 'stop' at any time to quit.");
        System.out.println("------------------------------------------");
    }
}