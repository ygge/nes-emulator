package nu.ygge.nes.emulator.gui;

import lombok.AllArgsConstructor;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;

/**
 * The emulator's own keyboard shortcuts, kept apart from the controller bindings: M takes a save
 * state, N brings up a file chooser to load one back, and P saves a screenshot.
 */
@AllArgsConstructor
class EmulatorHotkeys extends KeyAdapter {

    private final Runnable onSave, onLoad, onQuickLoad;
    private final Runnable onScreenshot;

    @Override
    public void keyPressed(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.VK_K -> onSave.run();
            case KeyEvent.VK_L -> onLoad.run();
            case KeyEvent.VK_J -> onQuickLoad.run();
            case KeyEvent.VK_P -> onScreenshot.run();
            default -> { /* every other key belongs to the controller */ }
        }
    }
}
