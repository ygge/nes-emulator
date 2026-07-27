package nu.ygge.nes.emulator.mapper;

import nu.ygge.nes.emulator.NesFileLoader;
import nu.ygge.nes.emulator.exception.LoadNESFileException;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class MapperFactoryTest {

    private static final int HEADER_SIZE = 16;
    private static final int PRG_ROM_BANK_SIZE = 16384;
    private static final int CHR_ROM_BANK_SIZE = 8192;

    @Test
    void verifyMapperZeroBuildsAnNromCartridge() {
        Assertions.assertInstanceOf(NromMapper.class, MapperFactory.create(loaderForMapper(0)));
    }

    @Test
    void verifyMapperOneBuildsAnMmc1Cartridge() {
        Assertions.assertInstanceOf(Mmc1Mapper.class, MapperFactory.create(loaderForMapper(1)));
    }

    @Test
    void verifyMapperFourBuildsAnMmc3Cartridge() {
        Assertions.assertInstanceOf(Mmc3Mapper.class, MapperFactory.create(loaderForMapper(4)));
    }

    @Test
    void verifyAnUnsupportedMapperGivesAHelpfulError() {
        var loader = loaderForMapper(2);

        var exception = Assertions.assertThrows(LoadNESFileException.class, () -> MapperFactory.create(loader));
        Assertions.assertTrue(exception.getMessage().contains("Unsupported mapper: 2"));
    }

    @Test
    void verifyTheMapperNumberIsSplitAcrossBothControlBytes() {
        // low nibble in byte six, high nibble in byte seven: 0x2 | 0x10 = mapper 18
        var data = iNesFile();
        data[6] = 0x20;
        data[7] = 0x10;

        Assertions.assertEquals(18, new NesFileLoader(data).getMapper());
    }

    private NesFileLoader loaderForMapper(int mapper) {
        var data = iNesFile();
        data[6] = (byte) ((mapper & 0x0f) << 4);
        data[7] = (byte) (mapper & 0xf0);
        return new NesFileLoader(data);
    }

    private byte[] iNesFile() {
        var data = new byte[HEADER_SIZE + PRG_ROM_BANK_SIZE + CHR_ROM_BANK_SIZE];
        data[0] = 0x4e; // N
        data[1] = 0x45; // E
        data[2] = 0x53; // S
        data[3] = 0x1a;
        data[4] = 1; // one program ROM bank
        data[5] = 1; // one character ROM bank
        return data;
    }
}
