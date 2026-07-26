package nu.ygge.nes.emulator.gui;

import nu.ygge.nes.emulator.input.Controller;
import nu.ygge.nes.emulator.ppu.Frame;

import javax.swing.*;
import java.awt.*;

public class EmulatorFrame extends JFrame {

    private final EmulatorPanel panel = new EmulatorPanel();

    public EmulatorFrame(String name, Controller controller) {
        super(name);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(panel);
        addKeyListener(new ControllerKeyListener(controller));
        setFocusable(true);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
        requestFocusInWindow();
    }

    public void setFrame(Frame ppuFrame) {
        panel.setPpuFrame(ppuFrame);
    }
}
