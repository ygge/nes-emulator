package nu.ygge.nes.emulator.cpu;

import nu.ygge.nes.emulator.cpu.instructions.Instructions;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class OpCodesTest {

    @Test
    void verifyAllOpCodesAreDistinct() {
        Set<Byte> opCodes = new HashSet<>();
        for (OpCodes opcode : OpCodes.values()) {
            assertTrue(opCodes.add(opcode.getCode()), "Duplicate opcode: " + opcode);
        }
    }

    @Test
    void verifyInstructionAndAddressingModeDistinct() {
        Set<InstructionAndAddressingMode> opCodes = new HashSet<>();
        for (OpCodes opcode : OpCodes.values()) {
            assertTrue(opCodes.add(new InstructionAndAddressingMode(opcode.getInstruction(), opcode.getAddressingMode())), "Duplicate opcode: " + opcode);
        }
    }

    private record InstructionAndAddressingMode(Instructions instruction, AddressingMode addressingMode) {
    }
}
