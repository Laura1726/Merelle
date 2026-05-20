package view;

import boardifier.model.ContainerElement;
import boardifier.view.ContainerLook;
import model.MerellePawnPot;
import model.Pawn;

/**
 * MerellePawnPotLook renders a pawn pot (reserve) as a row of symbols.
 *
 * Example output for black pot with 6 remaining pawns:
 *   Black reserve: [B][B][B][B][B][B][ ][ ][ ]
 *
 * Example output for white pot with 3 remaining pawns:
 *   White reserve: [W][W][W][ ][ ][ ][ ][ ][ ]
 *
 * Each slot is shown as:
 *   [B] or [W] if a pawn is present
 *   [ ]        if the slot is empty (pawn has been placed on the board)
 */
public class MerellePawnPotLook extends ContainerLook {

    private final MerellePawnPot pot;
    private final String label;

    /**
     * @param containerElement  the pot model element
     * @param label             "Black reserve" or "White reserve"
     */
    public MerellePawnPotLook(ContainerElement containerElement, String label) {
        super(containerElement, 0);
        this.pot = (MerellePawnPot) containerElement;
        this.label = label;
    }

    @Override
    protected void render() {
    }

    /**
     * Returns a single-line text representation of the pot.
     */
    public String toText() {
        StringBuilder sb = new StringBuilder();
        sb.append(label).append(": ");
        for (int col = 0; col < 9; col++) {
            if (!pot.isEmptyAt(0, col)) {
                Pawn p = (Pawn) pot.getElement(0, col);
                sb.append(p.getColor() == Pawn.PAWN_BLACK ? "[B]" : "[W]");
            } else {
                sb.append("[ ]");
            }
        }
        return sb.toString();
    }
}