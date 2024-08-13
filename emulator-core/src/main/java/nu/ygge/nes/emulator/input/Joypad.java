package nu.ygge.nes.emulator.input;

public class Joypad {

    public static final int RIGHT    = 0b10000000;
    public static final int LEFT     = 0b01000000;
    public static final int DOWN     = 0b00100000;
    public static final int UP       = 0b00010000;
    public static final int START    = 0b00001000;
    public static final int SELECT   = 0b00000100;
    public static final int BUTTON_B = 0b00000010;
    public static final int BUTTON_A = 0b00000001;

    private boolean strobe;
    private int buttonIndex;
    private short buttonStatus;

    public void write(byte data) {
        strobe = (data & 1) == 1;
        if (strobe) {
            buttonIndex = 0;
        }
    }

    public byte read() {
        if (buttonIndex > 7) {
            return 1;
        }
        if (!strobe) {
            ++buttonIndex;
        }
        return (buttonStatus & (1 << buttonIndex)) > 0 ? (byte)1 : 0;
    }

    public void keyCallback(int bitValue, boolean set) {
        var shortValue = (short)bitValue;
        if (set) {
            buttonStatus |= shortValue;
        } else {
            buttonStatus &= ~shortValue;
        }
    }
}
