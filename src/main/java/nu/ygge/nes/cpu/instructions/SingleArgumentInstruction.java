package nu.ygge.nes.cpu.instructions;

import nu.ygge.nes.Runtime;

@FunctionalInterface
public interface SingleArgumentInstruction {

    void perform(Runtime runtime, byte value);
}
