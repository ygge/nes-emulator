package nu.ygge.nes.emulator.gui;

import nu.ygge.nes.emulator.input.Button;
import nu.ygge.nes.emulator.input.Controller;

import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Map;

class ControllerKeyListener extends KeyAdapter {

    private static final Map<Integer, Button> KEY_BINDINGS = Map.of(
            KeyEvent.VK_UP, Button.UP,
            KeyEvent.VK_DOWN, Button.DOWN,
            KeyEvent.VK_LEFT, Button.LEFT,
            KeyEvent.VK_RIGHT, Button.RIGHT,
            KeyEvent.VK_A, Button.A,
            KeyEvent.VK_S, Button.B,
            KeyEvent.VK_ENTER, Button.START,
            KeyEvent.VK_SHIFT, Button.SELECT);

    private final Controller controller;

    ControllerKeyListener(Controller controller) {
        this.controller = controller;
    }

    @Override
    public void keyPressed(KeyEvent event) {
        setPressed(event, true);
    }

    @Override
    public void keyReleased(KeyEvent event) {
        setPressed(event, false);
    }

    private void setPressed(KeyEvent event, boolean pressed) {
        var button = KEY_BINDINGS.get(event.getKeyCode());
        if (button != null) {
            controller.setPressed(button, pressed);
        }
    }
}
