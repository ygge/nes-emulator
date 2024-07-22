package nu.ygge.nes;

import nu.ygge.nes.cpu.OpCodes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Runtime_ImmediateAddressingModeTest {

    private Runtime runtime;

    @BeforeEach
    void setUp() {
        runtime = new Runtime();
    }

    @Test
    void verifySetCarryFlag() {
        runSingleImmediateOperation(OpCodes.SEC);
        Assertions.assertTrue(runtime.getCpu().isStatusCarry());
    }

    @Test
    void verifyClearCarryFlag() {
        runtime.getCpu().setStatusCarry();
        runSingleImmediateOperation(OpCodes.CLC);
        Assertions.assertFalse(runtime.getCpu().isStatusCarry());
    }

    @Test
    void verifyClearOverflowFlag() {
        runtime.getCpu().setStatusOverflow();
        runSingleImmediateOperation(OpCodes.CLV);
        Assertions.assertFalse(runtime.getCpu().isStatusOverflow());
    }

    @Test
    void verifySetInterruptFlag() {
        runSingleImmediateOperation(OpCodes.SEI);
        Assertions.assertTrue(runtime.getCpu().isStatusInterrupt());
    }

    @Test
    void verifyClearInterruptFlag() {
        runtime.getCpu().setStatusInterrupt();
        runSingleImmediateOperation(OpCodes.CLI);
        Assertions.assertFalse(runtime.getCpu().isStatusInterrupt());
    }

    @Test
    void verifySetDecimalFlag() {
        runSingleImmediateOperation(OpCodes.SED);
        Assertions.assertTrue(runtime.getCpu().isStatusDecimal());
    }

    @Test
    void verifyClearDecimalFlag() {
        runtime.getCpu().setStatusCarry();
        runSingleImmediateOperation(OpCodes.CLD);
        Assertions.assertFalse(runtime.getCpu().isStatusDecimal());
    }

    private void runSingleImmediateOperation(OpCodes opCode) {
        runtime.getMemory().write(0, opCode.getCode());

        runtime.performSingleInstruction();

        Assertions.assertEquals(1, runtime.getCpu().getProgramCounter());
        Assertions.assertEquals(2, runtime.getCpu().getCycles());
    }
}
