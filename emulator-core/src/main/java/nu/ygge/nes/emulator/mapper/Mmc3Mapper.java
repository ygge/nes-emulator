package nu.ygge.nes.emulator.mapper;

import nu.ygge.nes.emulator.ppu.Mirroring;

/**
 * The Nintendo MMC3 (iNES mapper 4), used by many later games such as Super Mario Bros. 3. It banks
 * program memory in eight kilobyte slices and character memory in one kilobyte slices, and crucially
 * carries a scanline counter that raises an interrupt at a chosen point down the screen. That is how
 * these games split the display, for instance to hold a status bar still while the rest scrolls.
 */
public class Mmc3Mapper implements Mapper {

    private static final int PRG_ROM_START = 0x8000;
    private static final int PRG_RAM_START = 0x6000;
    private static final int PRG_RAM_SIZE = 0x2000;
    private static final int PRG_BANK_SIZE = 0x2000;
    private static final int CHR_BANK_SIZE = 0x400;

    private final byte[] prgRom;
    private final byte[] prgRam = new byte[PRG_RAM_SIZE];
    private final byte[] chr;
    private final boolean chrIsRam;
    private final int prgBankCount;
    private final int chrBankCount;

    private final int[] bankRegisters = new int[8];
    private int bankSelect;
    private Mirroring mirroring;

    private int irqLatch;
    private int irqCounter;
    private boolean irqReloadPending;
    private boolean irqEnabled;
    private boolean irqAsserted;

    public Mmc3Mapper(byte[] prgRom, byte[] chrRom, Mirroring mirroring) {
        this.prgRom = prgRom;
        this.chrIsRam = chrRom.length == 0;
        this.chr = chrIsRam ? new byte[8 * CHR_BANK_SIZE] : chrRom;
        this.prgBankCount = Math.max(1, prgRom.length / PRG_BANK_SIZE);
        this.chrBankCount = Math.max(1, this.chr.length / CHR_BANK_SIZE);
        this.mirroring = mirroring;
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
        return mirroring;
    }

    @Override
    public boolean isIrqAsserted() {
        return irqAsserted;
    }

    @Override
    public void onScanline() {
        if (irqCounter == 0 || irqReloadPending) {
            irqCounter = irqLatch;
            irqReloadPending = false;
        } else {
            --irqCounter;
        }
        if (irqCounter == 0 && irqEnabled) {
            irqAsserted = true;
        }
    }

    private void writeRegister(int address, byte data) {
        var value = data & 0xff;
        var even = (address & 1) == 0;
        if (address < 0xa000) {
            if (even) {
                bankSelect = value;
            } else {
                bankRegisters[bankSelect & 0x07] = value;
            }
        } else if (address < 0xc000) {
            if (even) {
                mirroring = (value & 1) == 0 ? Mirroring.VERTICAL : Mirroring.HORIZONTAL;
            }
            // the odd address only toggles program RAM protection, which we do not enforce
        } else if (address < 0xe000) {
            if (even) {
                irqLatch = value;
            } else {
                irqCounter = 0;
                irqReloadPending = true;
            }
        } else {
            if (even) {
                irqEnabled = false;
                irqAsserted = false;
            } else {
                irqEnabled = true;
            }
        }
    }

    private int prgRomIndex(int address) {
        var slot = (address - PRG_ROM_START) / PRG_BANK_SIZE;
        var last = prgBankCount - 1;
        var secondLast = prgBankCount - 2;
        int bank;
        if ((bankSelect & 0x40) == 0) {
            bank = switch (slot) {
                case 0 -> bankRegisters[6];
                case 1 -> bankRegisters[7];
                case 2 -> secondLast;
                default -> last;
            };
        } else {
            bank = switch (slot) {
                case 0 -> secondLast;
                case 1 -> bankRegisters[7];
                case 2 -> bankRegisters[6];
                default -> last;
            };
        }
        return (Math.floorMod(bank, prgBankCount)) * PRG_BANK_SIZE + (address & (PRG_BANK_SIZE - 1));
    }

    private int chrIndex(int address) {
        var slot = address / CHR_BANK_SIZE;
        // the high control bit swaps the two and four kilobyte halves of the pattern table
        if ((bankSelect & 0x80) != 0) {
            slot ^= 0x04;
        }
        var bank = switch (slot) {
            case 0 -> bankRegisters[0] & 0xfe;
            case 1 -> bankRegisters[0] | 0x01;
            case 2 -> bankRegisters[1] & 0xfe;
            case 3 -> bankRegisters[1] | 0x01;
            case 4 -> bankRegisters[2];
            case 5 -> bankRegisters[3];
            case 6 -> bankRegisters[4];
            default -> bankRegisters[5];
        };
        return (Math.floorMod(bank, chrBankCount)) * CHR_BANK_SIZE + (address & (CHR_BANK_SIZE - 1));
    }
}
