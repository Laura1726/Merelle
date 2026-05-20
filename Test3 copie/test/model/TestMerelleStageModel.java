package model;

import boardifier.model.Model;
import boardifier.model.TextElement;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests pour MerelleStageModel.
 */
public class TestMerelleStageModel {

    private Model model;
    private MerelleStageModel stageModel;
    private MerelleBoard board;

    @BeforeEach
    void setUp() {
        model = new Model();
        model.addHumanPlayer("Black");
        model.addHumanPlayer("White");

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

        TextElement playerName = new TextElement("Black", stageModel);
        stageModel.setPlayerName(playerName);
    }


    @Test
    void testInitialPhase() {
        assertEquals(MerelleStageModel.PHASE_PLACEMENT, stageModel.getPhase());
    }

    @Test
    void testInitialMoveState() {
        assertEquals(MerelleStageModel.STATE_SELECT_PAWN, stageModel.getMoveState());
    }

    @Test
    void testInitialPawnsToPlace() {
        assertEquals(9, stageModel.getBlackPawnsToPlace());
        assertEquals(9, stageModel.getWhitePawnsToPlace());
    }

    @Test
    void testInitialSelectedPawnPos() {
        assertEquals(-1, stageModel.getSelectedPawnPos());
    }

    @Test
    void testInitialNoCaptureTurns() {
        assertEquals(0, stageModel.getNoCaptureTurns());
    }

    @Test
    void testInitialLastMillFormed_null() {
        assertNull(stageModel.getLastMillFormed(0));
        assertNull(stageModel.getLastMillFormed(1));
    }


    @Test
    void testSetPhase() {
        stageModel.setPhase(MerelleStageModel.PHASE_MOVEMENT);
        assertEquals(MerelleStageModel.PHASE_MOVEMENT, stageModel.getPhase());
    }

    @Test
    void testSetMoveState() {
        stageModel.setMoveState(MerelleStageModel.STATE_SELECT_DEST);
        assertEquals(MerelleStageModel.STATE_SELECT_DEST, stageModel.getMoveState());
    }

    @Test
    void testSetSelectedPawnPos() {
        stageModel.setSelectedPawnPos(7);
        assertEquals(7, stageModel.getSelectedPawnPos());
    }

    @Test
    void testIncrementNoCaptureTurns() {
        stageModel.incrementNoCaptureTurns();
        stageModel.incrementNoCaptureTurns();
        assertEquals(2, stageModel.getNoCaptureTurns());
    }

    @Test
    void testResetNoCaptureTurns() {
        stageModel.incrementNoCaptureTurns();
        stageModel.incrementNoCaptureTurns();
        stageModel.resetNoCaptureTurns();
        assertEquals(0, stageModel.getNoCaptureTurns());
    }

    @Test
    void testSetLastMillFormed() {
        int[] mill = {0, 1, 2};
        stageModel.setLastMillFormed(0, mill);
        assertArrayEquals(mill, stageModel.getLastMillFormed(0));
        assertNull(stageModel.getLastMillFormed(1));
    }


    @Test
    void testCancelSelection_resetsState() {
        stageModel.setPhase(MerelleStageModel.PHASE_MOVEMENT);
        stageModel.setSelectedPawnPos(5);
        stageModel.setMoveState(MerelleStageModel.STATE_SELECT_DEST);

        stageModel.cancelSelection();

        assertEquals(-1, stageModel.getSelectedPawnPos());
        assertEquals(MerelleStageModel.STATE_SELECT_PAWN, stageModel.getMoveState());
    }


    @Test
    void testCanFly_false_duringPlacement() {
        stageModel.setPhase(MerelleStageModel.PHASE_PLACEMENT);
        assertFalse(stageModel.canFly(Pawn.PAWN_BLACK));
    }

    @Test
    void testCanFly_false_moreThan3Pawns() {
        stageModel.setPhase(MerelleStageModel.PHASE_MOVEMENT);
        addToBoard(0, Pawn.PAWN_BLACK);
        addToBoard(1, Pawn.PAWN_BLACK);
        addToBoard(3, Pawn.PAWN_BLACK);
        addToBoard(5, Pawn.PAWN_BLACK);
        assertFalse(stageModel.canFly(Pawn.PAWN_BLACK));
    }

