package nu.ygge.nes.cpu.instructions;

import nu.ygge.nes.NESRuntime;

@FunctionalInterface
public interface NoArgumentInstruction {

    void perform(NESRuntime runtime);
}
