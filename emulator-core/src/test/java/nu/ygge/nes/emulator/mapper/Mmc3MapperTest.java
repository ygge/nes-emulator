package nu.ygge.nes.emulator.mapper;

import nu.ygge.nes.emulator.ppu.Mirroring;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Mmc3MapperTest {

    private static final int PRG_BANK_SIZE = 0x2000;
    private static final int BANKS = 8;

    private Mmc3Mapper mapper;

    @BeforeEach
    void setUp() {
        var prgRom = new byte[BANKS * PRG_BANK_SIZE];
        for (int bank = 0; bank < BANKS; ++bank) {
            prgRom[bank * PRG_BANK_SIZE] = (byte) bank;
        }
        mapper = new Mmc3Mapper(prgRom, new byte[8192], Mirroring.VERTICAL);
    }

    @Test
    void verifyTheLastTwoBanksAreFixedHigh() {
        Assertions.assertEquals(BANKS - 1, mapper.cpuRead(0xe000));
        Assertions.assertEquals(BANKS - 2, mapper.cpuRead(0xc000));
    }

    @Test
    void verifyTheProgramBankRegistersSelectTheLowBanks() {
        selectBank(6, 2); // register six drives $8000 in the default mode
        selectBank(7, 3); // register seven drives $A000

        Assertions.assertEquals(2, mapper.cpuRead(0x8000));
        Assertions.assertEquals(3, mapper.cpuRead(0xa000));
    }

    @Test
    void verifyTheScanlineCounterRaisesAndAcknowledgesAnInterrupt() {
        mapper.cpuWrite(0xc000, (byte) 3); // interrupt latch
        mapper.cpuWrite(0xc001, (byte) 0); // force a reload on the next scanline
        mapper.cpuWrite(0xe001, (byte) 0); // enable the interrupt

        mapper.onScanline(); // reloads to three
        mapper.onScanline(); // two
        mapper.onScanline(); // one
        Assertions.assertFalse(mapper.isIrqAsserted());

        mapper.onScanline(); // zero, which fires
        Assertions.assertTrue(mapper.isIrqAsserted());

        mapper.cpuWrite(0xe000, (byte) 0); // acknowledging disables and clears it
        Assertions.assertFalse(mapper.isIrqAsserted());
    }

    @Test
    void verifyTheMirroringCanBeSwitched() {
        mapper.cpuWrite(0xa000, (byte) 0);
        Assertions.assertEquals(Mirroring.VERTICAL, mapper.getMirroring());

        mapper.cpuWrite(0xa000, (byte) 1);
        Assertions.assertEquals(Mirroring.HORIZONTAL, mapper.getMirroring());
    }

    private void selectBank(int register, int bank) {
        mapper.cpuWrite(0x8000, (byte) register); // choose which bank register to update
        mapper.cpuWrite(0x8001, (byte) bank);     // and give it a value
    }
}
