package nu.ygge.nes.emulator.gui;

import nu.ygge.nes.emulator.NESRuntime;
import nu.ygge.nes.emulator.bus.EmulatorBus;
import nu.ygge.nes.emulator.ppu.PPU;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class EmulatorGui {

    @SuppressWarnings("resource")
    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalStateException("Program must be started with path to ROM as only parameter");
        }
        var fileName = args[0];
        InputStream in;
        if (fileName.startsWith("/")) {
            in = EmulatorGui.class.getResourceAsStream(fileName);
        } else {
            in = new FileInputStream(fileName);
        }
        if (in == null) {
            throw new IllegalStateException(String.format("File '%s' not found", fileName));
        }
        var data = in.readAllBytes();
        runGame(fileName, data);
    }

    private static void runGame(String fileName, byte[] data) {
        var frame = new EmulatorFrame(fileName);
        var runtime = new NESRuntime();
        runtime.loadGame(data);
        runtime.reset();

        var bus = (EmulatorBus)runtime.getBus();

        int paletteIndex = 0;
        for (int b = 0; b < 2; ++b) {
            for (int t = 0; t < 256; ++t) {
                var tile = bus.getPpu().getTile(b, t);
                frame.addTile(tile);
            }
        }
        while (true) {
            frame.setPalette(PPU.COLOR_PALETTES[paletteIndex]);
            frame.repaint();
            if (++paletteIndex == PPU.COLOR_PALETTES.length) {
                paletteIndex = 0;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
