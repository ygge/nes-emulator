package nu.ygge.nes.emulator;

import nu.ygge.nes.emulator.cpu.OpCodes;
import nu.ygge.nes.emulator.util.MemoryWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NESRuntime_AdditionAndSubtractionTest {

    private NESRuntime runtime;
    private MemoryWriter memoryWriter;

    @BeforeEach
    void setUp() {
        runtime = new NESRuntime();
        int pc = 0x100;
        runtime.getCpu().setProgramCounter(pc);
        memoryWriter = new MemoryWriter(runtime.getBus(), pc);
    }

    // $00FE + $0003 (254 + 3 in decimal)
    @Test
    void testAdditionWithCarry() {
        // Store LSB of first value in memory at address $0000.
        memoryWriter.write(OpCodes.LDAI.getCode());
        memoryWriter.write((byte) 0xFE);
        memoryWriter.write(OpCodes.STAZ.getCode());
        memoryWriter.write((byte) 0);

        // Store MSB of first value in memory at address $0001.
        memoryWriter.write(OpCodes.LDAI.getCode());
        memoryWriter.write((byte) 0x00);
        memoryWriter.write(OpCodes.STAZ.getCode());
        memoryWriter.write((byte) 1);

        // Store LSB of second value in memory at address $0002.
        memoryWriter.write(OpCodes.LDAI.getCode());
        memoryWriter.write((byte) 0x03);
        memoryWriter.write(OpCodes.STAZ.getCode());
        memoryWriter.write((byte) 2);

        // Store MSB of second value in memory at address $0003.
        memoryWriter.write(OpCodes.LDAI.getCode());
        memoryWriter.write((byte) 0x00);
        memoryWriter.write(OpCodes.STAZ.getCode());
        memoryWriter.write((byte) 3);

        memoryWriter.write(OpCodes.CLC.getCode());

        // Add the LSBs
        memoryWriter.write(OpCodes.LDAZ.getCode());
        memoryWriter.write((byte) 0);
        memoryWriter.write(OpCodes.ADCZ.getCode());
        memoryWriter.write((byte) 2);
        memoryWriter.write(OpCodes.STAZ.getCode());
        memoryWriter.write((byte) 4);

        // Add the MSBs, including the carry bit from the first addition.
        memoryWriter.write(OpCodes.LDAZ.getCode());
        memoryWriter.write((byte) 1);
        memoryWriter.write(OpCodes.ADCZ.getCode());
        memoryWriter.write((byte) 3);
        memoryWriter.write(OpCodes.STAZ.getCode());
        memoryWriter.write((byte) 5);

        while (runtime.getCpu().getProgramCounter() < memoryWriter.getAddress()) {
            runtime.performSingleInstruction();
        }

        Assertions.assertEquals((byte) 0xFE, runtime.getBus().read(0));
        Assertions.assertEquals((byte) 0x00, runtime.getBus().read(1));
        Assertions.assertEquals((byte) 0x03, runtime.getBus().read(2));
        Assertions.assertEquals((byte) 0x00, runtime.getBus().read(3));
        Assertions.assertEquals((byte) 0x01, runtime.getBus().read(4));
        Assertions.assertEquals((byte) 0x01, runtime.getBus().read(5));
        Assertions.assertFalse(runtime.getCpu().isStatusCarry());
    }

    // $FF80 + $FFFB (-128 + -5 in decimal)
    @Test
    void testAdditionWithSignedValues() {
        // Store LSB of -128 in memory at address $0000.
        memoryWriter.write(OpCodes.LDAI.getCode());
        memoryWriter.write((byte) 0x80);
        memoryWriter.write(OpCodes.STAZ.getCode());
        memoryWriter.write((byte) 0);

        // Store MSB of -128 in memory at address $0001.
        memoryWriter.write(OpCodes.LDAI.getCode());
        memoryWriter.write((byte) 0xFF);
        memoryWriter.write(OpCodes.STAZ.getCode());
        memoryWriter.write((byte) 1);

        // Store LSB of -5 in memory at address $0002.
        memoryWriter.write(OpCodes.LDAI.getCode());
        memoryWriter.write((byte) 0xFB);
        memoryWriter.write(OpCodes.STAZ.getCode());
        memoryWriter.write((byte) 2);

        // Store MSB of -5 in memory at address $0003.
        memoryWriter.write(OpCodes.LDAI.getCode());
        memoryWriter.write((byte) 0xFF);
        memoryWriter.write(OpCodes.STAZ.getCode());
        memoryWriter.write((byte) 3);

        memoryWriter.write(OpCodes.CLC.getCode());

        // Add the LSBs
        memoryWriter.write(OpCodes.LDAZ.getCode());
        memoryWriter.write((byte) 0);
        memoryWriter.write(OpCodes.ADCZ.getCode());
        memoryWriter.write((byte) 2);
        memoryWriter.write(OpCodes.STAZ.getCode());
        memoryWriter.write((byte) 4);

        // Add the MSBs, including any carry bit from the first addition.
        memoryWriter.write(OpCodes.LDAZ.getCode());
        memoryWriter.write((byte) 1);
        memoryWriter.write(OpCodes.ADCZ.getCode());
        memoryWriter.write((byte) 3);
        memoryWriter.write(OpCodes.STAZ.getCode());
        memoryWriter.write((byte) 5);

        while (runtime.getCpu().getProgramCounter() < memoryWriter.getAddress()) {
            runtime.performSingleInstruction();
        }

        Assertions.assertEquals((byte) 0x80, runtime.getBus().read(0));
        Assertions.assertEquals((byte) 0xFF, runtime.getBus().read(1));
        Assertions.assertEquals((byte) 0xFB, runtime.getBus().read(2));
        Assertions.assertEquals((byte) 0xFF, runtime.getBus().read(3));
        Assertions.assertEquals((byte) 0x7B, runtime.getBus().read(4));
        Assertions.assertEquals((byte) 0xFF, runtime.getBus().read(5)); // unsure about this one
        Assertions.assertTrue(runtime.getCpu().isStatusCarry());
        Assertions.assertFalse(runtime.getCpu().isStatusOverflow());
    }

    // $FF80 - $0005 (-128 - +5 in decimal)
    @Test
    void testSubtractionWithBorrow() {
        // Load LSB of -128 in memory at address $0000.
        memoryWriter.write(OpCodes.LDAI.getCode());
        memoryWriter.write((byte) 0x80);
        memoryWriter.write(OpCodes.STAZ.getCode());
        memoryWriter.write((byte) 0);

        // Load MSB of -128 in memory at address $0001.
        memoryWriter.write(OpCodes.LDAI.getCode());
        memoryWriter.write((byte) 0xFF);
        memoryWriter.write(OpCodes.STAZ.getCode());
        memoryWriter.write((byte) 1);

        // Load LSB of +5 in memory at address $0002.
        memoryWriter.write(OpCodes.LDAI.getCode());
        memoryWriter.write((byte) 5);
        memoryWriter.write(OpCodes.STAZ.getCode());
        memoryWriter.write((byte) 2);

        // Load MSB of +5 in memory at address $0003.
        memoryWriter.write(OpCodes.LDAI.getCode());
        memoryWriter.write((byte) 0);
        memoryWriter.write(OpCodes.STAZ.getCode());
        memoryWriter.write((byte) 3);

        memoryWriter.write(OpCodes.SEC.getCode());

        // Add the LSBs.
        memoryWriter.write(OpCodes.LDAZ.getCode());
        memoryWriter.write((byte) 0);
        memoryWriter.write(OpCodes.SBCZ.getCode());
        memoryWriter.write((byte) 2);
        memoryWriter.write(OpCodes.STAZ.getCode());
        memoryWriter.write((byte) 4);

        // Add the MSBs, including the carry bit from the first addition.
        memoryWriter.write(OpCodes.LDAZ.getCode());
        memoryWriter.write((byte) 1);
        memoryWriter.write(OpCodes.SBCZ.getCode());
        memoryWriter.write((byte) 3);
        memoryWriter.write(OpCodes.STAZ.getCode());
        memoryWriter.write((byte) 5);

        while (runtime.getCpu().getProgramCounter() < memoryWriter.getAddress()) {
            runtime.performSingleInstruction();
        }

        Assertions.assertEquals((byte) 0x80, runtime.getBus().read(0));
        Assertions.assertEquals((byte) 0xFF, runtime.getBus().read(1));
        Assertions.assertEquals((byte) 0x05, runtime.getBus().read(2));
        Assertions.assertEquals((byte) 0x00, runtime.getBus().read(3));
        Assertions.assertEquals((byte) 0x7B, runtime.getBus().read(4));
        Assertions.assertEquals((byte) 0xFF, runtime.getBus().read(5));
        Assertions.assertFalse(runtime.getCpu().isStatusOverflow());
    }

    /**
     * The sequence Super Mario Bros uses to place the player relative to an edge of the screen.
     * Subtracting zero from the low byte must not borrow from the high byte, or the player ends up
     * a whole page to the left of where it belongs.
     */
    @Test
    void verifySubtractingZeroDoesNotBorrowFromTheHighByte() {
        memoryWriter.write(OpCodes.SEC.getCode());
        subtractImmediateFromValue((byte) 0x90, (byte) 0x00, 4);
        subtractImmediateFromValue((byte) 0x00, (byte) 0x00, 5);

        run();

        Assertions.assertEquals((byte) 0x90, runtime.getBus().read(4));
        Assertions.assertEquals((byte) 0x00, runtime.getBus().read(5));
        Assertions.assertTrue(runtime.getCpu().isStatusCarry());
    }

    @Test
    void verifyASubtractionThatNeedsNoBorrowLeavesTheCarryFlagSet() {
        memoryWriter.write(OpCodes.SEC.getCode());
        subtractImmediateFromValue((byte) 0x05, (byte) 0x05, 0);

        run();

        Assertions.assertEquals((byte) 0x00, runtime.getBus().read(0));
        Assertions.assertTrue(runtime.getCpu().isStatusCarry());
        Assertions.assertTrue(runtime.getCpu().isStatusZero());
    }

    @Test
    void verifyASubtractionThatNeedsABorrowClearsTheCarryFlag() {
        memoryWriter.write(OpCodes.SEC.getCode());
        subtractImmediateFromValue((byte) 0x04, (byte) 0x05, 0);

        run();

        Assertions.assertEquals((byte) 0xFF, runtime.getBus().read(0));
        Assertions.assertFalse(runtime.getCpu().isStatusCarry());
    }

    @Test
    void verifyAClearedCarryFlagSubtractsAnAdditionalOne() {
        memoryWriter.write(OpCodes.CLC.getCode());
        subtractImmediateFromValue((byte) 0x05, (byte) 0x04, 0);
        // the borrow out of the first subtraction feeds into the second one
        subtractImmediateFromValue((byte) 0x05, (byte) 0x04, 1);

        run();

        Assertions.assertEquals((byte) 0x00, runtime.getBus().read(0));
        Assertions.assertEquals((byte) 0x01, runtime.getBus().read(1));
        Assertions.assertTrue(runtime.getCpu().isStatusCarry());
    }

    @Test
    void verifySubtractingANegativeValueFromAPositiveOneOverflows() {
        memoryWriter.write(OpCodes.SEC.getCode());
        subtractImmediateFromValue((byte) 0x50, (byte) 0xB0, 0);

        run();

        Assertions.assertEquals((byte) 0xA0, runtime.getBus().read(0));
        Assertions.assertTrue(runtime.getCpu().isStatusOverflow());
        Assertions.assertFalse(runtime.getCpu().isStatusCarry());
    }

    private void subtractImmediateFromValue(byte value, byte subtrahend, int targetAddress) {
        memoryWriter.write(OpCodes.LDAI.getCode());
        memoryWriter.write(value);
        memoryWriter.write(OpCodes.SBCI.getCode());
        memoryWriter.write(subtrahend);
        memoryWriter.write(OpCodes.STAZ.getCode());
        memoryWriter.write((byte) targetAddress);
    }

    private void run() {
        while (runtime.getCpu().getProgramCounter() < memoryWriter.getAddress()) {
            runtime.performSingleInstruction();
        }
    }
}
