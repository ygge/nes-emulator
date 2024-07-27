package nu.ygge.nes.snake.gui;

import javax.swing.*;
import java.awt.*;

public class SnakeFrame extends JFrame {

    public SnakeFrame(byte[][] data) {
        super("Snake!");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(new SnakePanel(data));
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }
}
