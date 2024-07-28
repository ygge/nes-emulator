package nu.ygge.nes.emulator.gui;

import nu.ygge.nes.emulator.ppu.Tile;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class EmulatorPanel extends JPanel {

    private static final int SCALE = 4;
    private static final int WIDTH = 256 * SCALE;
    private static final int HEIGHT = 240 * SCALE;
    private final List<Tile> tiles = new ArrayList<>();
    private final Color[] palette = new Color[4];

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, Math.max(HEIGHT, tiles.size() * 8 / 32 * SCALE));
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        int x = 0;
        int y = 0;
        for (Tile tile : tiles) {
            paintTile(g, x, y, tile);
            x += 9 * SCALE;
            if (x + 9 * SCALE >= WIDTH) {
                x = 0;
                y += 9 * SCALE;
            }
        }
    }

    public void addTile(Tile tile) {
        tiles.add(tile);
    }

    public void setPalette(String[] colorPalette) {
        for (int i = 0; i < palette.length; ++i) {
            int v = Integer.parseInt(colorPalette[i], 16);
            palette[i] = new Color((v >> 16), (v >> 8) & 0xFF, v & 0xFF);
        }
    }

    private void paintTile(Graphics g, int x, int y, Tile tile) {
        for (int yy = 0; yy < 8; ++yy) {
            for (int xx = 0; xx < 8; ++xx) {
                g.setColor(getColor(tile.getData()[yy][xx]));
                g.fillRect(x + xx * SCALE, y + yy * SCALE, SCALE, SCALE);
            }
        }
    }

    private Color getColor(byte value) {
        return palette[value];
    }
}
