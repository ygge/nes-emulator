package nu.ygge.nes.emulator.input;

/**
 * A standard controller. The CPU latches the current button state by pulsing the strobe, and then
 * clocks the buttons out one at a time by reading the same address once per button.
 */
public class Controller {

    private static final int BUTTON_COUNT = 8;

    // written by the user interface thread and read by the emulation thread
    private volatile int pressedButtons;
    private int shiftRegister;
    private boolean strobe;

    public void setPressed(Button button, boolean pressed) {
        pressedButtons = pressed
                ? pressedButtons | button.getMask()
                : pressedButtons & ~button.getMask();
    }

    public void write(byte data) {
        strobe = (data & 1) != 0;
        if (strobe) {
            shiftRegister = pressedButtons;
        }
    }

    public byte read() {
        if (strobe) {
            // as long as the strobe is high the controller keeps reporting the first button
            shiftRegister = pressedButtons;
        }
        var value = shiftRegister & 1;
        // once every button has been clocked out the controller reports a pressed button
        shiftRegister = (shiftRegister >> 1) | (1 << (BUTTON_COUNT - 1));
        return (byte) value;
    }
}
