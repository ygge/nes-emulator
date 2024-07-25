package nu.ygge.nes.cpu.instructions;

import nu.ygge.nes.NESRuntime;
import nu.ygge.nes.exception.NESException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class InstructionFunctionsTest {

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

    @Test
    void givenZeroToRotateLeftOneBitThenStillZero() {
        verifyRotateLeftOneBit(0, 0, false);
    }

    @Test
    void givenZeroAndCarrySetToRotateLeftOneBitThenOne() {
        runtime.getCpu().setStatusCarry();
        verifyRotateLeftOneBit(0, 1, false);
    }

    @Test
    void givenAllBitsSetToRotateOneBitThenSetCarry() {
        verifyRotateLeftOneBit(0xFF, 0xFE, true);
    }

    @Test
    void givenAllBitsAndCarrySetToRotateOneBitThenSetCarry() {
        runtime.getCpu().setStatusCarry();
        verifyRotateLeftOneBit(0xFF, 0xFF, true);
    }

    @Test
    void givenAllButMostSignificantBitSetToRotateLeftOneBitThenDoNotSetCarry() {
        verifyRotateLeftOneBit(0x7F, 0xFE, false);
    }

    @Test
    void givenBit7SetOfOperandAndAccumulatorZeroToTestBitThenResultIsZeroButNegativeFlagSet() {
        verifyTestBit(0x80, 0, 0, true, false);
    }

    @Test
    void givenBit6SetOfOperandAndAccumulatorZeroToTestBitThenResultIsZeroButOverflowFlagSet() {
        verifyTestBit(0x40, 0, 0, false, true);
    }

    @Test
    void givenAllBitsSetOfBothOperandAndAccumulatorToTestBitThenResultIsAllBitsSet() {
        verifyTestBit(0xFF, 0xFF, 0xFF, true, true);
    }

    @Test
    void givenNoOverlappingBitsSetOfOperandAndAccumulatorToTestBitThenResultIsZero() {
        verifyTestBit(0xF0, 0x0F, 0, true, true);
    }

    @Test
    void givenFullStackToPullProcessorStatusThenPullStatus() {
        verifyPullProcessorStatus(0, 0xF0);
        Assertions.assertTrue(runtime.getCpu().isStatusNegative());
        Assertions.assertFalse(runtime.getCpu().isStatusCarry());
    }

    @Test
    void givenAlmostEmptyStackToPullProcessorStatusThenPullStatus() {
        verifyPullProcessorStatus(0xFE, 1);
        Assertions.assertFalse(runtime.getCpu().isStatusNegative());
        Assertions.assertTrue(runtime.getCpu().isStatusCarry());
    }

    @Test
    void givenEmptyStackToPullProcessorStatusThenThrowException() {
        Assertions.assertThrows(NESException.class, () -> verifyPullProcessorStatus(0xFF, 0));
    }
    @Test
    void givenZeroAndPositiveValueToExclusiveOrMemoryWithAccumulatorThenSetAccumulatorToResult() {
        verifyExclusiveOrMemoryWithAccumulator(0, 42, 42);
    }

    @Test
    void givenZeroAndNegativeValueToExclusiveOrMemoryWithAccumulatorThenSetAccumulatorToResult() {
        verifyExclusiveOrMemoryWithAccumulator(0, 0xEE, 0xEE);
    }

    @Test
    void givenPositiveAndNegativeValueToExclusiveOrMemoryWithAccumulatorThenSetAccumulatorToResult() {
        verifyExclusiveOrMemoryWithAccumulator(1, 0xEE, 0xEF);
    }

    @Test
    void givenOverlappingBitsToExclusiveOrMemoryWithAccumulatorThenSetAccumulatorToResult() {
        verifyExclusiveOrMemoryWithAccumulator(0xFF, 0x7F, 0x80);
    }

    private void verifyExclusiveOrMemoryWithAccumulator(int accumulator, int value, int intResult) {
        runtime.getCpu().setAccumulator((byte) accumulator);
        var ret = InstructionFunctions.exclusiveOrMemoryWithAccumulator(runtime, (byte) value);

        Assertions.assertEquals((byte) intResult, ret);
    }

    private void verifyPullProcessorStatus(int stackPointer, int statusRegister) {
        runtime.getCpu().setStackPointer((byte) stackPointer);
        runtime.getMemory().write(0x100 | stackPointer, (byte) statusRegister);
        InstructionFunctions.pullProcessorStatusFromStack(runtime);

        Assertions.assertEquals((byte) (stackPointer + 1), runtime.getCpu().getStackPointer());
    }

    private void verifyTestBit(int operand, int accumulator, int result, boolean negativeSet, boolean overflowSet) {
        runtime.getCpu().setAccumulator((byte) accumulator);
        var ret = InstructionFunctions.testBitsWithAccumulator(runtime, (byte) operand);

        Assertions.assertEquals((byte) result, ret);
        Assertions.assertEquals(negativeSet, runtime.getCpu().isStatusNegative());
        Assertions.assertEquals(overflowSet, runtime.getCpu().isStatusOverflow());
    }

    private void verifyRotateLeftOneBit(int intValue, int intResult, boolean carrySet) {
        var ret = InstructionFunctions.rotateLeftOneBit(runtime, (byte) intValue);

        Assertions.assertEquals((byte) intResult, ret);
        Assertions.assertEquals(carrySet, runtime.getCpu().isStatusCarry());
    }

    private void verifyAndMemoryWithAccumulator(int intValue1, int intValue2, int intResult) {
        runtime.getCpu().setAccumulator((byte) intValue1);
        var ret = InstructionFunctions.andMemoryWithAccumulator(runtime, (byte) intValue2);

        Assertions.assertEquals((byte) intResult, ret);
    }

    private void verifyPushProcessorStatus(int stackPointer) {
        runtime.getCpu().setStatusCarry();
        runtime.getCpu().setStatusNegative();
        runtime.getCpu().setStackPointer((byte) stackPointer);
        InstructionFunctions.pushProcessorStatusOnStack(runtime);

        Assertions.assertEquals((byte) (stackPointer - 1), runtime.getCpu().getStackPointer());
        Assertions.assertEquals(runtime.getCpu().getStatusRegister(), runtime.getMemory().read(0x100 | stackPointer));
    }

    private void verifyShiftLeftOneBit(int intValue, int intResult, boolean carrySet) {
        var ret = InstructionFunctions.shiftLeftOneBit(runtime, (byte) intValue);

        Assertions.assertEquals((byte) intResult, ret);
        Assertions.assertEquals(carrySet, runtime.getCpu().isStatusCarry());
    }

    private void verifyOrMemoryWithAccumulator(int intValue1, int intValue2, int intResult) {
        runtime.getCpu().setAccumulator((byte) intValue1);
        var ret = InstructionFunctions.orMemoryWithAccumulator(runtime, (byte) intValue2);

        Assertions.assertEquals((byte) intResult, ret);
    }

    private void verifyLoadAccumulator(int intValue) {
        byte value = (byte) intValue;
        var ret = InstructionFunctions.loadAccumulator(runtime, value);

        Assertions.assertEquals(value, ret);
        Assertions.assertEquals(value, runtime.getCpu().getAccumulator());
    }
}
