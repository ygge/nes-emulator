package nu.ygge.nes.emulator.mapper;

import nu.ygge.nes.emulator.ppu.Mirroring;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class Mmc1MapperTest {

    private static final int PRG_BANK_SIZE = 0x4000;
    private static final int BANKS = 4;

    private Mmc1Mapper mapper;

    @BeforeEach
    void setUp() {
        var prgRom = new byte[BANKS * PRG_BANK_SIZE];
        // stamp each bank with its own number so we can tell which one is mapped in
        for (int bank = 0; bank < BANKS; ++bank) {
            prgRom[bank * PRG_BANK_SIZE] = (byte) bank;
        }
        mapper = new Mmc1Mapper(prgRom, new byte[8192]);
    }

    @Test
    void verifyTheLastBankIsFixedHighAtPowerOnSoTheResetVectorIsReachable() {
        Assertions.assertEquals(BANKS - 1, mapper.cpuRead(0xc000));
    }

    @Test
    void verifyTheSwitchableBankAppearsLow() {
        Assertions.assertEquals(0, mapper.cpuRead(0x8000));

        writeSerial(0xe000, 2); // the program bank register

        Assertions.assertEquals(2, mapper.cpuRead(0x8000));
        // the high bank stays fixed on the last one
        Assertions.assertEquals(BANKS - 1, mapper.cpuRead(0xc000));
    }

    @Test
    void verifyTheControlRegisterSelectsTheMirroring() {
        writeSerial(0x8000, 0x0e); // mirroring bits set to two

        Assertions.assertEquals(Mirroring.VERTICAL, mapper.getMirroring());

        writeSerial(0x8000, 0x0f); // mirroring bits set to three

        Assertions.assertEquals(Mirroring.HORIZONTAL, mapper.getMirroring());
    }

    /**
     * The MMC1 receives a value one bit at a time, least significant bit first, over five writes.
     */
    private void writeSerial(int address, int value) {
        for (int i = 0; i < 5; ++i) {
            mapper.cpuWrite(address, (byte) ((value >> i) & 1));
        }
    }
}
