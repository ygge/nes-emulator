package nu.ygge.nes.emulator;

import nu.ygge.nes.emulator.cpu.InterruptAddress;
import nu.ygge.nes.emulator.cpu.OpCodes;
import nu.ygge.nes.emulator.util.MemoryWriter;
import nu.ygge.nes.emulator.util.TestBus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class NESRuntime_JumpTest {

    private NESRuntime runtime;
    private MemoryWriter memoryWriter;

    @BeforeEach
    void setUp() {
        runtime = new NESRuntime();
        runtime.loadGame(new TestBus());
        runtime.getCpu().setStackPointer((byte) 0xFF);
        memoryWriter = new MemoryWriter(runtime.getBus(), 0);
    }

    @Test
    void verifyJumpWithAbsolutIndirect() {
        memoryWriter.write(OpCodes.JMPAI.getCode());
        memoryWriter.write((byte) 0x34);
        memoryWriter.write((byte) 0x12);

        runtime.getBus().write(0x1234, (byte) 0x78);
        runtime.getBus().write(0x1235, (byte) 0x56);

        runtime.performSingleInstruction();

        Assertions.assertEquals(0x5678, runtime.getCpu().getProgramCounter());
    }

    @Test
    void verifySubroutineCallAndReturn() {
        memoryWriter.write(OpCodes.JSRA.getCode());
        memoryWriter.write((byte) 0x34);
        memoryWriter.write((byte) 0x12);

        runtime.getBus().write(0x1234, OpCodes.RTS.getCode());

        runtime.performSingleInstruction();
        runtime.performSingleInstruction();

        Assertions.assertEquals(3, runtime.getCpu().getProgramCounter());
    }

    @Test
    void verifyForceBreakAndReturn() {
        memoryWriter.write(OpCodes.BRK.getCode());

        runtime.getBus().write(InterruptAddress.BREAK.getStartAddress(), (byte) 0x34);
        runtime.getBus().write(InterruptAddress.BREAK.getStartAddress() + 1, (byte) 0x12);
        runtime.getBus().write(0x1234, OpCodes.RTI.getCode());

        runtime.performSingleInstruction();
        Assertions.assertTrue(runtime.getCpu().isStatusBreak());

        runtime.performSingleInstruction();

        Assertions.assertEquals(2, runtime.getCpu().getProgramCounter());
        Assertions.assertFalse(runtime.getCpu().isStatusBreak());
    }
}
