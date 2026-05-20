package model;

import model.MerelleBoard;
import model.MerelleStageModel;
import model.Pawn;
import boardifier.model.Model;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for MerelleBoard.
 */
public class TestMerelleBoard {

    private MerelleBoard board;

    @BeforeEach
    void setUp() {
        Model model = mock(Model.class);
        MerelleStageModel stageModel = mock(MerelleStageModel.class);
        when(stageModel.getModel()).thenReturn(model);

        board = new MerelleBoard(0, 0, stageModel);
    }


    @Test
    void testPosToRowCol_position0() {
        int[] rc = MerelleBoard.posToRowCol(0);
        assertEquals(0, rc[0], "Position 0 should be row 0");
        assertEquals(0, rc[1], "Position 0 should be col 0");
    }

    @Test
    void testPosToRowCol_position7() {
        int[] rc = MerelleBoard.posToRowCol(7);
        assertEquals(2, rc[0], "Position 7 should be row 2");
        assertEquals(3, rc[1], "Position 7 should be col 3");
    }

    @Test
    void testPosToRowCol_position23() {
        int[] rc = MerelleBoard.posToRowCol(23);
        assertEquals(6, rc[0], "Position 23 should be row 6");
        assertEquals(6, rc[1], "Position 23 should be col 6");
    }

    @Test
    void testRowColToPos_valid() {
        assertEquals(0, MerelleBoard.rowColToPos(0, 0));
        assertEquals(16, MerelleBoard.rowColToPos(4, 3));
    }

    @Test
    void testRowColToPos_invalid() {
        assertEquals(-1, MerelleBoard.rowColToPos(0, 1));
        assertEquals(-1, MerelleBoard.rowColToPos(3, 3));
    }

    @Test
    void testIsValidPosition_valid() {
        assertTrue(MerelleBoard.isValidPosition(0, 0));
        assertTrue(MerelleBoard.isValidPosition(3, 1));
    }

    @Test
    void testIsValidPosition_invalid() {
        assertFalse(MerelleBoard.isValidPosition(0, 1));
        assertFalse(MerelleBoard.isValidPosition(3, 3));
    }


    @Test
    void testIsEmptyAtPos_initiallyEmpty() {
        for (int i = 0; i < 24; i++) {
            assertTrue(board.isEmptyAtPos(i), "Position " + i + " should be empty initially");
        }
    }

    @Test
    void testGetPawnAt_emptyReturnsNull() {
        assertNull(board.getPawnAt(0));
        assertNull(board.getPawnAt(12));
    }

    @Test
    void testGetPawnAt_afterPlacement() {
        Model model = mock(Model.class);
        MerelleStageModel stageModel = mock(MerelleStageModel.class);
        when(stageModel.getModel()).thenReturn(model);

        Pawn pawn = new Pawn(Pawn.PAWN_BLACK, stageModel);
        int[] rc = MerelleBoard.posToRowCol(5);
        board.addElement(pawn, rc[0], rc[1]);

        assertFalse(board.isEmptyAtPos(5));
        assertNotNull(board.getPawnAt(5));
        assertEquals(Pawn.PAWN_BLACK, board.getPawnAt(5).getColor());
    }


    @Test
    void testCountPawns_empty() {
        assertEquals(0, board.countPawns(Pawn.PAWN_BLACK));
        assertEquals(0, board.countPawns(Pawn.PAWN_WHITE));
    }

    @Test
    void testCountPawns_afterPlacement() {
        Model model = mock(Model.class);
        MerelleStageModel stageModel = mock(MerelleStageModel.class);
        when(stageModel.getModel()).thenReturn(model);

        placeAt(0, Pawn.PAWN_BLACK, stageModel);
        placeAt(1, Pawn.PAWN_BLACK, stageModel);
        placeAt(2, Pawn.PAWN_BLACK, stageModel);
        placeAt(3, Pawn.PAWN_WHITE, stageModel);
        placeAt(4, Pawn.PAWN_WHITE, stageModel);

        assertEquals(3, board.countPawns(Pawn.PAWN_BLACK));
        assertEquals(2, board.countPawns(Pawn.PAWN_WHITE));
    }


