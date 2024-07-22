package nu.ygge.nes.cpu.instructions;

import nu.ygge.nes.Runtime;

public final class Instructions {

    private Instructions() { }

    public static byte loadAccumulator(Runtime runtime, byte value) {
        runtime.getCpu().setAccumulator(value);
        return value;
    }
}