    @Test
    void testCanFly_true_exactly3Pawns() {
        stageModel.setPhase(MerelleStageModel.PHASE_MOVEMENT);
        addToBoard(0, Pawn.PAWN_BLACK);
        addToBoard(9, Pawn.PAWN_BLACK);
        addToBoard(21, Pawn.PAWN_BLACK);
        assertTrue(stageModel.canFly(Pawn.PAWN_BLACK));
    }



    @Test
    void testCheckEndConditions_noEnd_enoughPawns() {
        int[] bPos = {0, 3, 6, 9, 12, 15, 18, 21, 23};
        int[] wPos = {2, 5, 8, 14, 17, 20, 11, 16, 19};
        for (int i = 0; i < 9; i++) {
            addToBoard(bPos[i], Pawn.PAWN_BLACK);
            addToBoard(wPos[i], Pawn.PAWN_WHITE);
        }

        MerelleStageModel spy = spy(stageModel);
        spy.setPhase(MerelleStageModel.PHASE_MOVEMENT);
        doReturn(0).when(spy).getBlackPawnsToPlace();
        doReturn(0).when(spy).getWhitePawnsToPlace();

        spy.checkEndConditions();

        assertFalse(model.isEndStage(),
                "Game should not end with 9 pawns on each side");
    }

    @Test
    void testCheckEndConditions_win_opponentBelow3() {
        addToBoard(0, Pawn.PAWN_BLACK);
        addToBoard(3, Pawn.PAWN_BLACK);
        addToBoard(6, Pawn.PAWN_BLACK);
        addToBoard(9, Pawn.PAWN_BLACK);
        addToBoard(2, Pawn.PAWN_WHITE);
        addToBoard(5, Pawn.PAWN_WHITE);

        MerelleStageModel spy = spy(stageModel);
        spy.setPhase(MerelleStageModel.PHASE_MOVEMENT);
        doReturn(0).when(spy).getBlackPawnsToPlace();
        doReturn(0).when(spy).getWhitePawnsToPlace();

        spy.checkEndConditions();

        assertTrue(model.isEndStage(), "Game should end when opponent has < 3 pawns");
        assertEquals(0, model.getIdWinner(), "Black (player 0) should win");
    }

    @Test
    void testCheckEndConditions_draw_drawLimitReached() {
        int[] bPos = {0, 3, 6, 9, 12};
        int[] wPos = {2, 5, 8, 14, 17};
        for (int i = 0; i < 5; i++) {
            addToBoard(bPos[i], Pawn.PAWN_BLACK);
            addToBoard(wPos[i], Pawn.PAWN_WHITE);
        }

        MerelleStageModel spy = spy(stageModel);
        spy.setPhase(MerelleStageModel.PHASE_MOVEMENT);
        doReturn(0).when(spy).getBlackPawnsToPlace();
        doReturn(0).when(spy).getWhitePawnsToPlace();

        for (int i = 0; i < MerelleStageModel.DRAW_LIMIT; i++) {
            spy.incrementNoCaptureTurns();
        }

        spy.checkEndConditions();

        assertTrue(model.isEndStage(), "Game should end on draw");
        assertEquals(-1, model.getIdWinner(), "Winner should be -1 (draw)");
    }

    @Test
    void testCheckEndConditions_noEnd_beforeDrawLimit() {
        int[] bPos = {0, 3, 6, 9, 12};
        int[] wPos = {2, 5, 8, 14, 17};
        for (int i = 0; i < 5; i++) {
            addToBoard(bPos[i], Pawn.PAWN_BLACK);
            addToBoard(wPos[i], Pawn.PAWN_WHITE);
        }

        MerelleStageModel spy = spy(stageModel);
        spy.setPhase(MerelleStageModel.PHASE_MOVEMENT);
        doReturn(0).when(spy).getBlackPawnsToPlace();
        doReturn(0).when(spy).getWhitePawnsToPlace();
        for (int i = 0; i < MerelleStageModel.DRAW_LIMIT - 1; i++) {
            spy.incrementNoCaptureTurns();
        }

        spy.checkEndConditions();

        assertFalse(model.isEndStage(), "Game should not end one turn before draw limit");
    }


