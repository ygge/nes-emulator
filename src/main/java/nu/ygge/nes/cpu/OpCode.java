package nu.ygge.nes.cpu;

import lombok.Getter;
import nu.ygge.nes.Runtime;
import nu.ygge.nes.cpu.instructions.Instruction;

import java.util.HashMap;
import java.util.Map;

@Getter
public class OpCode {

    private static final Map<Byte, OpCode> OP_CODES = new HashMap<>();

    private final Instruction instruction;
    private final AddressingMode addressingMode;
    private final int cycles;

    private OpCode(Instruction instruction, AddressingMode addressingMode, int cycles) {
        this.instruction = instruction;
        this.addressingMode = addressingMode;
        this.cycles = cycles;
    }

    public static OpCode getOpCode(byte code) {
        return OP_CODES.get(code);
    }

    public void perform(Runtime runtime, byte eb1, byte eb2) {
        if (addressingMode == AddressingMode.Implied || addressingMode == AddressingMode.Accumulator) {
            instruction.getNoArgumentInstruction().perform(runtime);
        } else {
            throw new UnsupportedOperationException("Addressing mode not supported: " + addressingMode);
        }
    }

    static {
        addOpCode(OpCodes.CLC, Instruction.CLC, AddressingMode.Implied, 2);
        addOpCode(OpCodes.SEC, Instruction.SEC, AddressingMode.Implied, 2);
        addOpCode(OpCodes.CLI, Instruction.CLI, AddressingMode.Implied, 2);
        addOpCode(OpCodes.SEI, Instruction.SEI, AddressingMode.Implied, 2);
        addOpCode(OpCodes.CLV, Instruction.CLV, AddressingMode.Implied, 2);
        addOpCode(OpCodes.CLD, Instruction.CLD, AddressingMode.Implied, 2);
        addOpCode(OpCodes.SED, Instruction.SED, AddressingMode.Implied, 2);
    }

    private static void addOpCode(OpCodes opCode, Instruction instruction, AddressingMode addressingMode, int cycles) {
        OP_CODES.put(opCode.getCode(), new OpCode(instruction, addressingMode, cycles));
    }
}
