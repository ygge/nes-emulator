package nu.ygge.nes.emulator.input;

import lombok.Getter;

/**
 * The buttons of a standard controller, in the order they are shifted out to the CPU.
 */
@Getter
public enum Button {

    A(0b0000_0001),
    B(0b0000_0010),
    SELECT(0b0000_0100),
    START(0b0000_1000),
    UP(0b0001_0000),
    DOWN(0b0010_0000),
    LEFT(0b0100_0000),
    RIGHT(0b1000_0000);

    private final int mask;

    Button(int mask) {
        this.mask = mask;
    }
}
