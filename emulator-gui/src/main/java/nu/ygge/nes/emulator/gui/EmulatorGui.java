package nu.ygge.nes.emulator.gui;

import nu.ygge.nes.emulator.NESRuntime;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;

public class EmulatorGui {

    public static void main(String[] args) throws IOException {
        if (args.length != 1) {
            throw new IllegalStateException("Program must be started with path to ROM as only parameter");
        }
        var fileName = args[0];
        var data = readFile(fileName);
        runGame(fileName.startsWith("/") ? fileName.substring(1) : fileName, data);
    }

    private static byte[] readFile(String fileName) throws IOException {
        var in = getInputStream(fileName);
        if (in == null) {
            throw new IllegalStateException(String.format("File '%s' not found", fileName));
        }
        return in.readAllBytes();
    }

    private static InputStream getInputStream(String fileName) throws FileNotFoundException {
        if (fileName.startsWith("/")) {
            return EmulatorGui.class.getResourceAsStream(fileName);
        }
        return new FileInputStream(fileName);
    }

    private static void runGame(String fileName, byte[] data) {
        var frame = new EmulatorFrame(fileName);
        var runtime = new NESRuntime(frame::setFrame);
        frame.setKeyCallback((key, set) -> {
            runtime.keyCallback(key.getBitValue(), set);
            return true;
        });
        runtime.loadGame(data);
        runtime.reset();

        while (true) {
            runtime.performSingleInstruction();
        }
    }
}
