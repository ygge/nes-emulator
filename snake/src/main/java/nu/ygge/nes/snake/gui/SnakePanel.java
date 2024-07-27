package nu.ygge.nes.snake.gui;

import javax.swing.*;
import java.awt.*;

public class SnakePanel extends JPanel {

    private static final int SCALE = 20;
    private final byte[][] data;

    public SnakePanel(byte[][] data) {

        this.data = data;
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(data.length * SCALE, data[0].length * SCALE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        for (int y = 0; y < data.length; ++y) {
            for (int x = 0; x < data[y].length; ++x) {
                g.setColor(getColor(data[y][x]));
                g.fillRect(x * SCALE, y * SCALE, SCALE, SCALE);
            }
        }
    }

    private Color getColor(byte state) {
        return switch (state) {
            case 1 -> Color.WHITE;
            case 2, 9 -> Color.GRAY;
            case 3, 10 -> Color.RED;
            case 4, 11 -> Color.GREEN;
            case 5, 12 -> Color.BLUE;
            case 6, 13 -> Color.MAGENTA;
            case 7, 14 -> Color.YELLOW;
            default -> Color.BLACK;
        };
    }
}
