package nu.ygge.nes.emulator;

import lombok.Getter;
import nu.ygge.nes.emulator.exception.LoadNESFileException;
import nu.ygge.nes.emulator.ppu.Mirroring;

@Getter
public final class NesFileLoader {

    private static final int HEADER_SIZE = 16;
    private static final int TRAINER_SIZE = 512;
    private static final int PRG_ROM_BANK_SIZE = 16384;
    private static final int CHR_ROM_BANK_SIZE = 8192;

    private final byte[] prgRom, chrRom;
    private final Mirroring mirroring;

    public NesFileLoader(byte[] data) {
        if (data == null || data.length < HEADER_SIZE) {
            throw new LoadNESFileException("File is null or too small");
        }
        if (data[0] != 0x4E || data[1] != 0x45 || data[2] != 0x53 || data[3] != 0x1A) {
            throw new LoadNESFileException("Header has incorrect start");
        }
        int prgRomSize = data[4] & 0xff;
        int chrRomSize = data[5] & 0xff;
        int controlByte = data[6] & 0xff;

        prgRom = new byte[PRG_ROM_BANK_SIZE * prgRomSize];
        chrRom = new byte[CHR_ROM_BANK_SIZE * chrRomSize];
        mirroring = (controlByte & 1) == 1 ? Mirroring.VERTICAL : Mirroring.HORIZONTAL;

        var offset = HEADER_SIZE + ((controlByte & 0b100) != 0 ? TRAINER_SIZE : 0);
        if (data.length < offset + prgRom.length + chrRom.length) {
            throw new LoadNESFileException("File is smaller than its header claims");
        }
        System.arraycopy(data, offset, prgRom, 0, prgRom.length);
        System.arraycopy(data, offset + prgRom.length, chrRom, 0, chrRom.length);
    }
}
