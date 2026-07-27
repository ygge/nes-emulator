package nu.ygge.nes.emulator.gui;

import nu.ygge.nes.emulator.ppu.Frame;
import nu.ygge.nes.emulator.ppu.SystemPalette;

import javax.swing.*;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferInt;

public class EmulatorPanel extends JPanel {

    private static final int SCALE = 3;

    private final BufferedImage[] buffers = new BufferedImage[2];
    private final int[][] bufferPixels = new int[2][];
    private volatile BufferedImage visibleBuffer;
    private int backBuffer;

    public EmulatorPanel() {
        for (int i = 0; i < buffers.length; ++i) {
            buffers[i] = new BufferedImage(Frame.WIDTH, Frame.HEIGHT, BufferedImage.TYPE_INT_RGB);
            bufferPixels[i] = ((DataBufferInt) buffers[i].getRaster().getDataBuffer()).getData();
        }
        visibleBuffer = buffers[buffers.length - 1];
        setBackground(Color.BLACK);
    }

    @Override
    public Dimension getPreferredSize() {
        return new Dimension(Frame.WIDTH * SCALE, Frame.HEIGHT * SCALE);
    }

    @Override
    protected void paintComponent(Graphics g) {
        super.paintComponent(g);
        var graphics = (Graphics2D) g;
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics.drawImage(visibleBuffer, 0, 0, getWidth(), getHeight(), null);
    }

    /**
     * A standalone copy of the frame currently on screen, upscaled the same way it is drawn in the
     * window (nearest neighbour). Copying detaches it from the rotating buffers so it stays intact
     * even as emulation keeps producing new frames.
     */
    public BufferedImage snapshot() {
        var source = visibleBuffer;
        var copy = new BufferedImage(source.getWidth() * SCALE, source.getHeight() * SCALE,
                BufferedImage.TYPE_INT_RGB);
        var graphics = copy.createGraphics();
        graphics.setRenderingHint(RenderingHints.KEY_INTERPOLATION,
                RenderingHints.VALUE_INTERPOLATION_NEAREST_NEIGHBOR);
        graphics.drawImage(source, 0, 0, copy.getWidth(), copy.getHeight(), null);
        graphics.dispose();
        return copy;
    }

    /**
     * Called from the emulation thread. The finished image is handed over to the event dispatch
     * thread by swapping buffers, so that painting never sees a half written frame.
     */
    public void setPpuFrame(Frame ppuFrame) {
        var target = bufferPixels[backBuffer];
        var source = ppuFrame.getPixels();
        for (int i = 0; i < target.length; ++i) {
            target[i] = SystemPalette.toRgb(source[i]);
        }
        visibleBuffer = buffers[backBuffer];
        backBuffer = (backBuffer + 1) % buffers.length;
        repaint();
    }
}
