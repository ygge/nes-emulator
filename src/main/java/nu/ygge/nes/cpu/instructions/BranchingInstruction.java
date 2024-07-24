package nu.ygge.nes.cpu.instructions;

import nu.ygge.nes.cpu.CPU;

@FunctionalInterface
public interface BranchingInstruction {

    boolean shouldBranch(CPU cpu);
}
