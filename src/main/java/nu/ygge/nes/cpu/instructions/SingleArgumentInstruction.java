package nu.ygge.nes.cpu.instructions;

import nu.ygge.nes.NESRuntime;

@FunctionalInterface
public interface SingleArgumentInstruction {

    byte perform(NESRuntime runtime, byte value);
}
