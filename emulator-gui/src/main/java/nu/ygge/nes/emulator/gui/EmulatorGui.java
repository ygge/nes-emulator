package nu.ygge.nes.emulator.gui;

import nu.ygge.nes.emulator.NESRuntime;
import nu.ygge.nes.emulator.apu.APU;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;
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

        // the hotkeys fire on the UI thread, so they only flag their intent and let the emulation
        // thread carry it out between instructions, where the machine state is consistent
        var saveRequested = new AtomicBoolean();
        var loadRequested = new AtomicReference<File>();
        var quickLoadRequested = new AtomicBoolean();
        // a screenshot only reads the on-screen buffer, so it can run straight on the UI thread
        var hotkeys = new EmulatorHotkeys(
                () -> saveRequested.set(true),
                () -> chooseSaveFile().ifPresent(loadRequested::set),
                () -> quickLoadRequested.set(true),
                () -> takeScreenshot(window.get(), fileName));

        var controller = runtime.getBus().getController();
        SwingUtilities.invokeAndWait(() -> window.set(new EmulatorFrame(fileName, controller, hotkeys)));

        Optional<File> quickLoadFile = Optional.empty();
        while (true) {
            if (saveRequested.getAndSet(false)) {
                quickLoadFile = Optional.ofNullable(saveState(runtime, fileName, window.get()));
            }
            var toLoad = loadRequested.getAndSet(null);
            if (toLoad != null) {
                quickLoadFile = Optional.of(toLoad);
                loadState(runtime, toLoad, fileName, window.get());
            }
            if (quickLoadRequested.getAndSet(false)) {
                quickLoadFile.ifPresent(file -> loadState(runtime, file, fileName, window.get()));
            }
            runtime.performSingleInstruction();
        }
    }

    private static File saveState(NESRuntime runtime, String romName, EmulatorFrame frame) {
        var name = makeFileName(romName, "state");
        try {
            var path = Path.of(name);
            Files.write(path, runtime.saveState());
            announce(frame, romName, "sparade " + name);
            return path.toFile();
        } catch (IOException e) {
            showError(frame, "Kunde inte spara: " + e.getMessage());
            return null;
        }
    }

    private static void takeScreenshot(EmulatorFrame frame, String romName) {
        var name = makeFileName(romName, "png");
        try {
            ImageIO.write(frame.screenshot(), "png", new File(name));
            announce(frame, romName, "screenshot " + name);
        } catch (IOException e) {
            showError(frame, "Kunde inte spara screenshot: " + e.getMessage());
        }
    }

    private static void loadState(NESRuntime runtime, File file, String romName, EmulatorFrame frame) {
        try {
            runtime.loadState(Files.readAllBytes(file.toPath()));
            announce(frame, romName, "laddade " + file.getName());
        } catch (Exception e) {
            showError(frame, "Kunde inte ladda: " + e.getMessage());
        }
    }

    /**
     * A file name based on the game, the current time and the given extension, e.g.
     * {@code mario-20260727-153000.state} or {@code mario-20260727-153000.png}.
     */
    private static String makeFileName(String romName, String extension) {
        var base = new File(romName).getName();
        var dot = base.lastIndexOf('.');
        if (dot > 0) {
            base = base.substring(0, dot);
        }
        var stamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMdd-HHmmss"));
        return base + "-" + stamp + "." + extension;
    }

    private static Optional<File> chooseSaveFile() {
        var chooser = new JFileChooser(new File(".").getAbsoluteFile());
        chooser.setDialogTitle("Välj en sparfil att ladda");
        if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
            return Optional.of(chooser.getSelectedFile());
        }
        return Optional.empty();
    }

    private static void announce(EmulatorFrame frame, String romName, String message) {
        SwingUtilities.invokeLater(() -> frame.setTitle(romName + "  —  " + message));
    }

    private static void showError(EmulatorFrame frame, String message) {
        SwingUtilities.invokeLater(() -> JOptionPane.showMessageDialog(frame, message, "Fel", JOptionPane.ERROR_MESSAGE));
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
