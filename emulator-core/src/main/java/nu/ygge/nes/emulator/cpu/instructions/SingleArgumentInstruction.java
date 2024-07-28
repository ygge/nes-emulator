package nu.ygge.nes.emulator.cpu.instructions;

import nu.ygge.nes.emulator.NESRuntime;

@FunctionalInterface
public interface SingleArgumentInstruction {

    byte perform(NESRuntime runtime, byte value);
}
