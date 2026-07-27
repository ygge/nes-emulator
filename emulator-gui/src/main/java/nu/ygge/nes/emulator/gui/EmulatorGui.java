package nu.ygge.nes.emulator.gui;

import nu.ygge.nes.emulator.NESRuntime;
import nu.ygge.nes.emulator.apu.APU;

import javax.swing.*;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.LockSupport;

public class EmulatorGui {

    private static final long FRAME_INTERVAL_NANOS = 1_000_000_000L / 60;

    public static void main(String[] args) throws IOException, InterruptedException, InvocationTargetException {
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

    private static void runGame(String fileName, byte[] data) throws InterruptedException, InvocationTargetException {
        var window = new AtomicReference<EmulatorFrame>();
        var apu = new AtomicReference<APU>();
        var audio = new AudioPlayer();
        var pacer = new FramePacer();
        var samples = new short[AudioPlayer.SAMPLE_RATE / 10];
        var runtime = new NESRuntime(ppuFrame -> {
            window.get().setFrame(ppuFrame);
            if (audio.isActive()) {
                audio.play(samples, apu.get().readSamples(samples));
            }
            pacer.awaitNextFrame();
        });
        runtime.loadGame(data);
        runtime.reset();
        // loading the game swapped in the real bus, so grab its audio unit now
        apu.set(runtime.getBus().getApu());

        var controller = runtime.getBus().getController();
        SwingUtilities.invokeAndWait(() -> window.set(new EmulatorFrame(fileName, controller)));

        while (true) {
            runtime.performSingleInstruction();
        }
    }

    /**
     * Holds the emulation back to the speed of a real NES, which is otherwise limited only by how
     * fast the host can run the instruction loop.
     */
    private static final class FramePacer {

        private long nextFrame = System.nanoTime();

        void awaitNextFrame() {
            nextFrame += FRAME_INTERVAL_NANOS;
            var remaining = nextFrame - System.nanoTime();
            if (remaining > 0) {
                LockSupport.parkNanos(remaining);
            } else {
                // we have fallen behind, so start counting from now instead of trying to catch up
                nextFrame = System.nanoTime();
            }
        }
    }
}
