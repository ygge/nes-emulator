package nu.ygge.nes.emulator;

import nu.ygge.nes.emulator.cpu.OpCodes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NESRuntime_LDATest {

    private NESRuntime runtime;

    @BeforeEach
    void setUp() {
        runtime = new NESRuntime();
        byte dummyValue = 0x17;
        for (int i = 0; i < 2048; ++i) {
            runtime.getMemory().write(i, dummyValue);
        }
    }

    @Test
    void verifyImmediateAddressingMode() {
        runtime.getCpu().setStatusNegative();
        runtime.getCpu().setStatusZero();
        runtime.getMemory().write(1, (byte) 0x42);

        runSingleImmediateOperation(OpCodes.LDAI);

        Assertions.assertEquals(0x42, runtime.getCpu().getAccumulator());
        Assertions.assertEquals(2, runtime.getCpu().getProgramCounter());
        Assertions.assertEquals(2, runtime.getCpu().getCycles());
        Assertions.assertFalse(runtime.getCpu().isStatusNegative());
        Assertions.assertFalse(runtime.getCpu().isStatusZero());
    }

    @Test
    void verifyAbsoluteAddressingMode() {
        runtime.getCpu().setStatusNegative();
        runtime.getMemory().write(1, (byte) 2);
        runtime.getMemory().write(2, (byte) 0);

        runSingleImmediateOperation(OpCodes.LDAA);

        Assertions.assertEquals((byte) 0, runtime.getCpu().getAccumulator());
        Assertions.assertEquals(3, runtime.getCpu().getProgramCounter());
        Assertions.assertEquals(4, runtime.getCpu().getCycles());
        Assertions.assertFalse(runtime.getCpu().isStatusNegative());
        Assertions.assertTrue(runtime.getCpu().isStatusZero());
    }

    @Test
    void verifyZeroPageAddressingMode() {
        runtime.getCpu().setStatusZero();
        runtime.getMemory().write(1, (byte) 2);
        runtime.getMemory().write(2, (byte) 0xFF);

        runSingleImmediateOperation(OpCodes.LDAZ);

        Assertions.assertEquals((byte) 0xFF, runtime.getCpu().getAccumulator());
        Assertions.assertEquals(2, runtime.getCpu().getProgramCounter());
        Assertions.assertEquals(3, runtime.getCpu().getCycles());
        Assertions.assertTrue(runtime.getCpu().isStatusNegative());
        Assertions.assertFalse(runtime.getCpu().isStatusZero());
    }

    @Test
    void verifyZeroPageXAddressingMode() {
        runtime.getCpu().setStatusNegative();
        runtime.getCpu().setRegisterX((byte) 0xFF);
        runtime.getMemory().write(1, (byte) 3);
        runtime.getMemory().write(2, (byte) 0xFF);

        runSingleImmediateOperation(OpCodes.LDAZX);

        Assertions.assertEquals((byte) 0xFF, runtime.getCpu().getAccumulator());
        Assertions.assertEquals(2, runtime.getCpu().getProgramCounter());
        Assertions.assertEquals(4, runtime.getCpu().getCycles());
        Assertions.assertTrue(runtime.getCpu().isStatusNegative());
        Assertions.assertFalse(runtime.getCpu().isStatusZero());
    }

    @Test
    void verifyAbsoluteXAddressingMode() {
        runtime.getCpu().setRegisterX((byte) 1);
        runtime.getCpu().setStatusNegative();
        runtime.getMemory().write(1, (byte) 0);
        runtime.getMemory().write(2, (byte) 0);

        runSingleImmediateOperation(OpCodes.LDAAX);

        Assertions.assertEquals((byte) 0, runtime.getCpu().getAccumulator());
        Assertions.assertEquals(3, runtime.getCpu().getProgramCounter());
        Assertions.assertEquals(4, runtime.getCpu().getCycles());
        Assertions.assertFalse(runtime.getCpu().isStatusNegative());
        Assertions.assertTrue(runtime.getCpu().isStatusZero());
    }

    @Test
    void verifyAbsoluteYAddressingMode() {
        runtime.getCpu().setRegisterY((byte) 1);
        runtime.getCpu().setStatusNegative();
        runtime.getMemory().write(1, (byte) 0);
        runtime.getMemory().write(2, (byte) 0);

        runSingleImmediateOperation(OpCodes.LDAAY);

        Assertions.assertEquals((byte) 0, runtime.getCpu().getAccumulator());
        Assertions.assertEquals(3, runtime.getCpu().getProgramCounter());
        Assertions.assertEquals(4, runtime.getCpu().getCycles());
        Assertions.assertFalse(runtime.getCpu().isStatusNegative());
        Assertions.assertTrue(runtime.getCpu().isStatusZero());
    }

    @Test
    void verifyIndirectXAddressingMode() {
        runtime.getCpu().setRegisterX((byte) 1);
        runtime.getCpu().setStatusNegative();
        runtime.getMemory().write(1, (byte) 2);
        runtime.getMemory().write(3, (byte) 4);
        runtime.getMemory().write(4, (byte) 0);

        runSingleImmediateOperation(OpCodes.LDAIX);

        Assertions.assertEquals((byte) 0, runtime.getCpu().getAccumulator());
        Assertions.assertEquals(2, runtime.getCpu().getProgramCounter());
        Assertions.assertEquals(6, runtime.getCpu().getCycles());
        Assertions.assertFalse(runtime.getCpu().isStatusNegative());
        Assertions.assertTrue(runtime.getCpu().isStatusZero());
    }

    @Test
    void verifyIndirectYAddressingMode() {
        runtime.getCpu().setRegisterY((byte) 0xF0);
        runtime.getCpu().setStatusZero();
        runtime.getMemory().write(1, (byte) 2);
        runtime.getMemory().write(2, (byte) 2);
        runtime.getMemory().write(3, (byte) 1);
        runtime.getMemory().write(0x1F2, (byte) 0xFE);

        runSingleImmediateOperation(OpCodes.LDAIY);

        Assertions.assertEquals((byte) 0xFE, runtime.getCpu().getAccumulator());
        Assertions.assertEquals(2, runtime.getCpu().getProgramCounter());
        Assertions.assertEquals(5, runtime.getCpu().getCycles());
        Assertions.assertTrue(runtime.getCpu().isStatusNegative());
        Assertions.assertFalse(runtime.getCpu().isStatusZero());
    }

    @Test
    void verifyIndirectYAddressingModeWithageBoundaryCrossing() {
        runtime.getCpu().setRegisterY((byte) 0xFF);
        runtime.getCpu().setStatusZero();
        runtime.getMemory().write(1, (byte) 2);
        runtime.getMemory().write(2, (byte) 2);
        runtime.getMemory().write(3, (byte) 1);
        runtime.getMemory().write(0x201, (byte) 0xFE);

        runSingleImmediateOperation(OpCodes.LDAIY);

        Assertions.assertEquals((byte) 0xFE, runtime.getCpu().getAccumulator());
        Assertions.assertEquals(2, runtime.getCpu().getProgramCounter());
        Assertions.assertEquals(6, runtime.getCpu().getCycles());
        Assertions.assertTrue(runtime.getCpu().isStatusNegative());
        Assertions.assertFalse(runtime.getCpu().isStatusZero());
    }

    private void runSingleImmediateOperation(OpCodes opCode) {
        runtime.getMemory().write(0, opCode.getCode());
        runtime.performSingleInstruction();
    }
}
