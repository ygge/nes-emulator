package nu.ygge.nes.emulator.gui;

import nu.ygge.nes.emulator.ppu.Tile;

import javax.swing.*;
import java.awt.*;

public class EmulatorFrame extends JFrame {

    private final EmulatorPanel panel = new EmulatorPanel();

    public EmulatorFrame(String name) {
        super(name);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(panel);
        //addKeyListener(this);
        pack();
        setLocationRelativeTo(null);
        setVisible(true);
    }

    public void addTile(Tile tile) {
        panel.addTile(tile);
    }
}
