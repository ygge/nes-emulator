package nu.ygge.nes.emulator.cpu.instructions;

import nu.ygge.nes.emulator.NESRuntime;
import nu.ygge.nes.emulator.cpu.CPUUtil;
import nu.ygge.nes.emulator.cpu.InterruptAddress;

public final class InstructionFunctions {

    private InstructionFunctions() { }

    public static byte loadAccumulator(NESRuntime runtime, byte value) {
        runtime.getCpu().setAccumulator(value);
        return value;
    }

    public static byte orMemoryWithAccumulator(NESRuntime runtime, byte value) {
        return (byte)(value | runtime.getCpu().getAccumulator());
    }

    public static byte shiftLeftOneBit(NESRuntime runtime, byte value) {
        int v = CPUUtil.toInt(value);
        v <<= 1;
        if ((v&0x100) == 0) {
            runtime.getCpu().clearStatusCarry();
        } else {
            runtime.getCpu().setStatusCarry();
            v -= 0x100;
        }
        return (byte)v;
    }

    public static void pushProcessorStatusOnStack(NESRuntime runtime) {
        var prevStatus = runtime.getCpu().getStatusRegister();
        runtime.getCpu().setStatusIgnored();
        runtime.getCpu().setStatusBreak();
        pushToStack(runtime, runtime.getCpu().getStatusRegister());
        runtime.getCpu().setStatusRegister(prevStatus);
    }

    public static byte andMemoryWithAccumulator(NESRuntime runtime, byte value) {
        return (byte)(value & runtime.getCpu().getAccumulator());
    }

    public static byte rotateLeftOneBit(NESRuntime runtime, byte value) {
        int v = CPUUtil.toInt(value);
        v <<= 1;
        if (runtime.getCpu().isStatusCarry()) {
            v |= 1;
        }
        if ((v&0x100) == 0) {
            runtime.getCpu().clearStatusCarry();
        } else {
            runtime.getCpu().setStatusCarry();
            v -= 0x100;
        }
        return (byte)v;
    }

    public static byte testBitsWithAccumulator(NESRuntime runtime, byte value) {
        if ((value&0x80) != 0) {
            runtime.getCpu().setStatusNegative();
        } else {
            runtime.getCpu().clearStatusNegative();
        }
        if ((value&0x40) != 0) {
            runtime.getCpu().setStatusOverflow();
        } else {
            runtime.getCpu().clearStatusOverflow();
        }
        return (byte)(value & runtime.getCpu().getAccumulator());
    }

    public static void pullProcessorStatusFromStack(NESRuntime runtime) {
        runtime.getCpu().setStatusRegister(pullFromStack(runtime));
        runtime.getCpu().clearStatusBreak();
        runtime.getCpu().setStatusIgnored();
    }

    public static byte exclusiveOrMemoryWithAccumulator(NESRuntime runtime, byte value) {
        return (byte)(value ^ runtime.getCpu().getAccumulator());
    }

    public static byte shiftRightOneBit(NESRuntime runtime, byte value) {
        int v = CPUUtil.toInt(value);
        if ((v&1) == 0) {
            runtime.getCpu().clearStatusCarry();
        } else {
            runtime.getCpu().setStatusCarry();
        }
        v >>= 1;
        return (byte)v;
    }

    public static byte rotateRightOneBit(NESRuntime runtime, byte value) {
        int v = CPUUtil.toInt(value);
        var isCarry = runtime.getCpu().isStatusCarry();
        if ((v&1) == 0) {
            runtime.getCpu().clearStatusCarry();
        } else {
            runtime.getCpu().setStatusCarry();
        }
        v >>= 1;
        if (isCarry) {
            v |= 0x80;
        }
        return (byte)v;
    }

    public static void pushAccumulatorOnStack(NESRuntime runtime) {
        pushToStack(runtime, runtime.getCpu().getAccumulator());
    }

    public static byte pullAccumulatorFromStack(NESRuntime runtime) {
        runtime.getCpu().setAccumulator(pullFromStack(runtime));
        return runtime.getCpu().getAccumulator();
    }

    public static byte addMemoryToAccumulator(NESRuntime runtime, byte value) {
        return addition(runtime, value, runtime.getCpu().isStatusCarry() ? 1 : 0);
    }

    private static byte addition(NESRuntime runtime, byte value, int delta) {
        var sum = CPUUtil.toInt(runtime.getCpu().getAccumulator());
        sum += delta;
        sum += CPUUtil.toInt(value);
        var result = (byte) sum;
        var v = CPUUtil.toInt(value) + (delta > 1 ? delta : 0);
        if (((result ^ v) & (result ^ CPUUtil.toInt(runtime.getCpu().getAccumulator())) & 0x80) != 0) {
            runtime.getCpu().setStatusOverflow();
        } else {
            runtime.getCpu().clearStatusOverflow();
        }
        if (sum > 0xff) {
            runtime.getCpu().setStatusCarry();
        } else {
            runtime.getCpu().clearStatusCarry();
        }
        return result;
    }

    public static byte subtractMemoryFromAccumulator(NESRuntime runtime, byte value) {
        return addition(runtime, (byte)(~value + 1), runtime.getCpu().isStatusCarry() ? 0 : 255);
    }

    public static byte incrementRegisterX(NESRuntime runtime) {
        byte newValue = (byte) (runtime.getCpu().getRegisterX() + 1);
        runtime.getCpu().setRegisterX(newValue);
        return newValue;
    }

