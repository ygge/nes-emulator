package nu.ygge.nes.emulator;

import nu.ygge.nes.emulator.cpu.OpCodes;
import nu.ygge.nes.emulator.cpu.util.MemoryWriter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NESRuntime_JMPTest {

    private NESRuntime runtime;
    private MemoryWriter memoryWriter;

    @BeforeEach
    void setUp() {
        runtime = new NESRuntime();
        memoryWriter = new MemoryWriter(runtime.getMemory(), 0);
    }

    @Test
    void verifyJumpWithAbsolutIndirect() {
        memoryWriter.write(OpCodes.JMPAI.getCode());
        memoryWriter.write((byte) 0x12);
        memoryWriter.write((byte) 0x34);

        runtime.getMemory().write(0x1234, (byte) 0x78);
        runtime.getMemory().write(0x1235, (byte) 0x56);

        runtime.performSingleInstruction();

        Assertions.assertEquals(0x5678, runtime.getCpu().getProgramCounter());
    }
}
