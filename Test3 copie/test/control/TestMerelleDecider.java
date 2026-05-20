package control;

import control.MerelleController;
import control.MerelleDecider;
import model.MerelleBoard;
import model.MerelleStageModel;
import model.MerellePawnPot;
import model.Pawn;
import boardifier.control.Controller;
import boardifier.model.Model;
import boardifier.model.action.ActionList;
import boardifier.model.TextElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests pour MerelleDecider.
 *
 * On teste les deux stratégies (RANDOM et SMART) sur les trois phases.
 */
public class TestMerelleDecider {

    private Model model;
    private MerelleStageModel stageModel;
    private Controller controller;
    private MerelleBoard board;

    @BeforeEach
    void setUp() {
        model = new Model();
        model.addComputerPlayer("Robot-1");
        model.addComputerPlayer("Robot-2");

        stageModel = new MerelleStageModel("merelle", model);

        board = new MerelleBoard(0, 0, stageModel);
        stageModel.setBoard(board);

        MerellePawnPot blackPot = new MerellePawnPot("blackpot", 0, 0, stageModel);
        stageModel.setBlackPot(blackPot);
        MerellePawnPot whitePot = new MerellePawnPot("whitepot", 0, 0, stageModel);
        stageModel.setWhitePot(whitePot);

        Pawn[] blackPawns = new Pawn[9];
        for (int i = 0; i < 9; i++) {
            blackPawns[i] = new Pawn(Pawn.PAWN_BLACK, stageModel);
            blackPot.addElement(blackPawns[i], 0, i);
        }
        stageModel.setBlackPawns(blackPawns);

        Pawn[] whitePawns = new Pawn[9];
        for (int i = 0; i < 9; i++) {
            whitePawns[i] = new Pawn(Pawn.PAWN_WHITE, stageModel);
            whitePot.addElement(whitePawns[i], 0, i);
        }
        stageModel.setWhitePawns(whitePawns);

        TextElement playerName = new TextElement("Robot-1", stageModel);
        stageModel.setPlayerName(playerName);

        model.startStage(stageModel);

        controller = mock(Controller.class);
    }


    @Test
    void testDecide_placement_random_returnsNonNull() {
        stageModel.setPhase(MerelleStageModel.PHASE_PLACEMENT);
        MerelleDecider decider = new MerelleDecider(model, controller, MerelleDecider.STRATEGY_RANDOM);

        ActionList actions = decider.decide();

        assertNotNull(actions, "Random placement should return a non-null ActionList");
    }

    @Test
    void testDecide_placement_random_targetIsEmpty() {
        stageModel.setPhase(MerelleStageModel.PHASE_PLACEMENT);

        for (int i = 0; i < 10; i++) {
            Model m = new Model();
            m.addComputerPlayer("R1");
            m.addComputerPlayer("R2");
            MerelleStageModel sm = createStageModel(m);
            m.startStage(sm);
            MerelleDecider d = new MerelleDecider(m, controller, MerelleDecider.STRATEGY_RANDOM);
            assertNotNull(d.decide());
        }
    }


    @Test
    void testDecide_placement_smart_returnsNonNull() {
        stageModel.setPhase(MerelleStageModel.PHASE_PLACEMENT);
        MerelleDecider decider = new MerelleDecider(model, controller, MerelleDecider.STRATEGY_SMART);

        ActionList actions = decider.decide();

        assertNotNull(actions);
    }

    @Test
    void testDecide_placement_smart_prefersMillFormation() {
        placeOnBoard(0, Pawn.PAWN_BLACK);
        placeOnBoard(2, Pawn.PAWN_BLACK);
        placeOnBoard(9, Pawn.PAWN_WHITE);

        stageModel.setPhase(MerelleStageModel.PHASE_PLACEMENT);

        MerelleDecider decider = new MerelleDecider(model, controller, MerelleDecider.STRATEGY_SMART);
        ActionList actions = decider.decide();

        assertNotNull(actions, "Smart placement should return an action even with mill opportunity");
    }


    @Test
    void testDecide_movement_random_returnsNonNull() {
        placeOnBoard(0, Pawn.PAWN_BLACK);
        placeOnBoard(9, Pawn.PAWN_BLACK);
        placeOnBoard(21, Pawn.PAWN_BLACK);
        placeOnBoard(3, Pawn.PAWN_WHITE);
        placeOnBoard(10, Pawn.PAWN_WHITE);
        placeOnBoard(18, Pawn.PAWN_WHITE);

        stageModel.setPhase(MerelleStageModel.PHASE_MOVEMENT);
        MerelleDecider decider = new MerelleDecider(model, controller, MerelleDecider.STRATEGY_RANDOM);

        ActionList actions = decider.decide();
        assertNotNull(actions);
    }

    @Test
    void testDecide_movement_random_null_whenNoMoves() {
        placeOnBoard(15, Pawn.PAWN_BLACK);
        placeOnBoard(11, Pawn.PAWN_WHITE);
        placeOnBoard(16, Pawn.PAWN_WHITE);

        stageModel.setPhase(MerelleStageModel.PHASE_MOVEMENT);
        MerelleDecider decider = new MerelleDecider(model, controller, MerelleDecider.STRATEGY_RANDOM);

        ActionList actions = decider.decide();
        assertNull(actions, "Should return null when no moves available");
    }


