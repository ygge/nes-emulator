package nu.ygge.nes.cpu;

import lombok.Getter;
import nu.ygge.nes.cpu.instructions.Instruction;

@Getter
public enum OpCodes {
    ORAIX(0x01, Instruction.ORA, AddressingMode.IndirectX, 6),
    ORAZ(0x05, Instruction.ORA, AddressingMode.ZeroPage, 3),
    ASLZ(0x06, Instruction.ASL, AddressingMode.ZeroPage, 5),
    PHP(0x08, Instruction.PHP, AddressingMode.Implied, 3),
    ORAI(0x09, Instruction.ORA, AddressingMode.Immediate, 2),
    ASLAC(0x0A, Instruction.ASL, AddressingMode.Accumulator, 2),
    ORAA(0x0D, Instruction.ORA, AddressingMode.Absolute, 4),
    ASLA(0x0E, Instruction.ASL, AddressingMode.Absolute, 6),
    ORAIY(0x11, Instruction.ORA, AddressingMode.IndirectY, 5),
    ORAZX(0x15, Instruction.ORA, AddressingMode.ZeroPageX, 4),
    ASLZX(0x16, Instruction.ASL, AddressingMode.ZeroPageX, 6),
    CLC(0x18, Instruction.CLC, AddressingMode.Implied, 2),
    ORAAY(0x19, Instruction.CLC, AddressingMode.AbsoluteY, 4),
    ORAAX(0x1D, Instruction.CLC, AddressingMode.AbsoluteX, 4),
    ASLAX(0x1E, Instruction.ASL, AddressingMode.AbsoluteX, 7),
    ANDIX(0x21, Instruction.AND, AddressingMode.IndirectX, 6),
    BITZ(0x24, Instruction.BIT, AddressingMode.ZeroPage, 3),
    ANDZ(0x25, Instruction.AND, AddressingMode.ZeroPage, 3),
    ROLZ(0x26, Instruction.ROL, AddressingMode.ZeroPage, 5),
    ANDI(0x29, Instruction.AND, AddressingMode.Immediate, 2),
    ROLAC(0x2A, Instruction.ROL, AddressingMode.Accumulator, 2),
    BITA(0x2C, Instruction.BIT, AddressingMode.Absolute, 4),
    ANDA(0x2D, Instruction.AND, AddressingMode.Absolute, 4),
    ROLA(0x2E, Instruction.ROL, AddressingMode.Absolute, 6),
    ANDIY(0x31, Instruction.AND, AddressingMode.IndirectY, 5),
    ANDZX(0x35, Instruction.AND, AddressingMode.ZeroPageX, 4),
    ROLZX(0x36, Instruction.ROL, AddressingMode.ZeroPageX, 6),
    ANDAY(0x39, Instruction.AND, AddressingMode.AbsoluteY, 4),
    ANDAX(0x3D, Instruction.AND, AddressingMode.AbsoluteX, 4),
    ROLAX(0x3E, Instruction.ROL, AddressingMode.AbsoluteX, 7),
    SEC(0x38, Instruction.SEC, AddressingMode.Implied, 2),
    CLI(0x58, Instruction.CLI, AddressingMode.Implied, 2),
    SEI(0x78, Instruction.SEI, AddressingMode.Implied, 2),
    LDAIX(0xA1, Instruction.LDA, AddressingMode.IndirectX, 6),
    LDAZ(0xA5, Instruction.LDA, AddressingMode.ZeroPage, 3),
    LDAI(0xA9, Instruction.LDA, AddressingMode.Immediate, 2),
    LDAA(0xAD, Instruction.LDA, AddressingMode.Absolute, 4),
    LDAIY(0xB1, Instruction.LDA, AddressingMode.IndirectY, 5),
    LDAZX(0xB5, Instruction.LDA, AddressingMode.ZeroPageX, 4),
    LDAAY(0xB9, Instruction.LDA, AddressingMode.AbsoluteY, 4),
    LDAAX(0xBD, Instruction.LDA, AddressingMode.AbsoluteX, 4),
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
