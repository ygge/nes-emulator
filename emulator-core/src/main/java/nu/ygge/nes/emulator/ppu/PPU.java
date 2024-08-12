package nu.ygge.nes.emulator.ppu;

import nu.ygge.nes.emulator.bus.PPUTickResult;
import nu.ygge.nes.emulator.exception.NESException;

public class PPU {

    public static final String[][] COLOR_PALETTES = {
            new String[]{ "626262", "ABABAB", "FFFFFF", "FFFFFF" },
            new String[]{ "002E98", "0064F4", "4AB5FF", "B6E1FF" },
            new String[]{ "0C11C2", "353CFF", "858CFF", "CED1FF" },
            new String[]{ "3B00C2", "761BFF", "C86AFF", "E9C3FF" },
            new String[]{ "650098", "AE0AF4", "FF58FF", "FFBCFF" },
            new String[]{ "7D004E", "CF0C8F", "FF5BE2", "FFBDF4" },
            new String[]{ "7D0000", "CF231C", "FF726A", "FFC6C3" },
            new String[]{ "651900", "AE4700", "FF9702", "FFD59A" },
            new String[]{ "3B3600", "766F00", "C8C100", "E9E681" },
            new String[]{ "0C4F00", "359000", "85E300", "CEF481" },
            new String[]{ "005B00", "00A100", "4AF502", "B6FB9A" },
            new String[]{ "005900", "009E1C", "29F26A", "A9FAC3" },
            new String[]{ "00494E", "00888F", "29DBE2", "A9F0F4" },
            new String[]{ "000000", "000000", "4E4E4E", "B8B8B8" },
            new String[]{ "000000", "000000", "000000", "000000" },
            new String[]{ "000000", "000000", "000000", "000000" },
    };

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
        } else if (address <= 0x4000) {
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
        for (int y = 0; y < background.length; y++) {
            for (int x = 0; x < background[y].length; x++) {
                var tile = vram[y * background[y].length + x];
                background[y][x] = getTile(bank, tile);
            }
        }
        return new Frame(background, null);
    }

    public Tile getTile(int bankIndex, int tileIndex) {
        var tile = new Tile();
        for (int i = 0; i < 128; ++i) {
            int dataIndex = (bankIndex * 0x1000) + (tileIndex * 16) + i / 8;
            int x = i % 8;
            int y = (i % 64) / 8;
            byte value = (byte)((i / 64 + 1) * getBit(dataIndex, 7 - x));
            tile.add(x, y, value);
        }
        return tile;
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
