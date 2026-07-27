package nu.ygge.nes.emulator.gui;

import nu.ygge.nes.emulator.input.Controller;
import nu.ygge.nes.emulator.ppu.Frame;

import javax.swing.*;
import java.awt.*;
import java.awt.event.KeyListener;
import java.awt.image.BufferedImage;

public class EmulatorFrame extends JFrame {

    private final EmulatorPanel panel = new EmulatorPanel();

    public EmulatorFrame(String name, Controller controller, KeyListener hotkeys) {
        super(name);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(panel);
        addKeyListener(new ControllerKeyListener(controller));
        addKeyListener(hotkeys);
        setFocusable(true);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        requestFocusInWindow();
    }

    public void setFrame(Frame ppuFrame) {
        panel.setPpuFrame(ppuFrame);
    }

    public BufferedImage screenshot() {
        return panel.snapshot();
    }
}
