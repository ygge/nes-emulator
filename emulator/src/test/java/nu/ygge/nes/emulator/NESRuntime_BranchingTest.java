package nu.ygge.nes.emulator;

import nu.ygge.nes.emulator.cpu.OpCodes;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NESRuntime_BranchingTest {

    private static final int PROGRAM_COUNTER = 256;

    private NESRuntime runtime;

    @BeforeEach
    void setUp() {
        runtime = new NESRuntime();
        runtime.getCpu().setProgramCounter(PROGRAM_COUNTER);
    }

    @Test
    void givenStatusCarryToBranchOnResultNotCarryThenDoNotBranch() {
        runtime.getCpu().setStatusCarry();
        runSingleImmediateOperation(OpCodes.BCC, 15);

        Assertions.assertEquals(PROGRAM_COUNTER + 2, runtime.getCpu().getProgramCounter());
    }

    @Test
    void givenStatusNotCarryToBranchOnResultNotCarryThenBranch() {
        runSingleImmediateOperation(OpCodes.BCC, 15);

        Assertions.assertEquals(PROGRAM_COUNTER + 17, runtime.getCpu().getProgramCounter());
    }

    @Test
    void givenStatusCarryToBranchOnResultCarryThenBranch() {
        runtime.getCpu().setStatusCarry();
        runSingleImmediateOperation(OpCodes.BCS, -5);

        Assertions.assertEquals(PROGRAM_COUNTER - 3, runtime.getCpu().getProgramCounter());
    }

    @Test
    void givenStatusNotCarryToBranchOnResultCarryThenDoNotBranch() {
        runSingleImmediateOperation(OpCodes.BCS, -5);

        Assertions.assertEquals(PROGRAM_COUNTER + 2, runtime.getCpu().getProgramCounter());
    }

    @Test
    void givenStatusZeroToBranchOnResultZeroThenDoBranch() {
        runtime.getCpu().setStatusZero();
        runSingleImmediateOperation(OpCodes.BEQ, 6);

        Assertions.assertEquals(PROGRAM_COUNTER + 8, runtime.getCpu().getProgramCounter());
    }

    @Test
    void givenStatusNotZeroToBranchOnResultZeroThenDoNotBranch() {
        runSingleImmediateOperation(OpCodes.BEQ, 25);

        Assertions.assertEquals(PROGRAM_COUNTER + 2, runtime.getCpu().getProgramCounter());
    }

    @Test
    void givenStatusZeroToBranchOnResultNotZeroThenDoNotBranch() {
        runtime.getCpu().setStatusZero();
        runSingleImmediateOperation(OpCodes.BNE, 24);

        Assertions.assertEquals(PROGRAM_COUNTER + 2, runtime.getCpu().getProgramCounter());
    }

    @Test
    void givenStatusNotZeroToBranchOnResultNotZeroThenBranch() {
        runSingleImmediateOperation(OpCodes.BNE, 25);

        Assertions.assertEquals(PROGRAM_COUNTER + 27, runtime.getCpu().getProgramCounter());
    }

    @Test
    void givenStatusNegativeToBranchOnResultPlusThenDoNotBranch() {
        runtime.getCpu().setStatusNegative();
        runSingleImmediateOperation(OpCodes.BPL, 24);

        Assertions.assertEquals(PROGRAM_COUNTER + 2, runtime.getCpu().getProgramCounter());
    }

    @Test
    void givenStatusNotNegativeToBranchOnResultNotNegativeThenBranch() {
        runSingleImmediateOperation(OpCodes.BPL, 12);

        Assertions.assertEquals(PROGRAM_COUNTER + 14, runtime.getCpu().getProgramCounter());
    }

    @Test
    void givenStatusNegativeToBranchOnResultNegativeThenBranch() {
        runtime.getCpu().setStatusNegative();
        runSingleImmediateOperation(OpCodes.BMI, -12);

        Assertions.assertEquals(PROGRAM_COUNTER - 10, runtime.getCpu().getProgramCounter());
    }

    @Test
    void givenStatusNotNegativeToBranchOnResultNegativeThenDoNotBranch() {
        runSingleImmediateOperation(OpCodes.BMI, -12);

        Assertions.assertEquals(PROGRAM_COUNTER + 2, runtime.getCpu().getProgramCounter());
    }

    @Test
    void givenStatusOverflowToBranchOnResultNotOverflowThenDoNotBranch() {
        runtime.getCpu().setStatusOverflow();
        runSingleImmediateOperation(OpCodes.BVC, -100);

        Assertions.assertEquals(PROGRAM_COUNTER + 2, runtime.getCpu().getProgramCounter());
    }

    @Test
    void givenStatusNotOverflowToBranchOnResultNotOverflowThenBranch() {
        runSingleImmediateOperation(OpCodes.BVC, -100);

        Assertions.assertEquals(PROGRAM_COUNTER - 98, runtime.getCpu().getProgramCounter());
    }

    @Test
    void givenStatusOverflowToBranchOnResultOverflowThenBranch() {
        runtime.getCpu().setStatusOverflow();
        runSingleImmediateOperation(OpCodes.BVS, 120);

        Assertions.assertEquals(PROGRAM_COUNTER + 122, runtime.getCpu().getProgramCounter());
    }

    @Test
    void givenStatusNotOverflowToBranchOnResultOverflowThenDoNotBranch() {
        runSingleImmediateOperation(OpCodes.BVS, -100);

        Assertions.assertEquals(PROGRAM_COUNTER + 2, runtime.getCpu().getProgramCounter());
    }

    private void runSingleImmediateOperation(OpCodes opCode, int value) {
        runtime.getMemory().write(PROGRAM_COUNTER, opCode.getCode());
        runtime.getMemory().write(PROGRAM_COUNTER + 1, (byte)value);
        runtime.performSingleInstruction();
    }
}
