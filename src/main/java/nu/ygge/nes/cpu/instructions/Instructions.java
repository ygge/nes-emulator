package nu.ygge.nes.cpu.instructions;

import nu.ygge.nes.NESRuntime;

public final class Instructions {

    private Instructions() { }

    public static byte loadAccumulator(NESRuntime runtime, byte value) {
        runtime.getCpu().setAccumulator(value);
        return value;
    }

    public static byte orMemoryWithAccumulator(NESRuntime runtime, byte value) {
        byte orValue = (byte)(value | runtime.getCpu().getAccumulator());
        runtime.getCpu().setAccumulator(orValue);
        return orValue;
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
        var address = getStackPointerAddress(runtime);
        runtime.getCpu().decrementStackPointer();
        runtime.getMemory().write(address, runtime.getCpu().getStatusRegister());
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
