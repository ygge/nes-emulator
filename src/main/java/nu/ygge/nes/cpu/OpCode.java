package nu.ygge.nes.cpu;

import lombok.Getter;
import nu.ygge.nes.Runtime;
import nu.ygge.nes.cpu.instructions.Instruction;
import nu.ygge.nes.cpu.instructions.StatusFlagsAffected;

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
        } else if (addressingMode == AddressingMode.Immediate) {
            instruction.getSingleArgumentInstruction().perform(runtime, eb1);
            setStatusFlags(runtime.getCpu(), eb1);
        } else if (addressingMode == AddressingMode.Absolute) {
            byte value = runtime.getRam().read(toAddress(eb1, eb2));
            instruction.getSingleArgumentInstruction().perform(runtime, value);
            setStatusFlags(runtime.getCpu(), value);
        } else {
            throw new UnsupportedOperationException("Addressing mode not supported: " + addressingMode);
        }
    }

    private void setStatusFlags(CPU cpu, byte value) {
        if (instruction.getStatusFlagsAffected() == StatusFlagsAffected.NONE) {
            return;
        }
        if (value == 0) {
            cpu.setStatusZero();
        } else {
            cpu.clearStatusZero();
        }
        if (value < 0) {
            cpu.setStatusNegative();
        } else {
            cpu.clearStatusNegative();
        }
    }

    private int toAddress(byte eb1, byte eb2) {
        int address = eb1;
        if (address < 0) {
            address += 256;
        }
        return (address << 8) | eb2;
    }

    static {
        addOpCode(OpCodes.CLC, Instruction.CLC, AddressingMode.Implied, 2);
        addOpCode(OpCodes.SEC, Instruction.SEC, AddressingMode.Implied, 2);
        addOpCode(OpCodes.CLI, Instruction.CLI, AddressingMode.Implied, 2);
        addOpCode(OpCodes.SEI, Instruction.SEI, AddressingMode.Implied, 2);
        addOpCode(OpCodes.CLV, Instruction.CLV, AddressingMode.Implied, 2);
        addOpCode(OpCodes.CLD, Instruction.CLD, AddressingMode.Implied, 2);
        addOpCode(OpCodes.SED, Instruction.SED, AddressingMode.Implied, 2);
        addOpCode(OpCodes.LDAI, Instruction.LDA, AddressingMode.Immediate, 2);
        addOpCode(OpCodes.LDA, Instruction.LDA, AddressingMode.Absolute, 4);
    }

    private static void addOpCode(OpCodes opCode, Instruction instruction, AddressingMode addressingMode, int cycles) {
        OP_CODES.put(opCode.getCode(), new OpCode(instruction, addressingMode, cycles));
    }
}
