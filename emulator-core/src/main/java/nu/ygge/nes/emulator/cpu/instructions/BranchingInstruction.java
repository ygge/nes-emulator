package nu.ygge.nes.emulator.cpu.instructions;

import nu.ygge.nes.emulator.cpu.CPU;

@FunctionalInterface
public interface BranchingInstruction {

    boolean shouldBranch(CPU cpu);
}
