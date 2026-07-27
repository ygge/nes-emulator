package nu.ygge.nes.emulator;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class NESRuntimeSaveStateTest {

    private static final int HEADER_SIZE = 16;
    private static final int PRG_ROM_SIZE = 16384;

    private NESRuntime runtime;

    @BeforeEach
    void setUp() {
        runtime = new NESRuntime();
        runtime.loadGame(testRom());
        runtime.reset();
    }

    @Test
    void verifyASnapshotRestoresTheMachineExactly() {
        runInstructions(200);
        var snapshot = runtime.saveState();

        runInstructions(200);
        var divergedSnapshot = runtime.saveState();
        // running on has to actually change something, or the test proves nothing
        Assertions.assertFalse(java.util.Arrays.equals(snapshot, divergedSnapshot));

        runtime.loadState(snapshot);

        Assertions.assertArrayEquals(snapshot, runtime.saveState());
    }

    @Test
    void verifyTheMachineKeepsRunningAfterARestore() {
        runInstructions(100);
        var snapshot = runtime.saveState();
        runInstructions(100);

        runtime.loadState(snapshot);

        Assertions.assertDoesNotThrow(() -> runInstructions(100));
    }

    @Test
    void verifyGarbageIsRejected() {
        Assertions.assertThrows(RuntimeException.class, () -> runtime.loadState(new byte[HEADER_SIZE]));
    }

    private void runInstructions(int count) {
        for (int i = 0; i < count; ++i) {
            runtime.performSingleInstruction();
        }
    }

    /**
     * A tiny NROM cartridge whose reset vector points at a short loop that keeps touching memory and
     * the index registers, so that running it visibly changes the machine state.
     */
    private static byte[] testRom() {
        var program = new byte[]{
                (byte) 0xa9, 0x05, // LDA #$05
                (byte) 0x85, 0x20, // STA $20
                (byte) 0xe8,       // INX
                (byte) 0xc8,       // INY
                (byte) 0x4c, 0x00, (byte) 0x80 // JMP $8000
        };
        var data = new byte[HEADER_SIZE + PRG_ROM_SIZE];
        data[0] = 0x4e; // N
        data[1] = 0x45; // E
        data[2] = 0x53; // S
        data[3] = 0x1a;
        data[4] = 1; // one program ROM bank, no character ROM
        System.arraycopy(program, 0, data, HEADER_SIZE, program.length);
        // the reset vector at $FFFC/$FFFD points to $8000
        data[HEADER_SIZE + 0x3ffc] = 0x00;
        data[HEADER_SIZE + 0x3ffd] = (byte) 0x80;
        return data;
    }
}
