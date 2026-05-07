package model;

import boardifier.model.ContainerElement;
import boardifier.model.ElementTypes;
import boardifier.model.GameStageModel;

/**
 * MerellePawnPot holds the pawns that a player has not yet placed on the board.
 * Each player has their own pot with a unique name ("blackpot" or "whitepot").
 * The pot is a 1-row, 9-column ContainerElement (one slot per pawn).
 */
public class MerellePawnPot extends ContainerElement {

    public static final String POT_NAME = "pawnpot";
    private static final int POT_TYPE_ID = 51;

    static {
        ElementTypes.register(POT_NAME, POT_TYPE_ID);
    }

    /**
     * @param name            unique name for this pot ("blackpot" or "whitepot")
     * @param x               column position in the virtual character grid
     * @param y               row position in the virtual character grid
     * @param gameStageModel  the stage model this element belongs to
     */
    public MerellePawnPot(String name, int x, int y, GameStageModel gameStageModel) {
        // 1 row, 9 columns — one slot per pawn
        super(name, x, y, 1, 9, gameStageModel);
        this.type = ElementTypes.getType(POT_NAME);
    }
}