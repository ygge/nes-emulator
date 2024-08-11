package nu.ygge.nes.emulator.ppu;

import java.util.ArrayList;
import java.util.List;

public class MaskRegister {

    private static final short GREYSCALE                = 0b00000001;
    private static final short LEFTMOST_8PXL_BACKGROUND = 0b00000010;
    private static final short LEFTMOST_8PXL_SPRITE     = 0b00000100;
    private static final short SHOW_BACKGROUND          = 0b00001000;
    private static final short SHOW_SPRITES             = 0b00010000;
    private static final short EMPHASISE_RED            = 0b00100000;
    private static final short EMPHASISE_GREEN          = 0b01000000;
    private static final short EMPHASISE_BLUE           = 0b10000000;

    private short register = 0;

    public void update(byte data) {
        register = data;
        if (register < 0) {
            register += 256;
        }
    }

    public boolean isGrayscale() {
        return (register & GREYSCALE) != 0;
    }

    public boolean isLeftMost8pxlBackground() {
        return (register & LEFTMOST_8PXL_BACKGROUND) != 0;
    }

    public boolean isLeftMost8pxlSprite() {
        return (register & LEFTMOST_8PXL_SPRITE) != 0;
    }

    public boolean isShowBackground() {
        return (register & SHOW_BACKGROUND) != 0;
    }

    public boolean isShowSprite() {
        return (register & SHOW_SPRITES) != 0;
    }

    public List<PPUColor> emphasise() {
        var colors = new ArrayList<PPUColor>();
        if ((register & EMPHASISE_RED) != 0) {
            colors.add(PPUColor.RED);
        }
        if ((register & EMPHASISE_GREEN) != 0) {
            colors.add(PPUColor.GREEN);
        }
        if ((register & EMPHASISE_BLUE) != 0) {
            colors.add(PPUColor.BLUE);
        }
        return colors;
    }
}
