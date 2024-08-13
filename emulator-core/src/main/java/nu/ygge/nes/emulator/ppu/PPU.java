package nu.ygge.nes.emulator.ppu;

import lombok.Getter;
import nu.ygge.nes.emulator.bus.PPUTickResult;
import nu.ygge.nes.emulator.exception.NESException;

public class PPU {

    private static final String[] COLOR_PALETTE = {
            "626262", "002E98", "0C11C2", "3B00C2", "650098", "7D004E", "7D0000", "651900",
            "3B3600", "0C4F00", "005B00", "005900", "00494E", "000000", "000000", "000000",
            "ABABAB", "0064F4", "353CFF", "761BFF", "AE0AF4", "CF0C8F", "CF231C", "AE4700",
            "766F00", "359000", "00A100", "009E1C", "00888F", "000000", "000000", "000000",
            "FFFFFF", "4AB5FF", "858CFF", "C86AFF", "FF58FF", "FF5BE2", "FF726A", "FF9702",
            "C8C100", "85E300", "4AF502", "29F26A", "29DBE2", "4E4E4E", "000000", "000000",
            "FFFFFF", "B6E1FF", "CED1FF", "E9C3FF", "FFBCFF", "FFBDF4", "FFC6C3", "FFD59A",
            "E9E681", "CEF481", "B6FB9A", "A9FAC3", "A9F0F4", "B8B8B8", "000000", "000000"
    };

    @Getter
    private AddressRegister addressRegister;
    private ControlRegister controlRegister;
    private MaskRegister maskRegister;
    private StatusRegister statusRegister;
    private byte[] paletteTable, vram, oamData, chrRom;
    private Mirroring mirroring;
    private byte dataBuffer;
    private short oamAddress, scanline;
    private int cycles;
    private boolean nmiInterrupt;

    public void reset(byte[] chrRom, Mirroring mirroring) {
        addressRegister = new AddressRegister();
        controlRegister = new ControlRegister();
        maskRegister = new MaskRegister();
        statusRegister = new StatusRegister();
        paletteTable = new byte[32];
        vram = new byte[2048];
        oamData = new byte[256];
        this.chrRom = chrRom;
        this.mirroring = mirroring;
    }

    public void writeToAddressRegister(byte value) {
        addressRegister.write(value);
    }

    public void writeToControlRegister(byte value) {
        var before = controlRegister.canGenerateNMI();
        controlRegister.update(value);
        if (!before && controlRegister.canGenerateNMI() && statusRegister.isInVBlankStatus()) {
            nmiInterrupt = true;
        }
    }

    public void writeToMaskRegister(byte value) {
        maskRegister.update(value);
    }

    public byte readStatus() {
        var data = statusRegister.getSnapshot();
        statusRegister.resetVBlankStatus();
        addressRegister.resetLatch();
        return data;
    }

    public void writeToOamAddress(byte value) {
        oamAddress = value;
    }

    public void writeToOamData(byte value) {
        oamData[oamAddress] = value;
        if (++oamAddress == 256) {
            oamAddress = 0;
        }
    }

    public void incrementVramAddress() {
        addressRegister.add(controlRegister.getVramAddressIncrement());
    }

    public byte read() {
        var address = addressRegister.get();
        incrementVramAddress();
        if (address <= 0x1fff) {
            // read from CHR ROM, with delay
            var data = dataBuffer;
            dataBuffer = chrRom[address];
            return data;
        } else if (address <= 0x2fff) {
            // read from RAM, with delay
            var data = dataBuffer;
            dataBuffer = vram[mirrorVramAddress(address)];
            return data;
        } else if (address <= 0x3eff) {
            throw new NESException(String.format("Address %d not expected to be read", address));
        } else if (address <= 0x3fff) {
            return paletteTable[address - 0x3eff];
        } else {
            throw new NESException(String.format("Address %d not expected to be read", address));
        }
    }

