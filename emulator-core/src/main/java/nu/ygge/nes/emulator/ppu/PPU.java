package nu.ygge.nes.emulator.ppu;

import lombok.Getter;
import nu.ygge.nes.emulator.bus.PPUTickResult;

import java.util.Arrays;

public class PPU {

    private static final int CYCLES_PER_SCANLINE = 341;
    private static final int VISIBLE_SCANLINES = 240;
    private static final int VBLANK_SCANLINE = 241;
    private static final int SCANLINES_PER_FRAME = 262;
    private static final int PATTERN_TABLE_SIZE = 0x2000;
    private static final int TILE_SIZE = 16;
    private static final int TILES_PER_ROW = 32;
    private static final int ATTRIBUTE_TABLE_OFFSET = 0x3c0;
    private static final int OAM_ENTRIES = 64;
    private static final int SPRITES_PER_SCANLINE = 8;
    private static final int SPRITE_PALETTE_OFFSET = 0x10;

    private final WriteLatch writeLatch = new WriteLatch();
    private final boolean[] backgroundOpaque = new boolean[Frame.WIDTH];
    private final int[] scanlineSprites = new int[SPRITES_PER_SCANLINE];

    @Getter
    private AddressRegister addressRegister;
    private ScrollRegister scrollRegister;
    private ControlRegister controlRegister;
    private MaskRegister maskRegister;
    private StatusRegister statusRegister;
    private byte[] paletteTable, vram, oamData, patternTable;
    private boolean patternTableIsRam;
    private Mirroring mirroring;
    private byte dataBuffer;
    private int oamAddress, scanline, cycles;
    private boolean nmiInterrupt, frameComplete;
    private Frame workingFrame, completedFrame;

    public PPU() {
        reset(new byte[0], Mirroring.HORIZONTAL);
    }

    public void reset(byte[] chrRom, Mirroring mirroring) {
        writeLatch.reset();
        addressRegister = new AddressRegister(writeLatch);
        scrollRegister = new ScrollRegister(writeLatch);
        controlRegister = new ControlRegister();
        maskRegister = new MaskRegister();
        statusRegister = new StatusRegister();
        paletteTable = new byte[32];
        vram = new byte[2048];
        oamData = new byte[256];
        // a cartridge without CHR ROM brings its own writable pattern table instead
        patternTableIsRam = chrRom.length == 0;
        patternTable = chrRom.length >= PATTERN_TABLE_SIZE ? chrRom : Arrays.copyOf(chrRom, PATTERN_TABLE_SIZE);
        this.mirroring = mirroring;
        workingFrame = new Frame();
        completedFrame = new Frame();
        dataBuffer = 0;
        oamAddress = 0;
        scanline = 0;
        cycles = 0;
        nmiInterrupt = false;
        frameComplete = false;
    }

    public void writeToAddressRegister(byte value) {
        addressRegister.write(value);
    }

    public void writeToScrollRegister(byte value) {
        scrollRegister.write(value);
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
        writeLatch.reset();
        return data;
    }

    public void writeToOamAddress(byte value) {
        oamAddress = value & 0xff;
    }

    public void writeToOamData(byte value) {
        oamData[oamAddress] = value;
        oamAddress = (oamAddress + 1) & 0xff;
    }

    public byte readOamData() {
        return oamData[oamAddress];
    }

    /**
     * Bulk transfer of a full CPU page into OAM, starting at the current OAM address.
     */
    public void writeOamDma(byte[] page) {
        for (byte value : page) {
            writeToOamData(value);
        }
    }

    public void incrementVramAddress() {
        addressRegister.add(controlRegister.getVramAddressIncrement());
    }

    public byte read() {
        var address = addressRegister.get();
        incrementVramAddress();
        if (address < 0x2000) {
            // reads from the pattern table are delayed by one read
            var data = dataBuffer;
            dataBuffer = patternTable[address];
            return data;
        } else if (address < 0x3f00) {
            // reads from the name tables are delayed by one read
            var data = dataBuffer;
            dataBuffer = vram[mirrorVramAddress(address)];
            return data;
        }
        // palette reads are immediate, but still fill the buffer with the name table byte underneath
        dataBuffer = vram[mirrorVramAddress(address)];
        return paletteTable[paletteIndex(address)];
    }

