package nu.ygge.nes.emulator.mapper;

import nu.ygge.nes.emulator.NesFileLoader;
import nu.ygge.nes.emulator.exception.LoadNESFileException;

public final class MapperFactory {

    private MapperFactory() {
    }

    public static Mapper create(NesFileLoader file) {
        var prgRom = file.getPrgRom();
        var chrRom = file.getChrRom();
        var mirroring = file.getMirroring();
        return switch (file.getMapper()) {
            case 0 -> new NromMapper(prgRom, chrRom, mirroring);
            case 1 -> new Mmc1Mapper(prgRom, chrRom);
            case 4 -> new Mmc3Mapper(prgRom, chrRom, mirroring);
            default -> throw new LoadNESFileException(String.format(
                    "Unsupported mapper: %d. Only NROM (0), MMC1 (1) and MMC3 (4) are implemented.",
                    file.getMapper()));
        };
    }
}