    @Test
    void testHasValidMove_emptyBoard() {
        assertTrue(board.hasValidMove(0));
        assertTrue(board.hasValidMove(7));
    }

    @Test
    void testHasValidMove_blocked() {
        Model model = mock(Model.class);
        MerelleStageModel stageModel = mock(MerelleStageModel.class);
        when(stageModel.getModel()).thenReturn(model);


        placeAt(11, Pawn.PAWN_WHITE, stageModel);
        placeAt(16, Pawn.PAWN_WHITE, stageModel);

        assertFalse(board.hasValidMove(15),
                "Pawn at 15 should be blocked when 11 and 16 are occupied");
    }

    @Test
    void testHasValidMove_oneNeighborFree() {
        Model model = mock(Model.class);
        MerelleStageModel stageModel = mock(MerelleStageModel.class);
        when(stageModel.getModel()).thenReturn(model);

        placeAt(1, Pawn.PAWN_WHITE, stageModel);

        assertTrue(board.hasValidMove(0), "Pawn at 0 should still have move via 9");
    }


    @Test
    void testIsInMill_false_whenEmpty() {
        assertFalse(board.isInMill(0));
    }

    @Test
    void testIsInMill_false_incomplete() {
        Model model = mock(Model.class);
        MerelleStageModel stageModel = mock(MerelleStageModel.class);
        when(stageModel.getModel()).thenReturn(model);

        placeAt(0, Pawn.PAWN_BLACK, stageModel);
        placeAt(1, Pawn.PAWN_BLACK, stageModel);

        assertFalse(board.isInMill(0));
        assertFalse(board.isInMill(1));
    }

    @Test
    void testIsInMill_true_completeMill() {
        Model model = mock(Model.class);
        MerelleStageModel stageModel = mock(MerelleStageModel.class);
        when(stageModel.getModel()).thenReturn(model);

        placeAt(0, Pawn.PAWN_BLACK, stageModel);
        placeAt(1, Pawn.PAWN_BLACK, stageModel);
        placeAt(2, Pawn.PAWN_BLACK, stageModel);

        assertTrue(board.isInMill(0));
        assertTrue(board.isInMill(1));
        assertTrue(board.isInMill(2));
    }

    @Test
    void testIsInMill_false_mixedColors() {
        Model model = mock(Model.class);
        MerelleStageModel stageModel = mock(MerelleStageModel.class);
        when(stageModel.getModel()).thenReturn(model);

        placeAt(0, Pawn.PAWN_BLACK, stageModel);
        placeAt(1, Pawn.PAWN_WHITE, stageModel);
        placeAt(2, Pawn.PAWN_BLACK, stageModel);

        assertFalse(board.isInMill(0));
        assertFalse(board.isInMill(1));
    }


    @Test
    void testGetFormedMill_null_noMill() {
        Model model = mock(Model.class);
        MerelleStageModel stageModel = mock(MerelleStageModel.class);
        when(stageModel.getModel()).thenReturn(model);

        placeAt(0, Pawn.PAWN_BLACK, stageModel);

        assertNull(board.getFormedMill(1, Pawn.PAWN_BLACK));
    }

    @Test
    void testGetFormedMill_returnsCorrectMill() {
        Model model = mock(Model.class);
        MerelleStageModel stageModel = mock(MerelleStageModel.class);
        when(stageModel.getModel()).thenReturn(model);

        placeAt(0, Pawn.PAWN_BLACK, stageModel);
        placeAt(2, Pawn.PAWN_BLACK, stageModel);

        int[] mill = board.getFormedMill(1, Pawn.PAWN_BLACK);
        assertNotNull(mill);
        assertEquals(3, mill.length);

        boolean has0 = false, has1 = false, has2 = false;
        for (int pos : mill) {
            if (pos == 0) has0 = true;
            if (pos == 1) has1 = true;
            if (pos == 2) has2 = true;
        }
        assertTrue(has0 && has1 && has2, "Mill should contain positions 0, 1, 2");
    }