    public void write(byte data) {
        var address = addressRegister.get();
        if (address < 0x2000) {
            if (patternTableIsRam) {
                patternTable[address] = data;
            }
        } else if (address < 0x3f00) {
            vram[mirrorVramAddress(address)] = data;
        } else {
            paletteTable[paletteIndex(address)] = (byte) (data & 0x3f);
        }
        incrementVramAddress();
    }

    public PPUTickResult tick(int cyclesToAdd) {
        cycles += cyclesToAdd;
        while (cycles >= CYCLES_PER_SCANLINE) {
            cycles -= CYCLES_PER_SCANLINE;
            finishScanline();
        }
        if (nmiInterrupt) {
            nmiInterrupt = false;
            frameComplete = false;
            return PPUTickResult.NMI;
        }
        if (frameComplete) {
            frameComplete = false;
            return PPUTickResult.SCREEN_DONE;
        }
        return PPUTickResult.NORMAL;
    }

    public Frame getFrame() {
        return completedFrame;
    }

    private void finishScanline() {
        if (scanline < VISIBLE_SCANLINES) {
            renderScanline(scanline);
        }
        ++scanline;
        if (scanline == VBLANK_SCANLINE) {
            workingFrame.copyTo(completedFrame);
            frameComplete = true;
            statusRegister.setVBlankStatus(true);
            if (controlRegister.canGenerateNMI()) {
                nmiInterrupt = true;
            }
        } else if (scanline >= SCANLINES_PER_FRAME) {
            scanline = 0;
            statusRegister.setVBlankStatus(false);
            statusRegister.setSpriteZeroHit(false);
            statusRegister.setSpriteOverflow(false);
        }
    }

    private void renderScanline(int y) {
        renderBackground(y);
        renderSprites(y);
    }

    private void renderBackground(int y) {
        var backdrop = toColor(paletteTable[0]);
        if (!maskRegister.isShowBackground()) {
            Arrays.fill(backgroundOpaque, false);
            for (int x = 0; x < Frame.WIDTH; ++x) {
                workingFrame.setPixel(x, y, backdrop);
            }
            return;
        }
        var bank = controlRegister.getBackgroundPatternAddress();
        var sourceY = y + scrollRegister.getY();
        var baseNameTable = controlRegister.getNameTableAddress();
        if (sourceY >= VISIBLE_SCANLINES) {
            sourceY -= VISIBLE_SCANLINES;
            baseNameTable ^= 0x800;
        }
        var tileRow = sourceY >> 3;
        var fineY = sourceY & 7;
        int loadedColumn = -1, loadedNameTable = -1;
        int lowPlane = 0, highPlane = 0, paletteBase = 0;
        for (int x = 0; x < Frame.WIDTH; ++x) {
            var sourceX = x + scrollRegister.getX();
            var nameTable = baseNameTable;
            if (sourceX >= Frame.WIDTH) {
                sourceX -= Frame.WIDTH;
                nameTable ^= 0x400;
            }
            var tileColumn = sourceX >> 3;
            if (tileColumn != loadedColumn || nameTable != loadedNameTable) {
                loadedColumn = tileColumn;
                loadedNameTable = nameTable;
                var tileIndex = vram[mirrorVramAddress(nameTable + tileRow * TILES_PER_ROW + tileColumn)] & 0xff;
                var patternAddress = bank + tileIndex * TILE_SIZE + fineY;
                lowPlane = patternTable[patternAddress] & 0xff;
                highPlane = patternTable[patternAddress + 8] & 0xff;
                paletteBase = backgroundPaletteBase(nameTable, tileRow, tileColumn);
            }
            var bit = 7 - (sourceX & 7);
            var colorIndex = ((lowPlane >> bit) & 1) | (((highPlane >> bit) & 1) << 1);
            var opaque = colorIndex != 0 && (x >= 8 || maskRegister.isLeftMost8pxlBackground());
            backgroundOpaque[x] = opaque;
            workingFrame.setPixel(x, y, opaque ? toColor(paletteTable[paletteBase + colorIndex]) : backdrop);
        }
    }

