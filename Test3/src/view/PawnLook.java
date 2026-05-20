package view;

import boardifier.model.GameElement;
import boardifier.view.ElementLook;
import model.Pawn;

/**
 * PawnLook renders a Pawn as a single colored character.
 *
 * BLACK pawn → 'B'
 * WHITE pawn → 'W'
 */
public class PawnLook extends ElementLook {

    public PawnLook(GameElement element) {
        super(element);
    }

    @Override
    protected void render() {
    }

    /**
     * Returns the single-character symbol for this pawn.
     * Used by MerelleBoardLook when drawing the board.
     */
    public String getText() {
        Pawn pawn = (Pawn) element;
        return (pawn.getColor() == Pawn.PAWN_BLACK) ? "B" : "W";
    }
}