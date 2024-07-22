package nu.ygge.nes;

import nu.ygge.nes.cpu.OpCodes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Runtime_ASLTest {

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
    void verifyAccumulatorAddressingMode() {
        runtime.getCpu().setStatusNegative();
        runtime.getCpu().setStatusZero();
        runtime.getCpu().setStatusCarry();
        runtime.getCpu().setAccumulator((byte)7);

        runSingleImmediateOperation(OpCodes.ASLAC);

        Assertions.assertEquals(0xE, runtime.getCpu().getAccumulator());
        Assertions.assertEquals(1, runtime.getCpu().getProgramCounter());
        Assertions.assertEquals(2, runtime.getCpu().getCycles());
        Assertions.assertFalse(runtime.getCpu().isStatusNegative());
        Assertions.assertFalse(runtime.getCpu().isStatusZero());
        Assertions.assertFalse(runtime.getCpu().isStatusCarry());
    }

    @Test
    void verifyAbsoluteAddressingMode() {
        runtime.getCpu().setStatusNegative();
        runtime.getCpu().setStatusZero();
        runtime.getMemory().write(1, (byte)0);
        runtime.getMemory().write(2, (byte)3);
        runtime.getMemory().write(3, (byte)0x80);

        runSingleImmediateOperation(OpCodes.ASLA);

        Assertions.assertEquals((byte)0, runtime.getMemory().read(3));
        Assertions.assertEquals(3, runtime.getCpu().getProgramCounter());
        Assertions.assertEquals(6, runtime.getCpu().getCycles());
        Assertions.assertFalse(runtime.getCpu().isStatusNegative());
        Assertions.assertTrue(runtime.getCpu().isStatusZero());
        Assertions.assertTrue(runtime.getCpu().isStatusCarry());
    }

    @Test
    void verifyZeroPageAddressingMode() {
        runtime.getCpu().setStatusZero();
        runtime.getMemory().write(1, (byte)2);
        runtime.getMemory().write(2, (byte)0xC0);

        runSingleImmediateOperation(OpCodes.ASLZ);

        Assertions.assertEquals((byte)0x80, runtime.getMemory().read(2));
        Assertions.assertEquals(2, runtime.getCpu().getProgramCounter());
        Assertions.assertEquals(5, runtime.getCpu().getCycles());
        Assertions.assertTrue(runtime.getCpu().isStatusNegative());
        Assertions.assertFalse(runtime.getCpu().isStatusZero());
        Assertions.assertTrue(runtime.getCpu().isStatusCarry());
    }

    private void runSingleImmediateOperation(OpCodes opCode) {
        runtime.getMemory().write(0, opCode.getCode());
        runtime.performSingleInstruction();
    }
}
