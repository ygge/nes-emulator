package nu.ygge.nes.emulator.ppu;

import lombok.Getter;

@Getter
public class Tile {

    private final String[] palette;
    private final byte[][] data = new byte[8][8];

    public Tile(String[] palette) {
        this.palette = palette;
    }

    public void add(int x, int y, byte value) {
        data[y][x] += value;
    }
}
