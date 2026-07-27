package nu.ygge.nes.emulator.mapper;

import nu.ygge.nes.emulator.ppu.Mirroring;
import nu.ygge.nes.emulator.state.StateReader;
import nu.ygge.nes.emulator.state.StateWriter;

/**
 * The Nintendo MMC1 (iNES mapper 1), used by a large slice of the library including the likes of
 * Teenage Mutant Ninja Turtles and The Legend of Zelda. The CPU configures it by writing a value
 * one bit at a time into a serial shift register; once five bits have arrived they land in one of
 * four internal registers selected by the target address, controlling mirroring, program banking
 * and character banking.
 */
public class Mmc1Mapper implements Mapper {

    private static final int PRG_ROM_START = 0x8000;
    private static final int PRG_RAM_START = 0x6000;
    private static final int PRG_RAM_SIZE = 0x2000;
    private static final int PRG_BANK_SIZE = 0x4000;
    private static final int CHR_BANK_SIZE = 0x1000;
    private static final int SHIFT_RESET = 0x10;

    private final byte[] prgRom;
    private final byte[] prgRam = new byte[PRG_RAM_SIZE];
    private final byte[] chr;
    private final boolean chrIsRam;
    private final int prgBankCount;
    private final int chrBankCount;

    private int shiftRegister = SHIFT_RESET;
    // power on with the last program bank fixed at $C000 so the reset vector is reachable
    private int control = 0x0c;
    private int chrBank0;
    private int chrBank1;
    private int prgBank;

    public Mmc1Mapper(byte[] prgRom, byte[] chrRom) {
        this.prgRom = prgRom;
        this.chrIsRam = chrRom.length == 0;
        this.chr = chrIsRam ? new byte[2 * CHR_BANK_SIZE] : chrRom;
        this.prgBankCount = Math.max(1, prgRom.length / PRG_BANK_SIZE);
        this.chrBankCount = Math.max(1, this.chr.length / CHR_BANK_SIZE);
    }

    @Override
    public byte cpuRead(int address) {
        if (address >= PRG_ROM_START) {
            return prgRom[prgRomIndex(address)];
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
        } else if (address >= PRG_ROM_START) {
            writeRegister(address, data);
        }
    }

    @Override
    public byte ppuRead(int address) {
        return chr[chrIndex(address)];
    }

    @Override
    public void ppuWrite(int address, byte data) {
        if (chrIsRam) {
            chr[chrIndex(address)] = data;
        }
    }

    @Override
    public Mirroring getMirroring() {
        return switch (control & 0x03) {
            case 0 -> Mirroring.SINGLE_SCREEN_LOWER;
            case 1 -> Mirroring.SINGLE_SCREEN_UPPER;
            case 2 -> Mirroring.VERTICAL;
            default -> Mirroring.HORIZONTAL;
        };
    }

    @Override
    public void saveState(StateWriter writer) {
        writer.writeByte(MapperType.MMC1);
        writer.writeBytes(prgRam);
        if (chrIsRam) {
            writer.writeBytes(chr);
        }
        writer.writeInt(shiftRegister);
        writer.writeInt(control);
        writer.writeInt(chrBank0);
        writer.writeInt(chrBank1);
        writer.writeInt(prgBank);
    }

    @Override
    public void loadState(StateReader reader) {
        MapperType.verify(reader.readUnsignedByte(), MapperType.MMC1);
        reader.readBytes(prgRam);
        if (chrIsRam) {
            reader.readBytes(chr);
        }
        shiftRegister = reader.readInt();
        control = reader.readInt();
        chrBank0 = reader.readInt();
        chrBank1 = reader.readInt();
        prgBank = reader.readInt();
    }

    private void writeRegister(int address, byte data) {
        // a write with the top bit set resets the shift register and fixes the last program bank
        if ((data & 0x80) != 0) {
            shiftRegister = SHIFT_RESET;
            control |= 0x0c;
            return;
        }
        var complete = (shiftRegister & 1) != 0;
        shiftRegister = (shiftRegister >> 1) | ((data & 1) << 4);
        if (complete) {
            var value = shiftRegister & 0x1f;
            switch ((address >> 13) & 0x03) {
                case 0 -> control = value;
                case 1 -> chrBank0 = value;
                case 2 -> chrBank1 = value;
                default -> prgBank = value & 0x0f;
            }
            shiftRegister = SHIFT_RESET;
        }
    }

    private int prgRomIndex(int address) {
        var mode = (control >> 2) & 0x03;
        var offset = address & (PRG_BANK_SIZE - 1);
        int bank;
        if (mode == 0 || mode == 1) {
            // one large thirty two kilobyte bank, ignoring the low bank bit
            bank = (prgBank & 0x0e) | ((address >= 0xc000) ? 1 : 0);
        } else if (mode == 2) {
            // the first bank is fixed low, the selected bank sits high
            bank = (address >= 0xc000) ? prgBank : 0;
        } else {
            // the selected bank sits low, the last bank is fixed high
            bank = (address >= 0xc000) ? prgBankCount - 1 : prgBank;
        }
        return (bank % prgBankCount) * PRG_BANK_SIZE + offset;
    }

    private int chrIndex(int address) {
        var offset = address & (CHR_BANK_SIZE - 1);
        int bank;
        if ((control & 0x10) == 0) {
            // a single eight kilobyte bank, ignoring the low bank bit
            bank = (chrBank0 & 0x1e) | ((address >= CHR_BANK_SIZE) ? 1 : 0);
        } else {
            bank = (address >= CHR_BANK_SIZE) ? chrBank1 : chrBank0;
        }
        return (bank % chrBankCount) * CHR_BANK_SIZE + offset;
    }
}
