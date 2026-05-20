package model;

import boardifier.model.ElementTypes;
import boardifier.model.GameElement;
import boardifier.model.GameStageModel;

/**
 * A pawn for the Merelle (Nine Men's Morris) game.
 * Each pawn has a single attribute: its color (BLACK or WHITE).
 * All pawns of the same color are equivalent — there is no number.
 */
public class Pawn extends GameElement {

    public static final int PAWN_BLACK = 0;
    public static final int PAWN_WHITE = 1;

    public static final String PAWN_NAME = "pawn";
    private static final int PAWN_TYPE_ID = 50;

    // Register the element type once, at class-loading time (same pattern as PuissanceXDisk)
    static {
        ElementTypes.register(PAWN_NAME, PAWN_TYPE_ID);
    }

    private final int color;

    public Pawn(int color, GameStageModel gameStageModel) {
        super(gameStageModel);
        if (color != PAWN_BLACK && color != PAWN_WHITE) {
            throw new IllegalArgumentException("Invalid pawn color: " + color);
        }
        this.color = color;
        this.type = ElementTypes.getType(PAWN_NAME);
    }

    public int getColor() {
        return color;
    }
}