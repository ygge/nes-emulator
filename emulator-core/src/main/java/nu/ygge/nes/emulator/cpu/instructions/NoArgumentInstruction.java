package nu.ygge.nes.emulator.cpu.instructions;

import nu.ygge.nes.emulator.NESRuntime;

@FunctionalInterface
public interface NoArgumentInstruction {

    void perform(NESRuntime runtime);
}
