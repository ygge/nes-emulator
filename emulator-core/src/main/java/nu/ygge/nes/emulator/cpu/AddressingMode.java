package nu.ygge.nes.emulator.cpu;

import lombok.Getter;

@Getter
public enum AddressingMode {
    Implied(0),
    Accumulator(0),
    Immediate(1),
    Absolute(2),
    ZeroPage(1),
    AbsoluteX(2),
    AbsoluteY(2),
    ZeroPageX(1),
    ZeroPageY(1),
    IndirectX(1),
    IndirectY(1),
    AbsoluteIndirect(2),
    Relative(1);

    private final int extraBytes;

    AddressingMode(int extraBytes) {
        this.extraBytes = extraBytes;
    }
}