    @Test
    void testIsNewMill_true_noLastMill() {
        Model model = mock(Model.class);
        MerelleStageModel stageModel = mock(MerelleStageModel.class);
        when(stageModel.getModel()).thenReturn(model);

        placeAt(0, Pawn.PAWN_BLACK, stageModel);
        placeAt(2, Pawn.PAWN_BLACK, stageModel);

        assertTrue(board.isNewMill(1, Pawn.PAWN_BLACK, null));
    }

    @Test
    void testIsNewMill_false_sameMill() {
        Model model = mock(Model.class);
        MerelleStageModel stageModel = mock(MerelleStageModel.class);
        when(stageModel.getModel()).thenReturn(model);

        placeAt(0, Pawn.PAWN_BLACK, stageModel);
        placeAt(2, Pawn.PAWN_BLACK, stageModel);

        int[] lastMill = {0, 1, 2};
        assertFalse(board.isNewMill(1, Pawn.PAWN_BLACK, lastMill));
    }

    @Test
    void testIsNewMill_true_differentMill() {
        Model model = mock(Model.class);
        MerelleStageModel stageModel = mock(MerelleStageModel.class);
        when(stageModel.getModel()).thenReturn(model);

        placeAt(9, Pawn.PAWN_BLACK, stageModel);
        placeAt(21, Pawn.PAWN_BLACK, stageModel);

        int[] lastMill = {0, 1, 2};
        assertTrue(board.isNewMill(0, Pawn.PAWN_BLACK, lastMill));
    }

    @Test
    void testIsNewMill_false_noMillFormed() {

        assertFalse(board.isNewMill(0, Pawn.PAWN_BLACK, null));
    }


    @Test
    void testIsBlocked_canFly_alwaysFalse() {
        Model model = mock(Model.class);
        MerelleStageModel stageModel = mock(MerelleStageModel.class);
        when(stageModel.getModel()).thenReturn(model);

        placeAt(15, Pawn.PAWN_BLACK, stageModel);
        placeAt(11, Pawn.PAWN_WHITE, stageModel);
        placeAt(16, Pawn.PAWN_WHITE, stageModel);

        assertFalse(board.isBlocked(Pawn.PAWN_BLACK, true));
    }

    @Test
    void testIsBlocked_true_allPawnsBlocked() {
        Model model = mock(Model.class);
        MerelleStageModel stageModel = mock(MerelleStageModel.class);
        when(stageModel.getModel()).thenReturn(model);

        placeAt(15, Pawn.PAWN_BLACK, stageModel);
        placeAt(11, Pawn.PAWN_WHITE, stageModel);
        placeAt(16, Pawn.PAWN_WHITE, stageModel);

        assertTrue(board.isBlocked(Pawn.PAWN_BLACK, false));
    }

    @Test
    void testIsBlocked_false_hasMove() {
        Model model = mock(Model.class);
        MerelleStageModel stageModel = mock(MerelleStageModel.class);
        when(stageModel.getModel()).thenReturn(model);

        placeAt(0, Pawn.PAWN_BLACK, stageModel);

        assertFalse(board.isBlocked(Pawn.PAWN_BLACK, false));
    }


    @Test
    void testFormsMill_true() {
        Model model = mock(Model.class);
        MerelleStageModel stageModel = mock(MerelleStageModel.class);
        when(stageModel.getModel()).thenReturn(model);

        placeAt(0, Pawn.PAWN_BLACK, stageModel);
        placeAt(2, Pawn.PAWN_BLACK, stageModel);

        assertTrue(board.formsMill(1, Pawn.PAWN_BLACK));
    }

    @Test
    void testFormsMill_false() {
        assertFalse(board.formsMill(0, Pawn.PAWN_BLACK));
    }


    private void placeAt(int posIndex, int color, MerelleStageModel stageModel) {
        Pawn pawn = new Pawn(color, stageModel);
        int[] rc = MerelleBoard.posToRowCol(posIndex);
        board.addElement(pawn, rc[0], rc[1]);
    }
}