    private int backgroundPaletteBase(int nameTable, int tileRow, int tileColumn) {
        var address = nameTable + ATTRIBUTE_TABLE_OFFSET + (tileRow >> 2) * 8 + (tileColumn >> 2);
        var attribute = vram[mirrorVramAddress(address)] & 0xff;
        // each attribute byte holds four palette selections, one per 16x16 pixel quadrant
        var shift = ((tileRow & 2) << 1) | (tileColumn & 2);
        return ((attribute >> shift) & 3) * 4;
    }

    private void renderSprites(int y) {
        if (!maskRegister.isShowSprite()) {
            return;
        }
        var spriteHeight = controlRegister.getSpriteSize();
        var count = 0;
        for (int i = 0; i < OAM_ENTRIES; ++i) {
            var row = y - (oamData[i * 4] & 0xff) - 1;
            if (row < 0 || row >= spriteHeight) {
                continue;
            }
            if (count == SPRITES_PER_SCANLINE) {
                statusRegister.setSpriteOverflow(true);
                break;
            }
            scanlineSprites[count++] = i;
        }
        // a lower OAM index wins over a higher one, so draw them back to front
        for (int i = count - 1; i >= 0; --i) {
            renderSprite(scanlineSprites[i], y, spriteHeight);
        }
    }

    private void renderSprite(int index, int y, int spriteHeight) {
        var spriteY = oamData[index * 4] & 0xff;
        var tileIndex = oamData[index * 4 + 1] & 0xff;
        var attributes = oamData[index * 4 + 2] & 0xff;
        var spriteX = oamData[index * 4 + 3] & 0xff;
        var behindBackground = (attributes & 0x20) != 0;
        var flipHorizontally = (attributes & 0x40) != 0;
        var flipVertically = (attributes & 0x80) != 0;
        var paletteBase = SPRITE_PALETTE_OFFSET + (attributes & 3) * 4;

        var row = y - spriteY - 1;
        if (flipVertically) {
            row = spriteHeight - 1 - row;
        }
        int bank, tile;
        if (spriteHeight == 16) {
            // a tall sprite picks its bank from the lowest bit of the tile index
            bank = (tileIndex & 1) * 0x1000;
            tile = tileIndex & 0xfe;
            if (row >= 8) {
                ++tile;
                row -= 8;
            }
        } else {
            bank = controlRegister.getSpritePatternAddress();
            tile = tileIndex;
        }
        var patternAddress = bank + tile * TILE_SIZE + row;
        var lowPlane = patternTable[patternAddress] & 0xff;
        var highPlane = patternTable[patternAddress + 8] & 0xff;
        for (int x = 0; x < 8; ++x) {
            var screenX = spriteX + x;
            if (screenX >= Frame.WIDTH || (screenX < 8 && !maskRegister.isLeftMost8pxlSprite())) {
                continue;
            }
            var bit = flipHorizontally ? x : 7 - x;
            var colorIndex = ((lowPlane >> bit) & 1) | (((highPlane >> bit) & 1) << 1);
            if (colorIndex == 0) {
                continue;
            }
            if (index == 0 && backgroundOpaque[screenX] && screenX != Frame.WIDTH - 1) {
                statusRegister.setSpriteZeroHit(true);
            }
            if (behindBackground && backgroundOpaque[screenX]) {
                continue;
            }
            workingFrame.setPixel(screenX, y, toColor(paletteTable[paletteBase + colorIndex]));
        }
    }

    private byte toColor(byte paletteEntry) {
        var color = paletteEntry & 0x3f;
        if (maskRegister.isGrayscale()) {
            color &= 0x30;
        }
        return (byte) color;
    }

    private int mirrorVramAddress(int address) {
        var mirroredAddress = address & 0x2fff;
        var vramIndex = mirroredAddress - 0x2000;
        var nameTable = vramIndex / 0x400;
        if (mirroring == Mirroring.VERTICAL) {
            if (nameTable >= 2) {
                return vramIndex - 0x800;
            }
        } else if (nameTable == 1 || nameTable == 2) {
            return vramIndex - 0x400;
        } else if (nameTable == 3) {
            return vramIndex - 0x800;
        }
        return vramIndex;
    }

    private static int paletteIndex(int address) {
        var index = address & 0x1f;
        // the backdrop entry of each sprite palette mirrors the corresponding background one
        if (index >= SPRITE_PALETTE_OFFSET && (index & 3) == 0) {
            index -= SPRITE_PALETTE_OFFSET;
        }
        return index;
    }
}
