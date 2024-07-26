package nu.ygge.nes.cpu.instructions;

import nu.ygge.nes.NESRuntime;

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
        int v = toInt(value);
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
        int v = toInt(value);
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
        int v = toInt(value);
        if ((v&1) == 0) {
            runtime.getCpu().clearStatusCarry();
        } else {
            runtime.getCpu().setStatusCarry();
        }
        v >>= 1;
        return (byte)v;
    }

    public static byte rotateRightOneBit(NESRuntime runtime, byte value) {
        int v = toInt(value);
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
        var sum = toInt(runtime.getCpu().getAccumulator());
        sum += runtime.getCpu().isStatusCarry() ? 1 : 0;
        sum += value;
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

    private static byte pullFromStack(NESRuntime runtime) {
        var address = getStackPointerAddress(runtime);
        runtime.getCpu().incrementStackPointer();
        return runtime.getMemory().read(address);
    }

    private static void pushToStack(NESRuntime runtime, byte value) {
        var address = getStackPointerAddress(runtime);
        runtime.getCpu().decrementStackPointer();
        runtime.getMemory().write(address, value);
    }

    private static int toInt(byte value) {
        int v = value;
        if (v < 0) {
            v += 256;
        }
        return v;
    }

    private static int getStackPointerAddress(NESRuntime runtime) {
        int address = toInt(runtime.getCpu().getStackPointer());
        return 0x100 | address;
    }
}
