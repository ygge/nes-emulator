package nu.ygge.nes.emulator;

import nu.ygge.nes.emulator.cpu.OpCodes;
import nu.ygge.nes.emulator.exception.NESException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NESRuntime_ImpliedAddressingModeTest {

    private NESRuntime runtime;

    @BeforeEach
    void setUp() {
        runtime = new NESRuntime();
    }

    @Test
    void verifySetCarryFlag() {
        runSingleImpliedOperation(OpCodes.SEC);
        Assertions.assertTrue(runtime.getCpu().isStatusCarry());
    }

    @Test
    void verifyClearCarryFlag() {
        runtime.getCpu().setStatusCarry();
        runSingleImpliedOperation(OpCodes.CLC);
        Assertions.assertFalse(runtime.getCpu().isStatusCarry());
    }

    @Test
    void verifyClearOverflowFlag() {
        runtime.getCpu().setStatusOverflow();
        runSingleImpliedOperation(OpCodes.CLV);
        Assertions.assertFalse(runtime.getCpu().isStatusOverflow());
    }

    @Test
    void verifySetInterruptFlag() {
        runSingleImpliedOperation(OpCodes.SEI);
        Assertions.assertTrue(runtime.getCpu().isStatusInterrupt());
    }

    @Test
    void verifyClearInterruptFlag() {
        runtime.getCpu().setStatusInterrupt();
        runSingleImpliedOperation(OpCodes.CLI);
        Assertions.assertFalse(runtime.getCpu().isStatusInterrupt());
    }

    @Test
    void verifySetDecimalFlag() {
        runSingleImpliedOperation(OpCodes.SED);
        Assertions.assertTrue(runtime.getCpu().isStatusDecimal());
    }

    @Test
    void verifyClearDecimalFlag() {
        runtime.getCpu().setStatusCarry();
        runSingleImpliedOperation(OpCodes.CLD);
        Assertions.assertFalse(runtime.getCpu().isStatusDecimal());
    }

    @Test
    void verifyPushStackPointer() {
        runtime.getCpu().setStackPointer((byte)0xFF);
        runtime.getCpu().setStatusCarry();

        runSingleImpliedOperation(OpCodes.PHP, 3);

        Assertions.assertEquals((byte)0xFE, runtime.getCpu().getStackPointer());
        Assertions.assertEquals((byte)0b110001, runtime.getMemory().read(0x100 + 0xFF));
    }

    @Test
    void verifyStackOverflowOnToManyPushStackPointer() {
        runtime.getCpu().setStackPointer((byte)0xFF);
        for (int i = 0; i < 256; ++i) {
            runtime.getMemory().write(i, OpCodes.PHP.getCode());
        }

        for (int i = 0; i < 255; ++i) {
            runtime.performSingleInstruction(); // should succeed but fill up the stack
        }

        Assertions.assertThrows(NESException.class, runtime::performSingleInstruction);
    }

    private void runSingleImpliedOperation(OpCodes opCode) {
        runSingleImpliedOperation(opCode, 2);
    }

    private void runSingleImpliedOperation(OpCodes opCode, int cycles) {
        runtime.getMemory().write(0, opCode.getCode());

        runtime.performSingleInstruction();

        Assertions.assertEquals(1, runtime.getCpu().getProgramCounter());
        Assertions.assertEquals(cycles, runtime.getCpu().getCycles());
    }
}
