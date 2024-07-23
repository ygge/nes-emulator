package nu.ygge.nes.cpu;

import lombok.Getter;
import nu.ygge.nes.NESRuntime;
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

    public void perform(NESRuntime runtime, byte eb1, byte eb2) {
        if (addressingMode == AddressingMode.Implied) {
            instruction.getNoArgumentInstruction().perform(runtime);
        } else if (addressingMode == AddressingMode.Accumulator) {
            var result = instruction.getSingleArgumentInstruction().perform(runtime, runtime.getCpu().getAccumulator());
            setStatusFlags(runtime.getCpu(), result);
            if (instruction.isStoreValueBack()) {
                runtime.getCpu().setAccumulator(result);
            }
        } else if (addressingMode == AddressingMode.Immediate) {
            var result = instruction.getSingleArgumentInstruction().perform(runtime, eb1);
            setStatusFlags(runtime.getCpu(), result);
        } else if (addressingMode == AddressingMode.Absolute) {
            int address = toAddress(eb1, eb2);
            byte value = runtime.getMemory().read(address);
            var result = instruction.getSingleArgumentInstruction().perform(runtime, value);
            setStatusFlags(runtime.getCpu(), result);
            storeValueBack(runtime, address, result);
        } else if (addressingMode == AddressingMode.ZeroPage) {
            int address = toAddress((byte) 0, eb1);
            byte value = runtime.getMemory().read(address);
            var result = instruction.getSingleArgumentInstruction().perform(runtime, value);
            setStatusFlags(runtime.getCpu(), result);
            storeValueBack(runtime, address, result);
        } else if (addressingMode == AddressingMode.ZeroPageX) {
            int address = toZeroPageAddress(eb1, runtime.getCpu().getRegisterX());
            byte value = runtime.getMemory().read(address);
            var result = instruction.getSingleArgumentInstruction().perform(runtime, value);
            setStatusFlags(runtime.getCpu(), result);
            storeValueBack(runtime, address, result);
        } else if (addressingMode == AddressingMode.AbsoluteX) {
            int address = toAddress(eb1, eb2) + toInt(runtime.getCpu().getRegisterX());
            byte value = runtime.getMemory().read(address);
            var result = instruction.getSingleArgumentInstruction().perform(runtime, value);
            setStatusFlags(runtime.getCpu(), result);
            storeValueBack(runtime, address, result);
        } else if (addressingMode == AddressingMode.AbsoluteY) {
            int address = toAddress(eb1, eb2) + toInt(runtime.getCpu().getRegisterY());
            byte value = runtime.getMemory().read(address);
            var result = instruction.getSingleArgumentInstruction().perform(runtime, value);
            setStatusFlags(runtime.getCpu(), result);
            storeValueBack(runtime, address, result);
        } else if (addressingMode == AddressingMode.IndirectX) {
            int address = toZeroPageAddress(eb1, runtime.getCpu().getRegisterX());
            byte value1 = runtime.getMemory().read(address);
            byte value2 = runtime.getMemory().read(address + 1);
            int finalAddress = toAddress(value2, value1);
            byte value = runtime.getMemory().read(finalAddress);
            var result = instruction.getSingleArgumentInstruction().perform(runtime, value);
            setStatusFlags(runtime.getCpu(), result);
            storeValueBack(runtime, finalAddress, result);
        } else if (addressingMode == AddressingMode.IndirectY) {
            int address = toZeroPageAddress(eb1, (byte)0);
            byte value1 = runtime.getMemory().read(address);
            byte value2 = runtime.getMemory().read(address + 1);
            int finalAddress = toAddress(value2, value1) + toInt(runtime.getCpu().getRegisterY());
            byte value = runtime.getMemory().read(finalAddress);
            var result = instruction.getSingleArgumentInstruction().perform(runtime, value);
            setStatusFlags(runtime.getCpu(), result);
            storeValueBack(runtime, finalAddress, result);
        } else {
            throw new UnsupportedOperationException("Addressing mode not supported: " + addressingMode);
        }
    }

    private void storeValueBack(NESRuntime runtime, int address, byte result) {
        if (instruction.isStoreValueBack()) {
            runtime.getMemory().write(address, result);
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
        if (instruction.getStatusFlagsAffected() == StatusFlagsAffected.ZERO) {
            return;
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
            if (OP_CODES.containsKey(opCode.getCode())) {
                throw new IllegalStateException("Duplicate op code: " + opCode.getCode());
            }
            OP_CODES.put(opCode.getCode(), new OpCode(opCode.getInstruction(), opCode.getAddressingMode(), opCode.getCycles()));
        }
    }
}
