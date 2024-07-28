package nu.ygge.nes.emulator.cpu;

import nu.ygge.nes.emulator.cpu.instructions.Instructions;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.*;

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

    @Test
    void verifyAllInstructionsHaveOpCodes() {
        Map<Instructions, Integer> count = new HashMap<>();
        for (Instructions instruction : Instructions.values()) {
            count.put(instruction, 0);
        }
        for (OpCodes opCode : OpCodes.values()) {
            count.put(opCode.getInstruction(), count.get(opCode.getInstruction()) + 1);
        }
        Assertions.assertEquals(List.of(), count.entrySet().stream().filter(e -> e.getValue() == 0).toList());
/*
        var keys = count.keySet().stream().sorted().toList();
        for (Instructions key : keys) {
            System.out.println(key + ": " + count.get(key));
        }
*/
    }

    private record InstructionAndAddressingMode(Instructions instruction, AddressingMode addressingMode) {
    }
}
