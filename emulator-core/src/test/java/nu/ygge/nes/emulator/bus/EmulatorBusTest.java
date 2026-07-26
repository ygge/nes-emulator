package nu.ygge.nes.emulator.bus;

import nu.ygge.nes.emulator.input.Button;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class EmulatorBusTest {

    private static final int OAM_DMA_CYCLES = 513;

    @Test
    void verifyASingleProgramRomBankIsMirroredIntoTheUpperHalf() {
        var prgRom = new byte[16384];
        prgRom[0] = 0x42;
        prgRom[16383] = 0x17;
        var bus = new EmulatorBus(prgRom);

        Assertions.assertEquals(0x42, bus.read(0x8000));
        Assertions.assertEquals(0x42, bus.read(0xc000));
        Assertions.assertEquals(0x17, bus.read(0xffff));
    }

    @Test
    void verifyOamDmaCopiesAWholeCpuPageAndStallsTheCpu() {
        var bus = new EmulatorBus(new byte[32768]);
        for (int i = 0; i < 256; ++i) {
            bus.write(0x200 + i, (byte) i);
        }

        bus.write(0x4014, (byte) 0x02);

        Assertions.assertEquals(OAM_DMA_CYCLES, bus.consumeStallCycles());
        Assertions.assertEquals(0, bus.consumeStallCycles());
        bus.write(0x2003, (byte) 0x10);
        Assertions.assertEquals(0x10, bus.read(0x2004));
    }

    @Test
    void verifyPpuRegistersAreMirroredThroughoutTheirRange() {
        var bus = new EmulatorBus(new byte[32768]);

        bus.write(0x3ffe, (byte) 0x21); // a mirror of the address register
        bus.write(0x3ffe, (byte) 0x00);
        bus.write(0x3fff, (byte) 0x42); // a mirror of the data register

        bus.write(0x2006, (byte) 0x21);
        bus.write(0x2006, (byte) 0x00);
        bus.read(0x2007); // reads outside the palette are delayed by one read
        Assertions.assertEquals(0x42, bus.read(0x2007));
    }

    @Test
    void verifyTheControllerIsReachableThroughItsPort() {
        var bus = new EmulatorBus(new byte[32768]);
        bus.getController().setPressed(Button.START, true);

        bus.write(0x4016, (byte) 1);
        bus.write(0x4016, (byte) 0);

        Assertions.assertEquals(0, bus.read(0x4016)); // a
        Assertions.assertEquals(0, bus.read(0x4016)); // b
        Assertions.assertEquals(0, bus.read(0x4016)); // select
        Assertions.assertEquals(1, bus.read(0x4016)); // start
    }

    @Test
    void verifyReadingAWriteOnlyRegisterDoesNotFail() {
        var bus = new EmulatorBus(new byte[32768]);

        Assertions.assertEquals(0, bus.read(0x2000));
        Assertions.assertEquals(0, bus.read(0x2006));
    }
}
