package nu.ygge.nes.emulator.cpu.instructions;

import nu.ygge.nes.emulator.NESRuntime;
import nu.ygge.nes.emulator.cpu.CPU;
import nu.ygge.nes.emulator.cpu.CPUUtil;

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
        pushToStack(runtime, runtime.getCpu().getStatusRegister());
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

    public static void pullAccumulatorFromStack(NESRuntime runtime) {
        runtime.getCpu().setAccumulator(pullFromStack(runtime));
    }

    public static byte addMemoryToAccumulator(NESRuntime runtime, byte value) {
        var sum = add(runtime, value, runtime.getCpu().isStatusCarry());
        if ((runtime.getCpu().getAccumulator()&0x80) == (value&0x80)) {
            if ((sum&0x80) == (value&0x80)) {
                runtime.getCpu().clearStatusOverflow();
            } else {
                runtime.getCpu().setStatusOverflow();
            }
        }
        if ((sum&0x100) > 0) {
            runtime.getCpu().setStatusCarry();
            sum -= 0x100;
        } else {
            runtime.getCpu().clearStatusCarry();
        }
        return (byte) sum;
    }

    public static byte subtractMemoryFromAccumulator(NESRuntime runtime, byte value) {
        var result = add(runtime, (byte)(~value + 1), !runtime.getCpu().isStatusCarry());
        return (byte) result;
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
        int prevAddress = runtime.getCpu().getProgramCounter();
        pushToStack(runtime, (byte) (prevAddress & 0xFF));
        pushToStack(runtime, (byte) (prevAddress >> 8));
        jumpToNewLocation(runtime, address);
    }

    public static void returnFromSubroutine(NESRuntime runtime) {
        var msb = pullFromStack(runtime);
        var lsb = pullFromStack(runtime);
        int address = CPUUtil.toAddress(msb, lsb);
        jumpToNewLocation(runtime, address);
    }

    private static byte compare(NESRuntime runtime, byte register, byte memory) {
        if (register >= memory) {
            runtime.getCpu().setStatusCarry();
        } else {
            runtime.getCpu().clearStatusCarry();
        }
        return (byte) (register - memory);
    }

    private static int add(NESRuntime runtime, byte value, boolean addOne) {
        var sum = CPUUtil.toInt(runtime.getCpu().getAccumulator());
        sum += addOne ? 1 : 0;
        sum += value;
        return sum;
    }

    private static byte pullFromStack(NESRuntime runtime) {
        runtime.getCpu().incrementStackPointer();
        var address = getStackPointerAddress(runtime);
        return runtime.getMemory().read(address);
    }

    private static void pushToStack(NESRuntime runtime, byte value) {
        var address = getStackPointerAddress(runtime);
        runtime.getCpu().decrementStackPointer();
        runtime.getMemory().write(address, value);
    }

    private static int getStackPointerAddress(NESRuntime runtime) {
        int address = CPUUtil.toInt(runtime.getCpu().getStackPointer());
        return 0x100 | address;
    }
}
