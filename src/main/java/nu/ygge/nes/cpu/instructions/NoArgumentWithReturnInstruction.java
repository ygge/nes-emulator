package nu.ygge.nes.cpu.instructions;

import nu.ygge.nes.NESRuntime;

@FunctionalInterface
public interface NoArgumentWithReturnInstruction {

    byte perform(NESRuntime runtime);
}
