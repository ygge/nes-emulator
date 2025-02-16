package nu.ygge.nes.emulator.gui;

import nu.ygge.nes.emulator.ppu.Frame;
import nu.ygge.nes.emulator.ppu.Tile;

import javax.swing.*;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

public class EmulatorPanel extends JPanel {

    private static final int SCALE = 4;
    private static final int WIDTH = 256 * SCALE;
    private static final int HEIGHT = 240 * SCALE;

    private final Map<String, Color> colorMap = new HashMap<>();
    private Frame currentFrame;

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(WIDTH, HEIGHT);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);

        if (currentFrame != null) {
            Tile[][] background = currentFrame.getBackground();
            for (int y = 0; y < background.length; ++y) {
                for (int x = 0; x < background[y].length; ++x) {
                    paintTile(g, x * 8 * SCALE, y * 8 * SCALE, background[y][x]);
                }
            }
        }
    }

    private void paintTile(Graphics g, int x, int y, Tile tile) {
        for (int yy = 0; yy < 8; ++yy) {
            for (int xx = 0; xx < 8; ++xx) {
                g.setColor(getColor(tile, tile.getData()[yy][xx]));
                g.fillRect(x + xx * SCALE, y + yy * SCALE, SCALE, SCALE);
            }
        }
    }

    private Color getColor(Tile tile, byte value) {
        var str = tile.getPalette()[value];
        if (!colorMap.containsKey(str)) {
            int v = Integer.parseInt(str, 16);
            colorMap.put(str, new Color((v >> 16), (v >> 8) & 0xFF, v & 0xFF));
        }
        return colorMap.get(str);
    }

    public void setPpuFrame(Frame ppuFrame) {
        currentFrame = ppuFrame;
        repaint();
    }
}
