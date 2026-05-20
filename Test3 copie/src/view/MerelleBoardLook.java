package view;

import boardifier.model.ContainerElement;
import boardifier.view.ContainerLook;
import model.MerelleBoard;
import model.Pawn;

/**
 * MerelleBoardLook renders the Merelle board.
 *
 * The 24 valid positions are laid out in a 7x7 virtual grid.
 * Only 24 specific intersections are valid positions; the rest are either
 * connecting lines or empty space.
 *
 * Visual layout with position indices (0..23):
 *
 *  0-----------1-----------2
 *  |           |           |
 *  |   3-------4-------5   |
 *  |   |       |       |   |
 *  |   |   6---7---8   |   |
 *  |   |   |       |   |   |
 *  9--10--11      12--13--14
 *  |   |   |       |   |   |
 *  |   |  15--16--17   |   |
 *  |   |       |       |   |
 *  |  18------19------20   |
 *  |           |           |
 * 21----------22----------23
 *
 * Each position shows:
 *   - the position index (2 chars) if empty
 *   - 'B' if a black pawn occupies it
 *   - 'W' if a white pawn occupies it
 *
 * Reachable cells (highlighted for human player) are shown with brackets: [B], [W], [ 7]
 */
public class MerelleBoardLook extends ContainerLook {

    private final MerelleBoard board;

    public MerelleBoardLook(ContainerElement containerElement) {
        super(containerElement, 0);
        this.board = (MerelleBoard) containerElement;
    }

    @Override
    protected void render() {
    }

    /**
     * Renders the board as a multiline ASCII string.
     */
    public String toText() {
        String[] cells = new String[24];
        boolean[][] reachable = board.getReachableCells();

        for (int i = 0; i < 24; i++) {
            int[] rc = MerelleBoard.POS_TO_ROWCOL[i];
            boolean isReachable = reachable[rc[0]][rc[1]];

            Pawn p = board.getPawnAt(i);
            String symbol;
            if (p == null) {
                symbol = String.format("%2d", i);
            } else {
                symbol = (p.getColor() == Pawn.PAWN_BLACK) ? " B" : " W";
            }

            if (isReachable) {
                cells[i] = "[" + symbol.trim() + "]";
            } else {
                cells[i] = " " + symbol + " ";
            }
        }

        for (int i = 0; i < 24; i++) {
            while (cells[i].length() < 4) cells[i] = " " + cells[i];
            if (cells[i].length() > 4) cells[i] = cells[i].substring(0, 4);
        }


        String h = "----";
        String v = "|   ";
        String s = "    ";
        String nl = "\n";

        StringBuilder sb = new StringBuilder();


        sb.append("   ");
        sb.append("  0   ").append("        ").append("  1   ").append("        ").append("  2  ").append(nl);

        // Row 0: positions  0 --------- 1 --------- 2
        sb.append("   ").append(cells[0]).append(h).append(h).append(h).append(h)
                .append(cells[1]).append(h).append(h).append(h).append(h).append(cells[2]).append(nl);

        // Row 0→1 verticals
        sb.append("   ").append(v).append(s).append(s).append(s).append(v).append(s).append(s).append(s).append(v).append(nl);

        // Row 1: positions  3 ----- 4 ----- 5
        sb.append("   3   ").append(cells[3]).append(h).append(h).append(cells[4]).append(h).append(h).append(cells[5]).append("   5").append(nl);

        // Row 1→2 verticals
        sb.append("       ").append(v).append(s).append(v).append(s).append(v).append(nl);

        // Row 2: positions  6 -- 7 -- 8
        sb.append("   6       ").append(cells[6]).append(h).append(cells[7]).append(h).append(cells[8]).append("       8").append(nl);

        // Row 2→3 verticals (left: 9,10,11  right: 12,13,14)
        sb.append("   ").append(v).append(s).append(v).append(s).append(s).append(s).append(s).append(s).append(v).append(s).append(v).append(nl);

        // Row 3: positions  9 - 10 - 11         12 - 13 - 14
        sb.append("   ").append(cells[9]).append(h).append(cells[10]).append(h).append(cells[11])
                .append(s).append(s).append(s)
                .append(cells[12]).append(h).append(cells[13]).append(h).append(cells[14]).append(nl);

        // Row 3→4 verticals
        sb.append("   ").append(v).append(s).append(v).append(s).append(s).append(s).append(s).append(s).append(v).append(s).append(v).append(nl);

        // Row 4: positions  15 - 16 - 17
        sb.append("  15       ").append(cells[15]).append(h).append(cells[16]).append(h).append(cells[17]).append("      17").append(nl);

        // Row 4→5 verticals
        sb.append("       ").append(v).append(s).append(v).append(s).append(v).append(nl);

        // Row 5: positions  18 ----- 19 ----- 20
        sb.append("  18   ").append(cells[18]).append(h).append(h).append(cells[19]).append(h).append(h).append(cells[20]).append("  20").append(nl);

        // Row 5→6 verticals
        sb.append("   ").append(v).append(s).append(s).append(s).append(v).append(s).append(s).append(s).append(v).append(nl);

        // Row 6: positions  21 --------- 22 --------- 23
        sb.append("  21  ").append(cells[21]).append(h).append(h).append(h).append(h)
                .append(cells[22]).append(h).append(h).append(h).append(h).append(cells[23]).append("  23").append(nl);

        return sb.toString();
    }
}