    public static byte incrementRegisterY(NESRuntime runtime) {
        byte newValue = (byte) (runtime.getCpu().getRegisterY() + 1);
        runtime.getCpu().setRegisterY(newValue);
        return newValue;
    }

    public static byte decrementRegisterX(NESRuntime runtime) {
        byte newValue = (byte) (runtime.getCpu().getRegisterX() - 1);
        runtime.getCpu().setRegisterX(newValue);
        return newValue;
    }

    public static byte decrementRegisterY(NESRuntime runtime) {
        byte newValue = (byte) (runtime.getCpu().getRegisterY() - 1);
        runtime.getCpu().setRegisterY(newValue);
        return newValue;
    }

    public static byte transferAccumulatorToRegisterX(NESRuntime runtime) {
        var value = runtime.getCpu().getAccumulator();
        runtime.getCpu().setRegisterX(value);
        return value;
    }

    public static byte transferAccumulatorToRegisterY(NESRuntime runtime) {
        var value = runtime.getCpu().getAccumulator();
        runtime.getCpu().setRegisterY(value);
        return value;
    }

    public static byte transferRegisterXToAccumulator(NESRuntime runtime) {
        var value = runtime.getCpu().getRegisterX();
        runtime.getCpu().setAccumulator(value);
        return value;
    }

    public static byte transferRegisterYToAccumulator(NESRuntime runtime) {
        var value = runtime.getCpu().getRegisterY();
        runtime.getCpu().setAccumulator(value);
        return value;
    }

    public static byte transferStackPointerToRegisterX(NESRuntime runtime) {
        var value = runtime.getCpu().getStackPointer();
        runtime.getCpu().setRegisterX(value);
        return value;
    }

    public static void transferRegisterXToStackPointer(NESRuntime runtime) {
        runtime.getCpu().setStackPointer(runtime.getCpu().getRegisterX());
    }

    public static byte loadRegisterX(NESRuntime runtime, byte value) {
        runtime.getCpu().setRegisterX(value);
        return value;
    }

    public static byte loadRegisterY(NESRuntime runtime, byte value) {
        runtime.getCpu().setRegisterY(value);
        return value;
    }

    public static byte compareMemoryWithAccumulator(NESRuntime runtime, byte value) {
        return compare(runtime, runtime.getCpu().getAccumulator(), value);
    }

    public static byte compareMemoryWithRegisterX(NESRuntime runtime, byte value) {
        return compare(runtime, runtime.getCpu().getRegisterX(), value);
    }

    public static byte compareMemoryWithRegisterY(NESRuntime runtime, byte value) {
        return compare(runtime, runtime.getCpu().getRegisterY(), value);
    }

    public static void jumpToNewLocation(NESRuntime runtime, int address) {
        runtime.getCpu().setProgramCounter(address);
    }

    public static void jumpToNewLocationSavingReturnAddress(NESRuntime runtime, int address) {
        saveAddressToStack(runtime, runtime.getCpu().getProgramCounter() - 1);
        jumpToNewLocation(runtime, address);
    }

    public static void returnFromSubroutine(NESRuntime runtime) {
        returnFromCall(runtime, 1);
    }

    public static void forceBreak(NESRuntime runtime) {
        saveAddressToStack(runtime, runtime.getCpu().getProgramCounter() + 1);
        runtime.getCpu().setStatusBreak();
        pushToStack(runtime, runtime.getCpu().getStatusRegister());
        runtime.resetProgramCounter(InterruptAddress.BREAK);
    }

    public static void returnFromInterrupt(NESRuntime runtime) {
        var statusRegister = pullFromStack(runtime);
        runtime.getCpu().setStatusRegister(statusRegister);
        runtime.getCpu().clearStatusBreak();
        runtime.getCpu().setStatusIgnored();
        returnFromCall(runtime, 0);
    }

    public static byte incrementMemory(NESRuntime runtime, byte value) {
        return (byte) (value + 1);
    }

    public static byte decrementMemory(NESRuntime runtime, byte value) {
        return (byte) (value - 1);
    }

    private static void returnFromCall(NESRuntime runtime, int deltaAddress) {
        var lsb = pullFromStack(runtime);
        var msb = pullFromStack(runtime);
        int address = CPUUtil.toAddress(msb, lsb);
        jumpToNewLocation(runtime, address + deltaAddress);
    }

    private static void saveAddressToStack(NESRuntime runtime, int address) {
        pushToStack(runtime, (byte) (address >> 8));
        pushToStack(runtime, (byte) (address & 0xFF));
    }

    private static byte compare(NESRuntime runtime, byte register, byte memory) {
        if (CPUUtil.toInt(register) >= CPUUtil.toInt(memory)) {
            runtime.getCpu().setStatusCarry();
        } else {
            runtime.getCpu().clearStatusCarry();
        }
        return (byte) (register - memory);
    }

    private static byte pullFromStack(NESRuntime runtime) {
        runtime.getCpu().incrementStackPointer();
        var address = getStackPointerAddress(runtime);
        return runtime.getBus().read(address);
    }

    private static void pushToStack(NESRuntime runtime, byte value) {
        var address = getStackPointerAddress(runtime);
        runtime.getCpu().decrementStackPointer();
        runtime.getBus().write(address, value);
    }

    private static int getStackPointerAddress(NESRuntime runtime) {
        int address = CPUUtil.toInt(runtime.getCpu().getStackPointer());
        return 0x100 | address;
    }
}
