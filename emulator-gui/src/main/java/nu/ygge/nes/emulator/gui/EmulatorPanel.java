package nu.ygge.nes.emulator.gui;

import nu.ygge.nes.emulator.ppu.Tile;

import javax.swing.*;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class EmulatorPanel extends JPanel {

    private static final int SCALE = 4;
    private static final int WIDTH = 256 * SCALE;
    private static final int HEIGHT = 240 * SCALE;

    private static final List<Tile> tiles = new ArrayList<>();

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

    private void paintTile(Graphics g, int x, int y, Tile tile) {
        for (int yy = 0; yy < 8; ++yy) {
            for (int xx = 0; xx < 8; ++xx) {
                g.setColor(getColor(tile.getData()[yy][xx]));
                g.fillRect(x + xx * SCALE, y + yy * SCALE, SCALE, SCALE);
            }
        }
    }

    private Color getColor(byte value) {
        return switch (value) {
            case 0 -> Color.WHITE;
            case 1 -> Color.RED;
            case 2 -> Color.YELLOW;
            case 3 -> Color.ORANGE.darker();
            default -> throw new IllegalStateException(String.format("Color value %d is not supported", value));
        };
    }

    public void addTile(Tile tile) {
        tiles.add(tile);
    }
}
