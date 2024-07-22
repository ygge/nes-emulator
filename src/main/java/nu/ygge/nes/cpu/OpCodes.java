package nu.ygge.nes.cpu;

import lombok.Getter;
import nu.ygge.nes.cpu.instructions.Instruction;

@Getter
public enum OpCodes {
    CLC(0x18, Instruction.CLC, AddressingMode.Implied, 2),
    SEC(0x38, Instruction.SEC, AddressingMode.Implied, 2),
    CLI(0x58, Instruction.CLI, AddressingMode.Implied, 2),
    SEI(0x78, Instruction.SEI, AddressingMode.Implied, 2),
    LDAIX(0xA1, Instruction.LDA, AddressingMode.IndirectX, 6),
    LDAZ(0xA5, Instruction.LDA, AddressingMode.ZeroPage, 3),
    LDAI(0xA9, Instruction.LDA, AddressingMode.Immediate, 2),
    LDA(0xAD, Instruction.LDA, AddressingMode.Absolute, 4),
    LDAIY(0xB1, Instruction.LDA, AddressingMode.IndirectY, 5),
    LDAZX(0xB5, Instruction.LDA, AddressingMode.ZeroPageX, 4),
    LDAY(0xB9, Instruction.LDA, AddressingMode.AbsoluteY, 4),
    LDAX(0xBD, Instruction.LDA, AddressingMode.AbsoluteX, 4),
    CLV(0xB8, Instruction.CLV, AddressingMode.Implied, 2),
    CLD(0xD8, Instruction.CLD, AddressingMode.Implied, 2),
    SED(0xF8, Instruction.SED, AddressingMode.Implied, 2);

    private final byte code;
    private final Instruction instruction;
    private final AddressingMode addressingMode;
    private final int cycles;

    OpCodes(int code, Instruction instruction, AddressingMode addressingMode, int cycles) {
        this.code = (byte)code;
        this.instruction = instruction;
        this.addressingMode = addressingMode;
        this.cycles = cycles;
    }
}
