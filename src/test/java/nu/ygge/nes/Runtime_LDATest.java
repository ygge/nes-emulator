package nu.ygge.nes;

import nu.ygge.nes.cpu.OpCodes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class Runtime_LDATest {

    private Runtime runtime;

    @BeforeEach
    void setUp() {
        runtime = new Runtime();
    }

    @Test
    void verifyImmediateAddressingMode() {
        runtime.getCpu().setStatusNegative();
        runtime.getCpu().setStatusZero();
        runtime.getRam().write(1, (byte)0x42);

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
        runtime.getRam().write(1, (byte)0);
        runtime.getRam().write(2, (byte)1);

        runSingleImmediateOperation(OpCodes.LDA);

        Assertions.assertEquals((byte)0, runtime.getCpu().getAccumulator());
        Assertions.assertEquals(3, runtime.getCpu().getProgramCounter());
        Assertions.assertEquals(4, runtime.getCpu().getCycles());
        Assertions.assertFalse(runtime.getCpu().isStatusNegative());
        Assertions.assertTrue(runtime.getCpu().isStatusZero());
    }

    private void runSingleImmediateOperation(OpCodes opCode) {
        runtime.getRam().write(0, opCode.getCode());
        runtime.performSingleInstruction();
    }
}
