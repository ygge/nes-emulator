package nu.ygge.nes.emulator.ppu;

/**
 * The scroll position and the video memory address, which the hardware keeps in a single pair of
 * fifteen bit registers instead of separate ones. Writes to $2000, $2005 and $2006 all land in the
 * same temporary register, so a write to one of them moves the others: pointing the address register
 * somewhere to update video memory also moves the scroll position.
 * <p>
 * Rendering reads a second copy of the register, which is taken over from the temporary one at two
 * points. The horizontal half is taken over at the start of every scanline, so a write halfway
 * through a frame scrolls the remainder of the picture, while the vertical half is only taken over
 * between frames. Together that is what lets a game hold a status bar still at the top of the screen
 * while the playfield scrolls underneath it.
 */
public class ScrollAddress {

    private static final int COARSE_X     = 0b000_00_00000_11111;
    private static final int COARSE_Y     = 0b000_00_11111_00000;
    private static final int NAME_TABLE_X = 0b000_01_00000_00000;
    private static final int NAME_TABLE_Y = 0b000_10_00000_00000;
    private static final int FINE_Y       = 0b111_00_00000_00000;
    private static final int NAME_TABLE = NAME_TABLE_X | NAME_TABLE_Y;
    private static final int HORIZONTAL = NAME_TABLE_X | COARSE_X;
    private static final int REGISTER_MASK = 0x7fff;
    private static final int ADDRESS_MASK = 0x3fff;
    private static final int TILE_ROWS = 30;

    private int temporary, current, fineX;
    private boolean secondWrite;

    /**
     * The two name table bits of $2000. They only reach rendering by way of the temporary register,
     * which means a later address write overrides them.
     */
    public void writeNameTable(byte data) {
        temporary = (temporary & ~NAME_TABLE) | ((data & 0b11) << 10);
    }

    /**
     * $2005 takes the horizontal scroll first and the vertical scroll second.
     */
    public void writeScroll(byte data) {
        var value = data & 0xff;
        if (secondWrite) {
            temporary = (temporary & ~(COARSE_Y | FINE_Y)) | ((value & 0b1111_1000) << 2) | ((value & 0b111) << 12);
        } else {
            temporary = (temporary & ~COARSE_X) | (value >> 3);
            fineX = value & 0b111;
        }
        secondWrite = !secondWrite;
    }

    /**
     * $2006 takes the high byte of the address first and the low byte second, and only the second
     * write reaches the register that rendering and memory access read.
     */
    public void writeAddress(byte data) {
        var value = data & 0xff;
        if (secondWrite) {
            temporary = (temporary & ~0xff) | value;
            current = temporary;
        } else {
            temporary = (temporary & 0xff) | ((value & 0b0011_1111) << 8);
        }
        secondWrite = !secondWrite;
    }

    /**
     * Both scroll and address writes take two goes, and the register keeps track of which one is
     * next. Reading the status register abandons a half finished pair.
     */
    public void resetWriteLatch() {
        secondWrite = false;
    }

    public int getAddress() {
        return current & ADDRESS_MASK;
    }

    public void incrementAddress(int increment) {
        current = (current + increment) & REGISTER_MASK;
    }

    public void beginFrame() {
        current = temporary;
    }

    public void beginScanline() {
        current = (current & ~HORIZONTAL) | (temporary & HORIZONTAL);
    }

    /**
     * Steps down one row, rolling over into the name table below once the last row of tiles is done.
     * A game that has pointed the register into the attribute table instead rolls over early, just
     * as the hardware does.
     */
    public void endScanline() {
        if ((current & FINE_Y) != FINE_Y) {
            current += 0b001_00_00000_00000;
            return;
        }
        current &= ~FINE_Y;
        var row = (current & COARSE_Y) >> 5;
        if (row == TILE_ROWS - 1) {
            row = 0;
            current ^= NAME_TABLE_Y;
        } else {
            row = (row + 1) & 0b1_1111;
        }
        current = (current & ~COARSE_Y) | (row << 5);
    }

    public int getNameTable() {
        return 0x2000 | (current & NAME_TABLE);
    }

    public int getTileRow() {
        return (current & COARSE_Y) >> 5;
    }

    public int getTileColumn() {
        return current & COARSE_X;
    }

    public int getRowWithinTile() {
        return (current & FINE_Y) >> 12;
    }

    public int getColumnWithinTile() {
        return fineX;
    }
}
