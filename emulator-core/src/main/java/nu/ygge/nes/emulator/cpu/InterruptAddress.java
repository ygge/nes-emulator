package nu.ygge.nes.emulator.cpu;

import lombok.Getter;

@Getter
public enum InterruptAddress {
    NMI(0xFFFA),
    RESET(0xFFFC),
    BREAK(0xFFFE);

    private final int startAddress;

    InterruptAddress(int startAddress) {
        this.startAddress = startAddress;
    }
}
