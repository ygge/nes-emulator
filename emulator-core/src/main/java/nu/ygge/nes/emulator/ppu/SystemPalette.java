package nu.ygge.nes.emulator.ppu;

/**
 * The 64 colours the PPU can produce, laid out as 16 hues with 4 brightness levels each.
 * A colour index as stored in palette RAM has the hue in the low nibble and the level in the high nibble.
 */
public final class SystemPalette {

    private static final String[][] HUES = {
            new String[]{ "626262", "ABABAB", "FFFFFF", "FFFFFF" },
            new String[]{ "002E98", "0064F4", "4AB5FF", "B6E1FF" },
            new String[]{ "0C11C2", "353CFF", "858CFF", "CED1FF" },
            new String[]{ "3B00C2", "761BFF", "C86AFF", "E9C3FF" },
            new String[]{ "650098", "AE0AF4", "FF58FF", "FFBCFF" },
            new String[]{ "7D004E", "CF0C8F", "FF5BE2", "FFBDF4" },
            new String[]{ "7D0000", "CF231C", "FF726A", "FFC6C3" },
            new String[]{ "651900", "AE4700", "FF9702", "FFD59A" },
            new String[]{ "3B3600", "766F00", "C8C100", "E9E681" },
            new String[]{ "0C4F00", "359000", "85E300", "CEF481" },
            new String[]{ "005B00", "00A100", "4AF502", "B6FB9A" },
            new String[]{ "005900", "009E1C", "29F26A", "A9FAC3" },
            new String[]{ "00494E", "00888F", "29DBE2", "A9F0F4" },
            new String[]{ "000000", "000000", "4E4E4E", "B8B8B8" },
            new String[]{ "000000", "000000", "000000", "000000" },
            new String[]{ "000000", "000000", "000000", "000000" },
    };

    private static final int[] RGB = new int[64];

    static {
        for (int hue = 0; hue < HUES.length; ++hue) {
            for (int level = 0; level < HUES[hue].length; ++level) {
                RGB[(level << 4) | hue] = Integer.parseInt(HUES[hue][level], 16);
            }
        }
    }

    private SystemPalette() {
    }

    public static int toRgb(int colorIndex) {
        return RGB[colorIndex & 0x3f];
    }
}
