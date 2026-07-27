package nu.ygge.nes.emulator.gui;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * The emulator's own keyboard shortcuts, kept apart from the controller bindings: M takes a save
 * state and N brings up a file chooser to load one back.
 */
class EmulatorHotkeys extends KeyAdapter {

    private final Runnable onSave;
    private final Runnable onLoad;

    EmulatorHotkeys(Runnable onSave, Runnable onLoad) {
        this.onSave = onSave;
        this.onLoad = onLoad;
    }

    @Override
    public void keyPressed(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.VK_M -> onSave.run();
            case KeyEvent.VK_N -> onLoad.run();
            default -> { /* every other key belongs to the controller */ }
        }
    }
}
