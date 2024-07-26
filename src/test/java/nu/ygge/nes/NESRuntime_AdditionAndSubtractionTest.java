package nu.ygge.nes;

import nu.ygge.nes.cpu.OpCodes;
import nu.ygge.nes.cpu.util.MemoryWriter;
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
        memoryWriter = new MemoryWriter(runtime.getMemory(), pc);
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

        Assertions.assertEquals((byte) 0xFE, runtime.getMemory().read(0));
        Assertions.assertEquals((byte) 0x00, runtime.getMemory().read(1));
        Assertions.assertEquals((byte) 0x03, runtime.getMemory().read(2));
        Assertions.assertEquals((byte) 0x00, runtime.getMemory().read(3));
        Assertions.assertEquals((byte) 0x01, runtime.getMemory().read(4));
        Assertions.assertEquals((byte) 0x01, runtime.getMemory().read(5));
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

        Assertions.assertEquals((byte) 0x80, runtime.getMemory().read(0));
        Assertions.assertEquals((byte) 0xFF, runtime.getMemory().read(1));
        Assertions.assertEquals((byte) 0xFB, runtime.getMemory().read(2));
        Assertions.assertEquals((byte) 0xFF, runtime.getMemory().read(3));
        Assertions.assertEquals((byte) 0x7B, runtime.getMemory().read(4));
        Assertions.assertEquals((byte) 0xFE, runtime.getMemory().read(5));
        Assertions.assertFalse(runtime.getCpu().isStatusCarry());
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

        Assertions.assertEquals((byte) 0x80, runtime.getMemory().read(0));
        Assertions.assertEquals((byte) 0xFF, runtime.getMemory().read(1));
        Assertions.assertEquals((byte) 0x05, runtime.getMemory().read(2));
        Assertions.assertEquals((byte) 0x00, runtime.getMemory().read(3));
        Assertions.assertEquals((byte) 0x7B, runtime.getMemory().read(4));
        Assertions.assertEquals((byte) 0xFF, runtime.getMemory().read(5));
        Assertions.assertFalse(runtime.getCpu().isStatusOverflow());
    }
}
