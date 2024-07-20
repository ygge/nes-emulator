package nu.ygge.nes.cpu.instructions;

import nu.ygge.nes.Runtime;

@FunctionalInterface
public interface NoArgumentInstruction {

    void perform(Runtime runtime);
}
