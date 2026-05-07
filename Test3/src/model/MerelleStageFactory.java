package model;

import boardifier.model.StageElementsFactory;
import boardifier.model.TextElement;

/**
 * MerelleStageFactory creates and initialises all game elements for a Merelle party.
 *
 * Elements created:
 *  - 1 MerelleBoard at virtual position (0, 2)
 *  - 1 MerellePawnPot "blackpot" at (15, 2)
 *  - 1 MerellePawnPot "whitepot" at (15, 5)
 *  - 9 black Pawn instances in the black pot (row 0, columns 0..8)
 *  - 9 white Pawn instances in the white pot (row 0, columns 0..8)
 *  - 1 TextElement showing the current player name at (0, 0)
 *
 * API note: the constructor receives MerelleStageModel directly and passes it
 * to super(). All elements are created with stageModel as their GameStageModel
 * argument — there is no separate gameStageModel variable (confirmed by PuissanceXStageFactory).
 */
public class MerelleStageFactory extends StageElementsFactory {

    private final MerelleStageModel stageModel;

    public MerelleStageFactory(MerelleStageModel stageModel) {
        super(stageModel);
        this.stageModel = stageModel;
    }

    @Override
    public void setup() {

        // ---- Board ----
        MerelleBoard board = new MerelleBoard(0, 2, stageModel);
        stageModel.setBoard(board);

        // ---- Pawn pots ----
        MerellePawnPot blackPot = new MerellePawnPot("blackpot", 15, 2, stageModel);
        stageModel.setBlackPot(blackPot);

        MerellePawnPot whitePot = new MerellePawnPot("whitepot", 15, 5, stageModel);
        stageModel.setWhitePot(whitePot);

        // ---- Black pawns ----
        Pawn[] blackPawns = new Pawn[9];
        for (int i = 0; i < 9; i++) {
            blackPawns[i] = new Pawn(Pawn.PAWN_BLACK, stageModel);
            blackPot.addElement(blackPawns[i], 0, i);
        }
        stageModel.setBlackPawns(blackPawns);

        // ---- White pawns ----
        Pawn[] whitePawns = new Pawn[9];
        for (int i = 0; i < 9; i++) {
            whitePawns[i] = new Pawn(Pawn.PAWN_WHITE, stageModel);
            whitePot.addElement(whitePawns[i], 0, i);
        }
        stageModel.setWhitePawns(whitePawns);

        // ---- Player name text element ----
        TextElement playerName = new TextElement(
                stageModel.getModel().getCurrentPlayerName(), stageModel);
        playerName.setLocation(0, 0);
        stageModel.setPlayerName(playerName);
    }
}