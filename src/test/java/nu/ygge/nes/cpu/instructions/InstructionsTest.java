package nu.ygge.nes.cpu.instructions;

import nu.ygge.nes.NESRuntime;
import nu.ygge.nes.exception.NESException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InstructionsTest {

    private NESRuntime runtime;

    @BeforeEach
    void setup() {
        runtime = new NESRuntime();
    }

    @Test
    void givenZeroToLoadAccumulatorThenSetItToZero() {
        verifyLoadAccumulator(0);
    }

    @Test
    void givenPositiveValueToLoadAccumulatorThenSetItToSameValue() {
        verifyLoadAccumulator(42);
    }

    @Test
    void givenNegativeValueToLoadAccumulatorThenSetItToSameValue() {
        verifyLoadAccumulator(0xD4);
    }

    @Test
    void givenZeroAndPositiveValueToOrMemoryWithAccumulatorThenSetAccumulatorToResult() {
        verifyOrMemoryWithAccumulator(0, 42, 42);
    }

    @Test
    void givenZeroAndNegativeValueToOrMemoryWithAccumulatorThenSetAccumulatorToResult() {
        verifyOrMemoryWithAccumulator(0, 0xEE, 0xEE);
    }

    @Test
    void givenPositiveAndNegativeValueToOrMemoryWithAccumulatorThenSetAccumulatorToResult() {
        verifyOrMemoryWithAccumulator(1, 0xEE, 0xEF);
    }

    @Test
    void givenZeroToShiftLeftOneBitThenStillZero() {
        verifyShiftLeftOneBit(0, 0, false);
    }

    @Test
    void givenAllBitsSetToShiftLeftOneBitThenSetCarry() {
        verifyShiftLeftOneBit(0xFF, 0xFE, true);
    }

    @Test
    void givenAllButMostSignificantBitSetToShiftLeftOneBitThenDoNotSetCarry() {
        verifyShiftLeftOneBit(0x7F, 0xFE, false);
    }

    @Test
    void givenEmptyStackToPushProcessorStatusThenPushStatus() {
        verifyPushProcessorStatus(0xFF);
    }

    @Test
    void givenAlmostFullStackToPushProcessorStatusThenPushStatus() {
        verifyPushProcessorStatus(1);
    }

    @Test
    void givenFullStackToPushProcessorStatusThenThrowException() {
        Assertions.assertThrows(NESException.class, () -> verifyPushProcessorStatus(0));
    }

    @Test
    void givenZeroAndPositiveValueToAndMemoryWithAccumulatorThenSetAccumulatorToZero() {
        verifyAndMemoryWithAccumulator(0, 42, 0);
    }

    @Test
    void givenZeroAndNegativeValueToAndMemoryWithAccumulatorThenSetAccumulatorToZero() {
        verifyAndMemoryWithAccumulator(0, 0xEE, 0);
    }

    @Test
    void givenPositiveAndNegativeValuesNotOverlappingToAndMemoryWithAccumulatorThenSetAccumulatorToZero() {
        verifyAndMemoryWithAccumulator(1, 0xEE, 0);
    }

    @Test
    void givenPositiveAndNegativeValuesOverlappingToAndMemoryWithAccumulatorThenSetAccumulatorToResult() {
        verifyAndMemoryWithAccumulator(0xFE, 0x88, 0x88);
    }

    private void verifyAndMemoryWithAccumulator(int intValue1, int intValue2, int intResult) {
        runtime.getCpu().setAccumulator((byte) intValue1);
        var ret = Instructions.andMemoryWithAccumulator(runtime, (byte) intValue2);

        byte result = (byte) intResult;
        Assertions.assertEquals(result, ret);
        Assertions.assertEquals(result, runtime.getCpu().getAccumulator());
    }

    private void verifyPushProcessorStatus(int stackPointer) {
        runtime.getCpu().setStatusCarry();
        runtime.getCpu().setStatusNegative();
        runtime.getCpu().setStackPointer((byte) stackPointer);
        Instructions.pushProcessorStatusOnStack(runtime);

        Assertions.assertEquals((byte) (stackPointer - 1), runtime.getCpu().getStackPointer());
        Assertions.assertEquals(runtime.getCpu().getStatusRegister(), runtime.getMemory().read(0x100 | stackPointer));
    }

    private void verifyShiftLeftOneBit(int intValue, int intResult, boolean carrySet) {
        var ret = Instructions.shiftLeftOneBit(runtime, (byte) intValue);

        Assertions.assertEquals((byte) intResult, ret);
        Assertions.assertEquals(carrySet, runtime.getCpu().isStatusCarry());
    }

    private void verifyOrMemoryWithAccumulator(int intValue1, int intValue2, int intResult) {
        runtime.getCpu().setAccumulator((byte) intValue1);
        var ret = Instructions.orMemoryWithAccumulator(runtime, (byte) intValue2);

        byte result = (byte) intResult;
        Assertions.assertEquals(result, ret);
        Assertions.assertEquals(result, runtime.getCpu().getAccumulator());
    }

    private void verifyLoadAccumulator(int intValue) {
        byte value = (byte) intValue;
        var ret = Instructions.loadAccumulator(runtime, value);

        Assertions.assertEquals(value, ret);
        Assertions.assertEquals(value, runtime.getCpu().getAccumulator());
    }
}
