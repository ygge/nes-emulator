package nu.ygge.nes.emulator.cpu.instructions;

import nu.ygge.nes.emulator.NESRuntime;

@FunctionalInterface
public interface AddressInstruction {

    void perform(NESRuntime runtime, int address);
}
