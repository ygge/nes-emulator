package nu.ygge.nes.cpu.instructions;

import nu.ygge.nes.Runtime;

public final class Instructions {

    private Instructions() { }

    public static byte loadAccumulator(Runtime runtime, byte value) {
        runtime.getCpu().setAccumulator(value);
        return value;
    }

    public static byte orMemoryWithAccumulator(Runtime runtime, byte value) {
        byte orValue = (byte)(value | runtime.getCpu().getAccumulator());
        runtime.getCpu().setAccumulator(orValue);
        return orValue;
    }

    public static byte shiftLeftOneBit(Runtime runtime, byte value) {
        int v = value;
        if (v < 0) {
            v += 256;
        }
        v <<= 1;
        if ((v&0x100) == 0) {
            runtime.getCpu().clearStatusCarry();
        } else {
            runtime.getCpu().setStatusCarry();
            v -= 0x100;
        }
        return (byte)v;
    }
}