    @Test
    void testCallback_placementDecrementsBlackCounter() {
        Pawn pawn = stageModel.getBlackPawns()[0];
        int[] rc = MerelleBoard.posToRowCol(0);
        stageModel.putInContainer(pawn, board, rc[0], rc[1]);

        assertEquals(8, stageModel.getBlackPawnsToPlace());
        assertEquals(9, stageModel.getWhitePawnsToPlace());
    }

    @Test
    void testCallback_placementDecrementsWhiteCounter() {
        Pawn pawn = stageModel.getWhitePawns()[0];
        int[] rc = MerelleBoard.posToRowCol(2);
        stageModel.putInContainer(pawn, board, rc[0], rc[1]);

        assertEquals(9, stageModel.getBlackPawnsToPlace());
        assertEquals(8, stageModel.getWhitePawnsToPlace());
    }

    @Test
    void testCallback_transitionToMovement_whenAllPlaced() {
        int[] blackPositions = {0, 3, 6, 9, 12, 15, 18, 21, 23};
        int[] whitePositions = {2, 5, 8, 14, 17, 20, 11, 16, 19};

        for (int i = 0; i < 9; i++) {
            stageModel.putInContainer(stageModel.getBlackPawns()[i], board,
                    MerelleBoard.posToRowCol(blackPositions[i])[0],
                    MerelleBoard.posToRowCol(blackPositions[i])[1]);
            stageModel.putInContainer(stageModel.getWhitePawns()[i], board,
                    MerelleBoard.posToRowCol(whitePositions[i])[0],
                    MerelleBoard.posToRowCol(whitePositions[i])[1]);
        }

        assertEquals(MerelleStageModel.PHASE_MOVEMENT, stageModel.getPhase(),
                "After all 18 pawns placed without mill, phase should be MOVEMENT");
    }

    @Test
    void testCallback_millTriggersCapture() {
        addToBoard(9, Pawn.PAWN_WHITE);
        addToBoard(0, Pawn.PAWN_BLACK);
        addToBoard(2, Pawn.PAWN_BLACK);

        stageModel.putInContainer(stageModel.getBlackPawns()[0], board,
                MerelleBoard.posToRowCol(1)[0], MerelleBoard.posToRowCol(1)[1]);

        assertEquals(MerelleStageModel.PHASE_CAPTURE, stageModel.getPhase(),
                "Forming a mill with an opponent pawn on board should trigger PHASE_CAPTURE");
    }

    @Test
    void testCallback_noCapture_whenNoOpponentPawns() {
        addToBoard(0, Pawn.PAWN_BLACK);
        addToBoard(2, Pawn.PAWN_BLACK);

        stageModel.putInContainer(stageModel.getBlackPawns()[0], board,
                MerelleBoard.posToRowCol(1)[0], MerelleBoard.posToRowCol(1)[1]);

        assertNotEquals(MerelleStageModel.PHASE_CAPTURE, stageModel.getPhase(),
                "Should not enter PHASE_CAPTURE if no opponent pawns on board");
    }

    @Test
    void testCallback_lastMillFormed_updatedOnMill() {
        addToBoard(9, Pawn.PAWN_WHITE);
        addToBoard(0, Pawn.PAWN_BLACK);
        addToBoard(2, Pawn.PAWN_BLACK);

        stageModel.putInContainer(stageModel.getBlackPawns()[0], board,
                MerelleBoard.posToRowCol(1)[0], MerelleBoard.posToRowCol(1)[1]);

        assertNotNull(stageModel.getLastMillFormed(0),
                "lastMillFormed[0] should be set after black forms a mill");
    }

    @Test
    void testCallback_lastMillFormed_clearedOnNoMill() {
        stageModel.putInContainer(stageModel.getBlackPawns()[0], board,
                MerelleBoard.posToRowCol(0)[0], MerelleBoard.posToRowCol(0)[1]);

        assertNull(stageModel.getLastMillFormed(0),
                "lastMillFormed[0] should remain null if no mill was formed");
    }

    private void addToBoard(int posIndex, int color) {
        Pawn pawn = new Pawn(color, stageModel);
        int[] rc = MerelleBoard.posToRowCol(posIndex);
        board.addElement(pawn, rc[0], rc[1]);
    }
}