package nu.ygge.nes.cpu;

import lombok.Getter;
import nu.ygge.nes.cpu.instructions.Instructions;

@Getter
public enum OpCodes {
    ORAIX(0x01, Instructions.ORA, AddressingMode.IndirectX, 6),
    ORAZ(0x05, Instructions.ORA, AddressingMode.ZeroPage, 3),
    ASLZ(0x06, Instructions.ASL, AddressingMode.ZeroPage, 5),
    PHP(0x08, Instructions.PHP, AddressingMode.Implied, 3),
    ORAI(0x09, Instructions.ORA, AddressingMode.Immediate, 2),
    ASLAC(0x0A, Instructions.ASL, AddressingMode.Accumulator, 2),
    ORAA(0x0D, Instructions.ORA, AddressingMode.Absolute, 4),
    ASLA(0x0E, Instructions.ASL, AddressingMode.Absolute, 6),
    ORAIY(0x11, Instructions.ORA, AddressingMode.IndirectY, 5),
    ORAZX(0x15, Instructions.ORA, AddressingMode.ZeroPageX, 4),
    ASLZX(0x16, Instructions.ASL, AddressingMode.ZeroPageX, 6),
    CLC(0x18, Instructions.CLC, AddressingMode.Implied, 2),
    ORAAY(0x19, Instructions.CLC, AddressingMode.AbsoluteY, 4),
    ORAAX(0x1D, Instructions.CLC, AddressingMode.AbsoluteX, 4),
    ASLAX(0x1E, Instructions.ASL, AddressingMode.AbsoluteX, 7),
    ANDIX(0x21, Instructions.AND, AddressingMode.IndirectX, 6),
    BITZ(0x24, Instructions.BIT, AddressingMode.ZeroPage, 3),
    ANDZ(0x25, Instructions.AND, AddressingMode.ZeroPage, 3),
    ROLZ(0x26, Instructions.ROL, AddressingMode.ZeroPage, 5),
    PLP(0x28, Instructions.PLP, AddressingMode.Implied, 4),
    ANDI(0x29, Instructions.AND, AddressingMode.Immediate, 2),
    ROLAC(0x2A, Instructions.ROL, AddressingMode.Accumulator, 2),
    BITA(0x2C, Instructions.BIT, AddressingMode.Absolute, 4),
    ANDA(0x2D, Instructions.AND, AddressingMode.Absolute, 4),
    ROLA(0x2E, Instructions.ROL, AddressingMode.Absolute, 6),
    ANDIY(0x31, Instructions.AND, AddressingMode.IndirectY, 5),
    ANDZX(0x35, Instructions.AND, AddressingMode.ZeroPageX, 4),
    ROLZX(0x36, Instructions.ROL, AddressingMode.ZeroPageX, 6),
    ANDAY(0x39, Instructions.AND, AddressingMode.AbsoluteY, 4),
    ANDAX(0x3D, Instructions.AND, AddressingMode.AbsoluteX, 4),
    ROLAX(0x3E, Instructions.ROL, AddressingMode.AbsoluteX, 7),
    SEC(0x38, Instructions.SEC, AddressingMode.Implied, 2),
    CLI(0x58, Instructions.CLI, AddressingMode.Implied, 2),
    SEI(0x78, Instructions.SEI, AddressingMode.Implied, 2),
    LDAIX(0xA1, Instructions.LDA, AddressingMode.IndirectX, 6),
    LDAZ(0xA5, Instructions.LDA, AddressingMode.ZeroPage, 3),
    LDAI(0xA9, Instructions.LDA, AddressingMode.Immediate, 2),
    LDAA(0xAD, Instructions.LDA, AddressingMode.Absolute, 4),
    LDAIY(0xB1, Instructions.LDA, AddressingMode.IndirectY, 5),
    LDAZX(0xB5, Instructions.LDA, AddressingMode.ZeroPageX, 4),
    LDAAY(0xB9, Instructions.LDA, AddressingMode.AbsoluteY, 4),
    LDAAX(0xBD, Instructions.LDA, AddressingMode.AbsoluteX, 4),
    CLV(0xB8, Instructions.CLV, AddressingMode.Implied, 2),
    CLD(0xD8, Instructions.CLD, AddressingMode.Implied, 2),
    SED(0xF8, Instructions.SED, AddressingMode.Implied, 2);

    private final byte code;
    private final Instructions instruction;
    private final AddressingMode addressingMode;
    private final int cycles;

    OpCodes(int code, Instructions instruction, AddressingMode addressingMode, int cycles) {
        this.code = (byte)code;
        this.instruction = instruction;
        this.addressingMode = addressingMode;
        this.cycles = cycles;
    }
}
