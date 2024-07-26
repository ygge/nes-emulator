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
    void givenAllBitsSetToRotateLeftOneBitThenSetCarry() {
        verifyRotateLeftOneBit(0xFF, 0xFE, true);
    }

    @Test
    void givenAllBitsAndCarrySetToRotateLeftOneBitThenSetCarry() {
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

    @Test
    void givenZeroToShiftRightOneBitThenStillZero() {
        verifyShiftRightOneBit(0, 0, false);
    }

    @Test
    void givenAllBitsSetToShiftRightOneBitThenSetCarry() {
        verifyShiftRightOneBit(0xFF, 0x7F, true);
    }

    @Test
    void givenAllButLeastSignificantBitSetToShiftRightOneBitThenDoNotSetCarry() {
        verifyShiftRightOneBit(0xFE, 0x7F, false);
    }

    @Test
    void givenZeroToRotateRightOneBitThenStillZero() {
        verifyRotateRightOneBit(0, 0, false);
    }

    @Test
    void givenZeroAndCarrySetToRotateRightOneBitThenMostSignificantBitSet() {
        runtime.getCpu().setStatusCarry();
        verifyRotateRightOneBit(0, 0x80, false);
    }

    @Test
    void givenAllBitsSetToRotateRightOneBitThenSetCarry() {
        verifyRotateRightOneBit(0xFF, 0x7F, true);
    }

    @Test
    void givenAllBitsAndCarrySetToRotateRightOneBitThenSetCarry() {
        runtime.getCpu().setStatusCarry();
        verifyRotateRightOneBit(0xFF, 0xFF, true);
    }

    @Test
    void givenAllButLeastSignificantBitSetToRotateRightOneBitThenDoNotSetCarry() {
        verifyRotateRightOneBit(0xFE, 0x7F, false);
    }

    @Test
    void givenEmptyStackToPushAccumulatorThenPushAccumulator() {
        verifyPushAccumulator(0xFF, 0x34);
    }

    @Test
    void givenAlmostFullStackToPushAccumulatorThenPushAccumulator() {
        verifyPushAccumulator(1, 0x76);
    }

    @Test
    void givenFullStackToPushAccumulatorThenThrowException() {
        Assertions.assertThrows(NESException.class, () -> verifyPushAccumulator(0, 0));
    }

    @Test
    void givenFullStackToPullAccumulatorThenPullAccumulator() {
        verifyPullAccumulator(0, 0xF0);
    }

    @Test
    void givenAlmostEmptyStackToPullAccumulatorThenPullAccumulator() {
        verifyPullAccumulator(0xFE, 1);
    }

    @Test
    void givenEmptyStackToPullAccumulatorThenThrowException() {
        Assertions.assertThrows(NESException.class, () -> verifyPullAccumulator(0xFF, 0));
    }

    @Test
    void givenAdditionThatFitsInTheResultingBitsThenNoCarry() {
        runtime.getCpu().setAccumulator((byte) 0xFE);

        var result = InstructionFunctions.addMemoryToAccumulator(runtime, (byte) 0x1);

        Assertions.assertEquals((byte) 0xFF, result);
        Assertions.assertFalse(runtime.getCpu().isStatusCarry());
    }

    @Test
    void givenAdditionThatFitsInTheResultingBitsWithCarryThatDoesNotThenSetCarry() {
        runtime.getCpu().setAccumulator((byte) 0xFE);

        var result = InstructionFunctions.addMemoryToAccumulator(runtime, (byte) 0x3);

        Assertions.assertEquals((byte) 1, result);
        Assertions.assertTrue(runtime.getCpu().isStatusCarry());
        Assertions.assertFalse(runtime.getCpu().isStatusOverflow());
    }

    @Test
    void givenAdditionThatDoesNotFitInTheResultingBitsThenSetCarry() {
        runtime.getCpu().setStatusCarry();
        runtime.getCpu().setAccumulator((byte) 0xFE);

        var result = InstructionFunctions.addMemoryToAccumulator(runtime, (byte) 0x1);

        Assertions.assertEquals((byte) 0, result);
        Assertions.assertTrue(runtime.getCpu().isStatusCarry());
        Assertions.assertFalse(runtime.getCpu().isStatusOverflow());
    }

    @Test
    void givenAdditionWithSignedValuesThatDoesNotOverflowThenDoNotSetOverflow() {
        runtime.getCpu().setAccumulator((byte) 0x7E);

        var result = InstructionFunctions.addMemoryToAccumulator(runtime, (byte) 0xFB);

        Assertions.assertEquals((byte) 0x79, result);
        Assertions.assertFalse(runtime.getCpu().isStatusCarry());
        Assertions.assertFalse(runtime.getCpu().isStatusOverflow());
    }

    @Test
    void givenAdditionWithSignedValuesThatOverflowsThenSetOverflow() {
        runtime.getCpu().setAccumulator((byte) 0x80);

        var result = InstructionFunctions.addMemoryToAccumulator(runtime, (byte) 0xFB);

        Assertions.assertEquals((byte) 0x7B, result);
        Assertions.assertFalse(runtime.getCpu().isStatusCarry());
        Assertions.assertTrue(runtime.getCpu().isStatusOverflow());
    }

    @Test
    void givenSubtractionThatDoesNotOverflowThenDoNotSetOverflow() {
        runtime.getCpu().setStatusCarry();
        runtime.getCpu().setAccumulator((byte) 0xFE);

        var result = InstructionFunctions.subtractMemoryFromAccumulator(runtime, (byte) 1);

        Assertions.assertEquals((byte) 0xFD, result);
        Assertions.assertTrue(runtime.getCpu().isStatusCarry());
        Assertions.assertFalse(runtime.getCpu().isStatusOverflow());
    }

    @Test
    void givenZeroToIncrementRegisterXThenSetOne() {
        runtime.getCpu().setRegisterX((byte) 0);

        InstructionFunctions.incrementRegisterX(runtime);

        Assertions.assertEquals((byte) 1, runtime.getCpu().getRegisterX());
    }

    @Test
    void givenMinus1ToIncrementRegisterXThenSetZero() {
        runtime.getCpu().setRegisterX((byte) 0xFF);

        InstructionFunctions.incrementRegisterX(runtime);

        Assertions.assertEquals((byte) 0, runtime.getCpu().getRegisterX());
    }

    @Test
    void givenZeroToIncrementRegisterYThenSetOne() {
        runtime.getCpu().setRegisterY((byte) 0);

        InstructionFunctions.incrementRegisterY(runtime);

        Assertions.assertEquals((byte) 1, runtime.getCpu().getRegisterY());
    }

    @Test
    void givenMinus1ToIncrementRegisterYThenSetZero() {
        runtime.getCpu().setRegisterY((byte) 0xFF);

        InstructionFunctions.incrementRegisterY(runtime);

        Assertions.assertEquals((byte) 0, runtime.getCpu().getRegisterY());
    }

    @Test
    void givenZeroToInDecrementRegisterXThenSetMinusOne() {
        runtime.getCpu().setRegisterX((byte) 0);

        InstructionFunctions.decrementRegisterX(runtime);

        Assertions.assertEquals((byte) 0xFF, runtime.getCpu().getRegisterX());
    }

    @Test
    void givenMinus1ToDecrementRegisterXThenSetMinusTwo() {
        runtime.getCpu().setRegisterX((byte) 0xFF);

        InstructionFunctions.decrementRegisterX(runtime);

        Assertions.assertEquals((byte) 0xFE, runtime.getCpu().getRegisterX());
    }

    @Test
    void givenZeroToInDecrementRegisterYThenSetMinusOne() {
        runtime.getCpu().setRegisterY((byte) 0);

        InstructionFunctions.decrementRegisterY(runtime);

        Assertions.assertEquals((byte) 0xFF, runtime.getCpu().getRegisterY());
    }

    @Test
    void givenMinus1ToDecrementRegisterYThenSetMinusTwo() {
        runtime.getCpu().setRegisterY((byte) 0xFF);

        InstructionFunctions.decrementRegisterY(runtime);

        Assertions.assertEquals((byte) 0xFE, runtime.getCpu().getRegisterY());
    }

    @Test
    void givenValueInAccumulatorToTransferAccumulatorToRegisterXThenSetValueInRegisterX() {
        runtime.getCpu().setAccumulator((byte) 0xFF);

        InstructionFunctions.transferAccumulatorToRegisterX(runtime);

        Assertions.assertEquals((byte) 0xFF, runtime.getCpu().getRegisterX());
    }

    @Test
    void givenValueInAccumulatorToTransferAccumulatorToRegisterYThenSetValueInRegisterX() {
        runtime.getCpu().setAccumulator((byte) 0x80);

        InstructionFunctions.transferAccumulatorToRegisterY(runtime);

        Assertions.assertEquals((byte) 0x80, runtime.getCpu().getRegisterY());
    }

    @Test
    void givenValueInRegisterXToTransferRegisterXToAccumulatorThenSetValueInAccumulator() {
        runtime.getCpu().setRegisterX((byte) 42);

        InstructionFunctions.transferRegisterXToAccumulator(runtime);

        Assertions.assertEquals((byte) 42, runtime.getCpu().getAccumulator());
    }

    @Test
    void givenValueInRegisterYToTransferRegisterYToAccumulatorThenSetValueInAccumulator() {
        runtime.getCpu().setRegisterY((byte) 17);

        InstructionFunctions.transferRegisterYToAccumulator(runtime);

        Assertions.assertEquals((byte) 17, runtime.getCpu().getRegisterY());
    }

    private void verifyPullAccumulator(int stackPointer, int accumulator) {
        runtime.getCpu().setStackPointer((byte) stackPointer);
        runtime.getMemory().write(0x100 | stackPointer, (byte) accumulator);
        InstructionFunctions.pullAccumulatorFromStack(runtime);

        Assertions.assertEquals((byte) (stackPointer + 1), runtime.getCpu().getStackPointer());
        Assertions.assertEquals((byte) accumulator, runtime.getCpu().getAccumulator());
    }

    private void verifyPushAccumulator(int stackPointer, int accumulator) {
        runtime.getCpu().setAccumulator((byte) accumulator);
        runtime.getCpu().setStackPointer((byte) stackPointer);
        InstructionFunctions.pushAccumulatorOnStack(runtime);

        Assertions.assertEquals((byte) (stackPointer - 1), runtime.getCpu().getStackPointer());
        Assertions.assertEquals(accumulator, runtime.getMemory().read(0x100 | stackPointer));
    }

    private void verifyRotateRightOneBit(int intValue, int intResult, boolean carrySet) {
        var ret = InstructionFunctions.rotateRightOneBit(runtime, (byte) intValue);

        Assertions.assertEquals((byte) intResult, ret);
        Assertions.assertEquals(carrySet, runtime.getCpu().isStatusCarry());
    }

    private void verifyShiftRightOneBit(int intValue, int intResult, boolean carrySet) {
        var ret = InstructionFunctions.shiftRightOneBit(runtime, (byte) intValue);

        Assertions.assertEquals((byte) intResult, ret);
        Assertions.assertEquals(carrySet, runtime.getCpu().isStatusCarry());
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
