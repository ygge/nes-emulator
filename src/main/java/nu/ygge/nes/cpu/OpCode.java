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
            var result = instruction.getSingleArgumentInstruction().perform(runtime, eb1);
            setStatusFlags(runtime.getCpu(), result);
        } else if (addressingMode == AddressingMode.Absolute) {
            byte value = runtime.getMemory().read(toAddress(eb1, eb2));
            var result = instruction.getSingleArgumentInstruction().perform(runtime, value);
            setStatusFlags(runtime.getCpu(), result);
        } else if (addressingMode == AddressingMode.ZeroPage) {
            byte value = runtime.getMemory().read(toAddress((byte)0, eb1));
            var result = instruction.getSingleArgumentInstruction().perform(runtime, value);
            setStatusFlags(runtime.getCpu(), result);
        } else if (addressingMode == AddressingMode.ZeroPageX) {
            byte value = runtime.getMemory().read(toZeroPageAddress(eb1, runtime.getCpu().getRegisterX()));
            var result = instruction.getSingleArgumentInstruction().perform(runtime, value);
            setStatusFlags(runtime.getCpu(), result);
        } else if (addressingMode == AddressingMode.AbsoluteX) {
            byte value = runtime.getMemory().read(toAddress(eb1, eb2) + toInt(runtime.getCpu().getRegisterX()));
            var result = instruction.getSingleArgumentInstruction().perform(runtime, value);
            setStatusFlags(runtime.getCpu(), result);
        } else if (addressingMode == AddressingMode.AbsoluteY) {
            byte value = runtime.getMemory().read(toAddress(eb1, eb2) + toInt(runtime.getCpu().getRegisterY()));
            var result = instruction.getSingleArgumentInstruction().perform(runtime, value);
            setStatusFlags(runtime.getCpu(), result);
        } else if (addressingMode == AddressingMode.IndirectX) {
            int address = toZeroPageAddress(eb1, runtime.getCpu().getRegisterX());
            byte value1 = runtime.getMemory().read(address);
            byte value2 = runtime.getMemory().read(address + 1);
            byte value = runtime.getMemory().read(toAddress(value2, value1));
            var result = instruction.getSingleArgumentInstruction().perform(runtime, value);
            setStatusFlags(runtime.getCpu(), result);
        } else if (addressingMode == AddressingMode.IndirectY) {
            int address = toZeroPageAddress(eb1, (byte)0);
            byte value1 = runtime.getMemory().read(address);
            byte value2 = runtime.getMemory().read(address + 1);
            byte value = runtime.getMemory().read(toAddress(value2, value1) + toInt(runtime.getCpu().getRegisterY()));
            var result = instruction.getSingleArgumentInstruction().perform(runtime, value);
            setStatusFlags(runtime.getCpu(), result);
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

    private int toZeroPageAddress(byte b1, byte b2) {
        int address = toInt(b1) + toInt(b2);
        return address & 0xFF;
    }

    private int toAddress(byte eb1, byte eb2) {
        int address = toInt(eb1);
        return (address << 8) | eb2;
    }

    private static int toInt(byte eb1) {
        int address = eb1;
        if (address < 0) {
            address += 256;
        }
        return address;
    }

    static {
        for (OpCodes opCode : OpCodes.values()) {
            OP_CODES.put(opCode.getCode(), new OpCode(opCode.getInstruction(), opCode.getAddressingMode(), opCode.getCycles()));
        }
    }
}
