package nu.ygge.nes.emulator.cpu;

import lombok.Getter;
import nu.ygge.nes.emulator.NESRuntime;
import nu.ygge.nes.emulator.cpu.instructions.Instructions;
import nu.ygge.nes.emulator.cpu.instructions.StatusFlagsAffected;
import nu.ygge.nes.emulator.cpu.instructions.WriteValue;

import java.util.HashMap;
import java.util.Map;

@Getter
public class OpCode {

    private static final Map<Byte, OpCode> OP_CODES = new HashMap<>();

    private final Instructions instruction;
    private final AddressingMode addressingMode;
    private final int cycles;
    private final boolean addCycleOnPageBoundaryCrossing;

    private OpCode(Instructions instruction,
                   AddressingMode addressingMode,
                   int cycles,
                   boolean addCycleOnPageBoundaryCrossing) {
        this.instruction = instruction;
        this.addressingMode = addressingMode;
        this.cycles = cycles;
        this.addCycleOnPageBoundaryCrossing = addCycleOnPageBoundaryCrossing;
    }

    public static OpCode getOpCode(byte code) {
        return OP_CODES.get(code);
    }

    public void perform(NESRuntime runtime, byte eb1, byte eb2) {
        if (addressingMode == AddressingMode.Implied) {
            if (instruction.getNoArgumentInstruction() != null) {
                instruction.getNoArgumentInstruction().perform(runtime);
            } else {
                var result = instruction.getNoArgumentWithReturnInstruction().perform(runtime);
                setStatusFlags(runtime.getCpu(), result);
            }
        } else if (addressingMode == AddressingMode.Accumulator) {
            var result = instruction.getSingleArgumentInstruction().perform(runtime, runtime.getCpu().getAccumulator());
            setStatusFlags(runtime.getCpu(), result);
            if (instruction.getWriteValue() == WriteValue.Memory) {
                throw new IllegalStateException("Accumulator access mode may not write to memory");
            } else if (instruction.getWriteValue() != WriteValue.None) {
                runtime.getCpu().setAccumulator(result);
            }
        } else if (addressingMode == AddressingMode.Immediate) {
            var result = instruction.getSingleArgumentInstruction().perform(runtime, eb1);
            setStatusFlags(runtime.getCpu(), result);
            if (instruction.getWriteValue() == WriteValue.Accumulator) {
                runtime.getCpu().setAccumulator(result);
            } else if (instruction.getWriteValue() != WriteValue.None) {
                throw new IllegalStateException("Immediate access mode can only store value in Accumulator");
            }
        } else if (addressingMode == AddressingMode.Absolute) {
            performWithAddress(runtime, CPUUtil.toAddress(eb2, eb1));
        } else if (addressingMode == AddressingMode.ZeroPage) {
            performWithAddress(runtime, CPUUtil.toAddress((byte) 0, eb1));
        } else if (addressingMode == AddressingMode.ZeroPageX) {
            performWithAddress(runtime, toZeroPageAddress(eb1, runtime.getCpu().getRegisterX()));
        } else if (addressingMode == AddressingMode.ZeroPageY) {
            performWithAddress(runtime, toZeroPageAddress(eb1, runtime.getCpu().getRegisterY()));
        } else if (addressingMode == AddressingMode.AbsoluteX) {
            int baseAddress = CPUUtil.toAddress(eb2, eb1);
            int address = baseAddress + CPUUtil.toInt(runtime.getCpu().getRegisterX());
            checkForPageBoundaryCrossing(runtime, address, baseAddress);
            performWithAddress(runtime, address);
        } else if (addressingMode == AddressingMode.AbsoluteY) {
            performWithAbsoluteY(runtime, eb2, eb1);
        } else if (addressingMode == AddressingMode.AbsoluteIndirect) {
            int address = CPUUtil.toAddress(eb2, eb1);
            var lsb = runtime.getBus().read(address);
            int nextAddress = address + 1;
            if ((nextAddress >> 8) != (address >> 8)) {
                nextAddress -= 0x100;
            }
            var msb = runtime.getBus().read(nextAddress);
            performWithAddress(runtime, CPUUtil.toAddress(msb, lsb));
        } else if (addressingMode == AddressingMode.IndirectX) {
            int address = toZeroPageAddress(eb1, runtime.getCpu().getRegisterX());
            byte lsb = runtime.getBus().read(address);
            byte msb = runtime.getBus().read((address + 1) & 0xFF);
            performWithAddress(runtime, CPUUtil.toAddress(msb, lsb));
        } else if (addressingMode == AddressingMode.IndirectY) {
            byte value1 = runtime.getBus().read(toZeroPageAddress(eb1, (byte) 0));
            byte value2 = runtime.getBus().read(toZeroPageAddress(eb1, (byte) 1));
            performWithAbsoluteY(runtime, value2, value1);
        } else if (addressingMode == AddressingMode.Relative) {
            if (instruction.getBranchingInstruction().shouldBranch(runtime.getCpu())) {
                var newValue = runtime.getCpu().getProgramCounter() + eb1;
                if (newValue/256 != runtime.getCpu().getProgramCounter()/256) {
                    runtime.getCpu().addCycles(2);
                } else {
                    runtime.getCpu().addCycles(1);
                }
                runtime.getCpu().setProgramCounter(newValue);
            }
        } else {
            throw new UnsupportedOperationException("Addressing mode not supported: " + addressingMode);
        }
    }

    private void performWithAbsoluteY(NESRuntime runtime, byte msb, byte lsb) {
        int baseAddress = CPUUtil.toAddress(msb, lsb);
        int address = baseAddress + CPUUtil.toInt(runtime.getCpu().getRegisterY());
        checkForPageBoundaryCrossing(runtime, address, baseAddress);
        performWithAddress(runtime, address);
    }

    private void checkForPageBoundaryCrossing(NESRuntime runtime, int address, int baseAddress) {
        if (addCycleOnPageBoundaryCrossing && (address >> 8) != (baseAddress >> 8)) {
            runtime.getCpu().addCycles(1); // page boundary crossed
        }
    }

    private void performWithAddress(NESRuntime runtime, int address) {
        if (instruction.getAddressInstruction() != null) {
            instruction.getAddressInstruction().perform(runtime, address);
        } else {
            byte value = runtime.getBus().read(address);
            var result = instruction.getSingleArgumentInstruction().perform(runtime, value);
            setStatusFlags(runtime.getCpu(), result);
            writeValue(runtime, address, result);
        }
    }

    private void writeValue(NESRuntime runtime, int address, byte result) {
        switch (instruction.getWriteValue()) {
            case Accumulator -> runtime.getCpu().setAccumulator(result);
            case Memory, AccumulatorOrMemory -> runtime.getBus().write(address, result);
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
        int address = CPUUtil.toInt(b1) + CPUUtil.toInt(b2);
        return address & 0xFF;
    }

    static {
        for (OpCodes opCode : OpCodes.values()) {
            if (OP_CODES.containsKey(opCode.getCode())) {
                throw new IllegalStateException("Duplicate op code: " + opCode.getCode());
            }
            OP_CODES.put(opCode.getCode(), toOpCode(opCode));
        }
    }

    private static OpCode toOpCode(OpCodes opCode) {
        return new OpCode(
                opCode.getInstruction(),
                opCode.getAddressingMode(),
                opCode.getCycles(),
                opCode.isAddCycleOnPageBoundaryCrossing()
        );
    }
}