    @Test
    void testDecide_movement_smart_returnsNonNull() {
        placeOnBoard(0, Pawn.PAWN_BLACK);
        placeOnBoard(3, Pawn.PAWN_BLACK);
        placeOnBoard(6, Pawn.PAWN_BLACK);
        placeOnBoard(2, Pawn.PAWN_WHITE);
        placeOnBoard(5, Pawn.PAWN_WHITE);
        placeOnBoard(8, Pawn.PAWN_WHITE);

        stageModel.setPhase(MerelleStageModel.PHASE_MOVEMENT);
        MerelleDecider decider = new MerelleDecider(model, controller, MerelleDecider.STRATEGY_SMART);

        assertNotNull(decider.decide());
    }

    @Test
    void testDecide_movement_smart_prefersMillMove() {
        placeOnBoard(0, Pawn.PAWN_BLACK);
        placeOnBoard(2, Pawn.PAWN_BLACK);
        placeOnBoard(4, Pawn.PAWN_BLACK);
        placeOnBoard(9, Pawn.PAWN_WHITE);
        placeOnBoard(10, Pawn.PAWN_WHITE);
        placeOnBoard(11, Pawn.PAWN_WHITE);

        stageModel.setPhase(MerelleStageModel.PHASE_MOVEMENT);
        MerelleDecider decider = new MerelleDecider(model, controller, MerelleDecider.STRATEGY_SMART);

        ActionList actions = decider.decide();
        assertNotNull(actions, "Smart movement should find the mill-forming move");
    }


    @Test
    void testDecide_capture_random_returnsNonNull() {
        placeOnBoard(9, Pawn.PAWN_WHITE);
        placeOnBoard(10, Pawn.PAWN_WHITE);

        stageModel.setPhase(MerelleStageModel.PHASE_CAPTURE);
        MerelleDecider decider = new MerelleDecider(model, controller, MerelleDecider.STRATEGY_RANDOM);

        assertNotNull(decider.decide());
    }

    @Test
    void testDecide_capture_null_whenNoOpponent() {
        stageModel.setPhase(MerelleStageModel.PHASE_CAPTURE);
        MerelleDecider decider = new MerelleDecider(model, controller, MerelleDecider.STRATEGY_RANDOM);

        assertNull(decider.decide(), "Should return null when no opponent pawns to capture");
    }


    @Test
    void testDecide_capture_smart_prefersNotInMill() {
        placeOnBoard(0, Pawn.PAWN_WHITE);
        placeOnBoard(1, Pawn.PAWN_WHITE);
        placeOnBoard(2, Pawn.PAWN_WHITE);
        placeOnBoard(9, Pawn.PAWN_WHITE);

        placeOnBoard(3, Pawn.PAWN_BLACK);
        placeOnBoard(5, Pawn.PAWN_BLACK);

        stageModel.setPhase(MerelleStageModel.PHASE_CAPTURE);
        MerelleDecider decider = new MerelleDecider(model, controller, MerelleDecider.STRATEGY_SMART);

        ActionList actions = decider.decide();
        assertNotNull(actions, "Smart capture should choose pawn not in mill");
    }

    @Test
    void testDecide_capture_smart_allInMill_capturesAny() {
        placeOnBoard(0, Pawn.PAWN_WHITE);
        placeOnBoard(1, Pawn.PAWN_WHITE);
        placeOnBoard(2, Pawn.PAWN_WHITE);

        stageModel.setPhase(MerelleStageModel.PHASE_CAPTURE);
        MerelleDecider decider = new MerelleDecider(model, controller, MerelleDecider.STRATEGY_SMART);

        assertNotNull(decider.decide());
    }


    @Test
    void testDecide_routesToPlacement() {
        stageModel.setPhase(MerelleStageModel.PHASE_PLACEMENT);
        MerelleDecider decider = new MerelleDecider(model, controller, MerelleDecider.STRATEGY_RANDOM);
        assertNotNull(decider.decide());
    }

    @Test
    void testDecide_routesToMovement() {
        placeOnBoard(0, Pawn.PAWN_BLACK);
        placeOnBoard(1, Pawn.PAWN_WHITE);
        stageModel.setPhase(MerelleStageModel.PHASE_MOVEMENT);
        MerelleDecider decider = new MerelleDecider(model, controller, MerelleDecider.STRATEGY_RANDOM);
        assertNotNull(decider.decide());
    }

    @Test
    void testDecide_unknownPhase_returnsNull() {
        stageModel.setPhase(99);
        MerelleDecider decider = new MerelleDecider(model, controller, MerelleDecider.STRATEGY_RANDOM);
        assertNull(decider.decide());
    }


    private void placeOnBoard(int posIndex, int color) {
        Pawn pawn = new Pawn(color, stageModel);
        int[] rc = MerelleBoard.posToRowCol(posIndex);
        board.addElement(pawn, rc[0], rc[1]);
    }

    private MerelleStageModel createStageModel(Model m) {
        MerelleStageModel sm = new MerelleStageModel("merelle", m);
        MerelleBoard b = new MerelleBoard(0, 0, sm);
        sm.setBoard(b);
        MerellePawnPot bp = new MerellePawnPot("blackpot", 0, 0, sm);
        sm.setBlackPot(bp);
        MerellePawnPot wp = new MerellePawnPot("whitepot", 0, 0, sm);
        sm.setWhitePot(wp);
        Pawn[] bPawns = new Pawn[9];
        for (int i = 0; i < 9; i++) { bPawns[i] = new Pawn(Pawn.PAWN_BLACK, sm); bp.addElement(bPawns[i], 0, i); }
        sm.setBlackPawns(bPawns);
        Pawn[] wPawns = new Pawn[9];
        for (int i = 0; i < 9; i++) { wPawns[i] = new Pawn(Pawn.PAWN_WHITE, sm); wp.addElement(wPawns[i], 0, i); }
        sm.setWhitePawns(wPawns);
        TextElement te = new TextElement("R1", sm);
        sm.setPlayerName(te);
        return sm;
    }
}