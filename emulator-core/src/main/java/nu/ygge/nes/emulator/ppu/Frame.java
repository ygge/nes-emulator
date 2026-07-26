package nu.ygge.nes.emulator.ppu;

import lombok.Getter;

/**
 * A rendered screen, one byte per pixel holding an index into the system palette.
 */
@Getter
public class Frame {

    public static final int WIDTH = 256;
    public static final int HEIGHT = 240;

    private final byte[] pixels = new byte[WIDTH * HEIGHT];

    public void setPixel(int x, int y, byte colorIndex) {
        pixels[y * WIDTH + x] = colorIndex;
    }

    public byte getPixel(int x, int y) {
        return pixels[y * WIDTH + x];
    }

    public void copyTo(Frame target) {
        System.arraycopy(pixels, 0, target.pixels, 0, pixels.length);
    }
}