    public void write(byte data) {
        var address = addressRegister.get();
        if (address < 0x2000) {
            throw new NESException(String.format("Address %d is in CHR ROM, not expected to be written", address));
        } else if (address < 0x3000) {
            vram[mirrorVramAddress(address)] = data;
        } else if (address < 0x3f00) {
            throw new NESException(String.format("Address %d should not be written to", address));
        } else if (address == 0x3f10 || address == 0x3f14 || address == 0x3f18 || address == 0x3f1c) {
            // these four addresses are mirrors of the same address minus 0x10
            var mirroredAddress = address - 0x3f10;
            paletteTable[mirroredAddress] = data;
        } else if (address < 0x4000) {
            var mirroredAddress = address - 0x3f00;
            paletteTable[mirroredAddress] = data;
        } else {
            throw new NESException(String.format("Address %d is not expected to be written", address));
        }
        incrementVramAddress();
    }

    public byte readOamData() {
        return oamData[oamAddress];
    }

    public PPUTickResult tick(int cyclesToAdd) {
        cycles += cyclesToAdd;
        if (cycles >= 341) {
            cycles -= 341;
            ++scanline;
            if (scanline == 241) {
                statusRegister.setVBlankStatus(true);
                statusRegister.setSpriteZeroHit(false);
                if (controlRegister.canGenerateNMI()) {
                    return PPUTickResult.NMI;
                }
            }
            if (scanline >= 262) {
                scanline = 0;
                statusRegister.setSpriteZeroHit(false);
                statusRegister.resetVBlankStatus();
                return PPUTickResult.SCREEN_DONE;
            }
        }
        if (nmiInterrupt) {
            nmiInterrupt = false;
            return PPUTickResult.NMI;
        }
        return PPUTickResult.NORMAL;
    }

    public Frame getFrame() {
        int bank = controlRegister.getBackgroundPatternAddress();
        var background = new Tile[30][32];
        int index = 0;
        for (int y = 0; y < background.length; y++) {
            for (int x = 0; x < background[y].length; x++) {
                var tile = vram[index];
                background[y][x] = getTile(bank, tile, index);
                ++index;
            }
        }
        return new Frame(background, null);
    }

    public Tile getTile(int bankAddress, int tileIndex, int index) {
        var tile = new Tile(createPalette(index % 32, index / 32));
        for (int i = 0; i < 128; ++i) {
            int dataIndex = bankAddress + (tileIndex * 16) + i / 8;
            int x = i % 8;
            int y = (i % 64) / 8;
            byte value = (byte)((i / 64 + 1) * getBit(dataIndex, 7 - x));
            tile.add(x, y, value);
        }
        return tile;
    }

    private String[] createPalette(int column, int row) {
        var index = row / 4 * 8 + column / 4;
        var value = vram[0x3c0 + index];
        var paletteIndex = getPaletteIndex(column, row, value);
        var paletteStart = paletteIndex * 4 + 1;

        var palette = new String[4];
        palette[0] = COLOR_PALETTE[paletteTable[0]];
        palette[1] = COLOR_PALETTE[paletteTable[paletteStart]];
        palette[2] = COLOR_PALETTE[paletteTable[paletteIndex + 1]];
        palette[3] = COLOR_PALETTE[paletteTable[paletteIndex + 2]];
        return palette;
    }

    private int getPaletteIndex(int column, int row, byte value) {
        var rowIndex = (row % 4) / 2;
        if ((column % 4) / 2 == 0) {
            return rowIndex == 0 ? value & 3 : (value >> 4) & 3;
        }
        return rowIndex == 0 ? (value >> 2) & 3 : (value >> 6) & 3;
    }

    private int mirrorVramAddress(int address) {
        var mirroredAddress = address & 0b10111111111111;
        var vramIndex = mirroredAddress - 0x2000;
        var nameTable = vramIndex / 0x400;
        if (mirroring == Mirroring.VERTICAL) {
            if (nameTable == 2 || nameTable == 3) {
                return vramIndex - 0x800;
            }
        } else {
            if (vramIndex == 1 || vramIndex == 2) {
                return vramIndex - 0x400;
            } else if (vramIndex == 3) {
                return vramIndex - 0x800;
            }
        }
        return vramIndex;
    }

    private int getBit(int dataIndex, int index) {
        int bitMask = 1 << index;
        return (chrRom[dataIndex] & bitMask) > 0 ? 1 : 0;
    }
}
