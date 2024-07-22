package nu.ygge.nes;

import nu.ygge.nes.cpu.OpCodes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Runtime_ORATest {

    private Runtime runtime;

    @BeforeEach
    void setUp() {
        runtime = new Runtime();
        byte dummyValue = 0x17;
        for (int i = 0; i < 2048; ++i) {
            runtime.getMemory().write(i, dummyValue);
        }
    }

    @Test
    void verifyImmediateAddressingMode() {
        runtime.getCpu().setStatusNegative();
        runtime.getCpu().setStatusZero();
        runtime.getCpu().setAccumulator((byte)7);
        runtime.getMemory().write(1, (byte)0x42);

        runSingleImmediateOperation(OpCodes.ORAI);

        Assertions.assertEquals(0x47, runtime.getCpu().getAccumulator());
        Assertions.assertEquals(2, runtime.getCpu().getProgramCounter());
        Assertions.assertEquals(2, runtime.getCpu().getCycles());
        Assertions.assertFalse(runtime.getCpu().isStatusNegative());
        Assertions.assertFalse(runtime.getCpu().isStatusZero());
    }

    @Test
    void verifyAbsoluteAddressingMode() {
        runtime.getCpu().setStatusZero();
        runtime.getCpu().setAccumulator((byte)0xFE);
        runtime.getMemory().write(1, (byte)2);
        runtime.getMemory().write(2, (byte)0);

        runSingleImmediateOperation(OpCodes.ORAA);

        Assertions.assertEquals((byte)0xFF, runtime.getCpu().getAccumulator());
        Assertions.assertEquals(3, runtime.getCpu().getProgramCounter());
        Assertions.assertEquals(4, runtime.getCpu().getCycles());
        Assertions.assertTrue(runtime.getCpu().isStatusNegative());
        Assertions.assertFalse(runtime.getCpu().isStatusZero());
    }

    @Test
    void verifyZeroPageAddressingMode() {
        runtime.getCpu().setStatusZero();
        runtime.getCpu().setStatusNegative();
        runtime.getCpu().setAccumulator((byte)2);
        runtime.getMemory().write(1, (byte)2);
        runtime.getMemory().write(2, (byte)1);

        runSingleImmediateOperation(OpCodes.ORAZ);

        Assertions.assertEquals((byte)3, runtime.getCpu().getAccumulator());
        Assertions.assertEquals(2, runtime.getCpu().getProgramCounter());
        Assertions.assertEquals(3, runtime.getCpu().getCycles());
        Assertions.assertFalse(runtime.getCpu().isStatusNegative());
        Assertions.assertFalse(runtime.getCpu().isStatusZero());
    }

    private void runSingleImmediateOperation(OpCodes opCode) {
        runtime.getMemory().write(0, opCode.getCode());
        runtime.performSingleInstruction();
    }
}
