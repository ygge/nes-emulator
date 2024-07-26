package nu.ygge.nes;

import nu.ygge.nes.cpu.OpCodes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NESRuntime_AdditionAndSubtractionTest {

    private NESRuntime runtime;

    @BeforeEach
    void setUp() {
        runtime = new NESRuntime();
    }

    @Test
    void testSubtractionWithBorrow() {
        int pc = 0x100;
        runtime.getCpu().setProgramCounter(pc);
        runtime.getMemory().write(pc, OpCodes.LDAI.getCode());
        //runtime.getMemory().write(pc + 1, OpCodes.STA.getCode());
    }
}
