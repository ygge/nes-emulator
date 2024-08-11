package nu.ygge.nes.emulator;

import lombok.Getter;
import nu.ygge.nes.emulator.exception.LoadNESFileException;
import nu.ygge.nes.emulator.ppu.Mirroring;

@Getter
public final class NesFileLoader {

    private final byte[] prgRom, chrRom;
    private final Mirroring mirroring;

    public NesFileLoader(byte[] data) {
        if (data == null || data.length < 16) {
            throw new LoadNESFileException("File is null or too small");
        }
        if (data[0] != 0x4E || data[1] != 0x45 || data[2] != 0x53 || data[3] != 0x1A) {
            throw new LoadNESFileException("Header has incorrect start");
        }
        int prgRomSize = data[4];
        int chrRomSize = data[5];
        int controlByte = data[6];

        prgRom = new byte[16384 * prgRomSize];
        chrRom = new byte[8192 * chrRomSize];
        mirroring = (controlByte & 1) == 1 ? Mirroring.VERTICAL : Mirroring.HORIZONTAL;

        System.arraycopy(data, 16, prgRom, 0, prgRom.length);
        System.arraycopy(data, 16 + prgRom.length, chrRom, 0, chrRom.length);
    }
}
