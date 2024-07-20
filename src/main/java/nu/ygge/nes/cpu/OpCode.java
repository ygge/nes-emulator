package nu.ygge.nes.cpu;

import lombok.Getter;
import nu.ygge.nes.Runtime;
import nu.ygge.nes.cpu.instructions.Instruction;

import java.util.HashMap;
import java.util.Map;

@Getter
public class OpCode {

    public static final Map<Byte, OpCode> OP_CODES = new HashMap<>();

    private final Instruction instruction;
    private final AddressingMode addressingMode;
    private final int cycles;

    private OpCode(Instruction instruction, AddressingMode addressingMode, int cycles) {
        this.instruction = instruction;
        this.addressingMode = addressingMode;
        this.cycles = cycles;
    }

    public void perform(Runtime runtime, byte eb1, byte eb2) {
        if (addressingMode == AddressingMode.Implied || addressingMode == AddressingMode.Accumulator) {
            instruction.getNoArgumentInstruction().perform(runtime);
        } else {
            throw new UnsupportedOperationException("Addressing mode not supported: " + addressingMode);
        }
    }

    static {
        addOpCode(0x18, Instruction.CLC, AddressingMode.Implied, 2);
        addOpCode(0x38, Instruction.SEC, AddressingMode.Implied, 2);
        addOpCode(0x58, Instruction.CLI, AddressingMode.Implied, 2);
        addOpCode(0x78, Instruction.SEI, AddressingMode.Implied, 2);
        addOpCode(0xB8, Instruction.CLV, AddressingMode.Implied, 2);
        addOpCode(0xD8, Instruction.CLD, AddressingMode.Implied, 2);
        addOpCode(0xF8, Instruction.SED, AddressingMode.Implied, 2);
    }

    private static void addOpCode(int code, Instruction instruction, AddressingMode addressingMode, int cycles) {
        OP_CODES.put((byte)code, new OpCode(instruction, addressingMode, cycles));
    }
}
