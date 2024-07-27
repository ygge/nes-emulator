package nu.ygge.nes.snake.gui;

import nu.ygge.nes.snake.Move;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.function.Consumer;

public class SnakeFrame extends JFrame implements KeyListener {

    private final Consumer<Move> moveCallback;

    public SnakeFrame(byte[][] data, Consumer<Move> moveCallback) {
        super("Snake!");
        this.moveCallback = moveCallback;
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(new SnakePanel(data));
        addKeyListener(this);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {

    }

    @Override
    public void keyReleased(KeyEvent e) {
        switch (e.getKeyCode()) {
            case KeyEvent.VK_UP -> moveCallback.accept(Move.UP);
            case KeyEvent.VK_RIGHT -> moveCallback.accept(Move.RIGHT);
            case KeyEvent.VK_DOWN -> moveCallback.accept(Move.DOWN);
            case KeyEvent.VK_LEFT -> moveCallback.accept(Move.LEFT);
        }
    }
}
