package nu.ygge.nes.emulator.gui;

import lombok.Getter;
import nu.ygge.nes.emulator.input.Joypad;

@Getter
public enum JoypadKey {
    UP(Joypad.UP),
    DOWN(Joypad.DOWN),
    LEFT(Joypad.LEFT),
    RIGHT(Joypad.RIGHT),
    START(Joypad.START),
    SELECT(Joypad.SELECT),
    BUTTON_A(Joypad.BUTTON_A),
    BUTTON_B(Joypad.BUTTON_B);

    private final int bitValue;

    JoypadKey(int bitValue) {
        this.bitValue = bitValue;
    }
}
