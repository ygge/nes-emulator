package nu.ygge.nes.emulator.gui;

import nu.ygge.nes.emulator.NESRuntime;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class EmulatorGui {

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

        for (int b = 0; b < 2; ++b) {
            for (int t = 0; t < 256; ++t) {
                var tile = runtime.getPpu().getTile(b, t);
                frame.addTile(tile);
            }
        }
        frame.repaint();
    }
}
