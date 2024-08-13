package nu.ygge.nes.emulator.gui;

import lombok.Setter;
import nu.ygge.nes.emulator.ppu.Frame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.function.BiFunction;

public class EmulatorFrame extends JFrame implements KeyListener {

    private final EmulatorPanel panel = new EmulatorPanel();
    @Setter
    private BiFunction<JoypadKey, Boolean, Boolean> keyCallback;

    public EmulatorFrame(String name) {
        super(name);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(panel);
        //addKeyListener(this);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        addKeyListener(this);
    }

    public void setFrame(Frame ppuFrame) {
        panel.setPpuFrame(ppuFrame);
    }

    @Override
    public void keyTyped(KeyEvent e) {

    }

    @Override
    public void keyPressed(KeyEvent e) {
        keyCallback(e, true);
    }

    @Override
    public void keyReleased(KeyEvent e) {
        keyCallback(e, false);
    }

    private void keyCallback(KeyEvent e, boolean set) {
        if (e.getKeyCode() == KeyEvent.VK_UP) {
            keyCallback.apply(JoypadKey.UP, set);
        } else if (e.getKeyCode() == KeyEvent.VK_RIGHT) {
            keyCallback.apply(JoypadKey.RIGHT, set);
        } else if (e.getKeyCode() == KeyEvent.VK_DOWN) {
            keyCallback.apply(JoypadKey.DOWN, set);
        } else if (e.getKeyCode() == KeyEvent.VK_LEFT) {
            keyCallback.apply(JoypadKey.LEFT, set);
        } else if (e.getKeyCode() == KeyEvent.VK_ENTER) {
            keyCallback.apply(JoypadKey.START, set);
        } else if (e.getKeyCode() == KeyEvent.VK_SHIFT) {
            keyCallback.apply(JoypadKey.SELECT, set);
        } else if (e.getKeyCode() == KeyEvent.VK_Z) {
            keyCallback.apply(JoypadKey.BUTTON_A, set);
        } else if (e.getKeyCode() == KeyEvent.VK_X) {
            keyCallback.apply(JoypadKey.BUTTON_B, set);
        }
    }
}
