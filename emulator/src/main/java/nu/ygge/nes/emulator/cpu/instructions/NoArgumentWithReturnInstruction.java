package nu.ygge.nes.emulator.cpu.instructions;

import nu.ygge.nes.emulator.NESRuntime;

@FunctionalInterface
public interface NoArgumentWithReturnInstruction {

    byte perform(NESRuntime runtime);
}
