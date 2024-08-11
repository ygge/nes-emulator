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
        var runtime = new NESRuntime(frame::setFrame);
        runtime.loadGame(data);
        runtime.reset();

        while (true) {
            runtime.performSingleInstruction();
        }
    }
}
