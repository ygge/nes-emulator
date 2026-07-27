package nu.ygge.nes.emulator.mapper;

import nu.ygge.nes.emulator.ppu.Mirroring;
import nu.ygge.nes.emulator.state.StateReader;
import nu.ygge.nes.emulator.state.StateWriter;

/**
 * The simplest cartridge (iNES mapper 0): the program ROM is fixed in place with no banking, so a
 * sixteen kilobyte cartridge is mirrored into both halves of the address space and a thirty two
 * kilobyte one fills it exactly. Character memory is likewise a single fixed bank.
 */
public class NromMapper implements Mapper {

    private static final int PRG_ROM_START = 0x8000;
    private static final int PRG_RAM_START = 0x6000;
    private static final int PRG_RAM_SIZE = 0x2000;
    private static final int CHR_SIZE = 0x2000;

    private final byte[] prgRom;
    private final byte[] prgRam = new byte[PRG_RAM_SIZE];
    private final byte[] chr;
    private final boolean chrIsRam;
    private final Mirroring mirroring;

    public NromMapper(byte[] prgRom, byte[] chrRom, Mirroring mirroring) {
        this.prgRom = prgRom.length == 0 ? new byte[PRG_ROM_START] : prgRom;
        this.chrIsRam = chrRom.length == 0;
        this.chr = chrIsRam ? new byte[CHR_SIZE] : chrRom;
        this.mirroring = mirroring;
    }

    @Override
    public byte cpuRead(int address) {
        if (address >= PRG_ROM_START) {
            return prgRom[(address - PRG_ROM_START) % prgRom.length];
        }
        if (address >= PRG_RAM_START) {
            return prgRam[address - PRG_RAM_START];
        }
        return 0;
    }

    @Override
    public void cpuWrite(int address, byte data) {
        if (address >= PRG_RAM_START && address < PRG_ROM_START) {
            prgRam[address - PRG_RAM_START] = data;
        }
    }

    @Override
    public byte ppuRead(int address) {
        return chr[address & (CHR_SIZE - 1)];
    }

    @Override
    public void ppuWrite(int address, byte data) {
        if (chrIsRam) {
            chr[address & (CHR_SIZE - 1)] = data;
        }
    }

    @Override
    public Mirroring getMirroring() {
        return mirroring;
    }

    @Override
    public void saveState(StateWriter writer) {
        writer.writeByte(MapperType.NROM);
        writer.writeBytes(prgRam);
        if (chrIsRam) {
            writer.writeBytes(chr);
        }
    }

    @Override
    public void loadState(StateReader reader) {
        MapperType.verify(reader.readUnsignedByte(), MapperType.NROM);
        reader.readBytes(prgRam);
        if (chrIsRam) {
            reader.readBytes(chr);
        }
    }
}
