package nu.ygge.nes.emulator.ppu;

import nu.ygge.nes.emulator.bus.PPUTickResult;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PPUTest {

    private static final int CYCLES_PER_SCANLINE = 341;
    private static final int VBLANK_SCANLINE = 241;
    private static final int SCANLINES_PER_FRAME = 262;
    private static final int VBLANK_FLAG = 0x80;
    private static final int SPRITE_ZERO_HIT_FLAG = 0x40;
    // both masks also enable rendering of the leftmost eight pixels
    private static final int SHOW_BACKGROUND = 0b0000_1010;
    private static final int SHOW_SPRITES = 0b0001_0100;
    private static final int BACKDROP = 0x0f;
    private static final int BACKGROUND_COLOR = 0x21;
    private static final int SPRITE_COLOR = 0x16;

    private byte[] chrRom;
    private PPU ppu;

    @BeforeEach
    void setUp() {
        chrRom = new byte[8192];
        // tile 1 is opaque across the left half of every row, tile 0 is left fully transparent
        for (int row = 0; row < 8; ++row) {
            chrRom[16 + row] = (byte) 0b1111_0000;
        }
        ppu = new PPU();
        ppu.reset(chrRom, Mirroring.HORIZONTAL);
        writeVram(0x3f00, BACKDROP, BACKGROUND_COLOR);
        writeVram(0x3f11, SPRITE_COLOR);
    }

    @Test
    void verifyVBlankIsFlaggedWhenTheVisibleScanlinesAreDone() {
        runScanlines(240);
        Assertions.assertEquals(0, ppu.readStatus() & VBLANK_FLAG);

        runScanlines(1);
        Assertions.assertEquals(VBLANK_FLAG, ppu.readStatus() & VBLANK_FLAG);
    }

    @Test
    void verifyVBlankIsClearedAgainWhenTheNextFrameStarts() {
        runScanlines(262);

        Assertions.assertEquals(0, ppu.readStatus() & VBLANK_FLAG);
    }

    @Test
    void verifyNonMaskableInterruptIsRaisedWhenVBlankStarts() {
        ppu.writeToControlRegister((byte) 0x80);

        for (int i = 0; i < 240; ++i) {
            Assertions.assertEquals(PPUTickResult.NORMAL, ppu.tick(CYCLES_PER_SCANLINE));
        }

        Assertions.assertEquals(PPUTickResult.NMI, ppu.tick(CYCLES_PER_SCANLINE));
    }

    @Test
    void verifyIncrementingTheAddressOnlyCarriesOnALowByteOverflow() {
        var register = new ScrollAddress();

        register.writeAddress((byte) 0x21);
        register.writeAddress((byte) 0x7f);
        register.incrementAddress(1);
        Assertions.assertEquals(0x2180, register.getAddress());

        register.writeAddress((byte) 0x21);
        register.writeAddress((byte) 0xff);
        register.incrementAddress(1);
        Assertions.assertEquals(0x2200, register.getAddress());
    }

    @Test
    void verifyReadingTheStatusRegisterResetsTheSharedWriteLatch() {
        writeVram(0x2100, 0x42);
        ppu.writeToAddressRegister((byte) 0x21); // a first write that is never followed by a second

        ppu.readStatus();

        Assertions.assertEquals(0x42, readVram(0x2100));
    }

    @Test
    void verifyTheSpriteBackdropEntriesMirrorTheBackgroundOnes() {
        writeVram(0x3f10, 0x25);

        Assertions.assertEquals(0x25, readPalette(0x3f00));
    }

    @Test
    void verifyHorizontalMirroringSharesTheFirstTwoNameTables() {
        writeVram(0x2400, 0x42);

        Assertions.assertEquals(0x42, readVram(0x2000));
    }

    @Test
    void verifyHorizontalMirroringSharesTheLastTwoNameTables() {
        writeVram(0x2c00, 0x42);

        Assertions.assertEquals(0x42, readVram(0x2800));
    }

    @Test
    void verifyVerticalMirroringSharesEveryOtherNameTable() {
        ppu.reset(chrRom, Mirroring.VERTICAL);
        writeVram(0x2800, 0x42);

        Assertions.assertEquals(0x42, readVram(0x2000));
    }

    @Test
    void verifyBackgroundTilesArePaintedWithTheirPalette() {
        writeVram(0x2000, 1);
        ppu.writeToMaskRegister((byte) SHOW_BACKGROUND);
        writeScroll(0, 0);

        var frame = renderFrame();

        Assertions.assertEquals(BACKGROUND_COLOR, frame.getPixel(0, 0));
        Assertions.assertEquals(BACKGROUND_COLOR, frame.getPixel(3, 7));
        Assertions.assertEquals(BACKDROP, frame.getPixel(4, 0));
        Assertions.assertEquals(BACKDROP, frame.getPixel(0, 8));
    }

    @Test
    void verifyTheAttributeTableSelectsTheBackgroundPalette() {
        writeVram(0x3f0d, 0x30); // colour 1 of background palette 3
        writeVram(0x2000, 1);
        writeVram(0x23c0, 0b11); // the top left quadrant uses palette 3
        ppu.writeToMaskRegister((byte) SHOW_BACKGROUND);
        writeScroll(0, 0);

        Assertions.assertEquals(0x30, renderFrame().getPixel(0, 0));
    }

    @Test
    void verifyHorizontalScrollingShiftsTheBackground() {
        writeVram(0x2001, 1); // the tile in the second column
        ppu.writeToMaskRegister((byte) SHOW_BACKGROUND);
        writeScroll(8, 0);

        Assertions.assertEquals(BACKGROUND_COLOR, renderFrame().getPixel(0, 0));
    }

    @Test
    void verifyVerticalScrollingShiftsTheBackground() {
        writeVram(0x2020, 1); // the tile in the second row
        ppu.writeToMaskRegister((byte) SHOW_BACKGROUND);
        writeScroll(0, 8);

        Assertions.assertEquals(BACKGROUND_COLOR, renderFrame().getPixel(0, 0));
    }

    @Test
    void verifyAVerticalScrollWrittenDuringRenderingOnlyAffectsTheNextFrame() {
        writeVram(0x2020, 1); // the tile in the second row
        ppu.writeToMaskRegister((byte) SHOW_BACKGROUND);
        writeScroll(0, 0);
        runScanlines(SCANLINES_PER_FRAME); // let the starting position take effect

        runScanlines(100);
        writeScroll(0, 8);
        runScanlines(141);
        Assertions.assertEquals(BACKDROP, ppu.getFrame().getPixel(0, 0));

        runScanlines(SCANLINES_PER_FRAME - VBLANK_SCANLINE);
        runScanlines(VBLANK_SCANLINE);
        Assertions.assertEquals(BACKGROUND_COLOR, ppu.getFrame().getPixel(0, 0));
    }

    @Test
    void verifyAHorizontalScrollWrittenDuringRenderingAffectsTheRestOfTheFrame() {
        fillNameTableColumn(1);
        ppu.writeToMaskRegister((byte) SHOW_BACKGROUND);
        writeScroll(0, 0);
        runScanlines(SCANLINES_PER_FRAME); // let the starting position take effect

        runScanlines(100);
        writeScroll(8, 0);
        runScanlines(141);

        var frame = ppu.getFrame();
        Assertions.assertEquals(BACKDROP, frame.getPixel(0, 0));
        Assertions.assertEquals(BACKGROUND_COLOR, frame.getPixel(0, 120));
    }

    @Test
    void verifySpritesArePaintedFromOam() {
        writeOam(0, 10, 1, 0, 100);
        ppu.writeToMaskRegister((byte) SHOW_SPRITES);

        var frame = renderFrame();

        Assertions.assertEquals(SPRITE_COLOR, frame.getPixel(100, 11));
        Assertions.assertEquals(BACKDROP, frame.getPixel(104, 11));
    }

    @Test
    void verifyHorizontallyFlippedSpritesArePaintedMirrored() {
        writeOam(0, 10, 1, 0x40, 100);
        ppu.writeToMaskRegister((byte) SHOW_SPRITES);

        var frame = renderFrame();

        Assertions.assertEquals(BACKDROP, frame.getPixel(100, 11));
        Assertions.assertEquals(SPRITE_COLOR, frame.getPixel(104, 11));
    }

    @Test
    void verifySpritesInFrontOfTheBackgroundCoverIt() {
        writeVram(0x2000, 1);
        writeOam(0, 0, 1, 0, 0);
        ppu.writeToMaskRegister((byte) (SHOW_BACKGROUND | SHOW_SPRITES));
        writeScroll(0, 0);

        Assertions.assertEquals(SPRITE_COLOR, renderFrame().getPixel(0, 1));
    }

    @Test
    void verifySpritesBehindTheBackgroundAreHiddenByOpaquePixels() {
        writeVram(0x2000, 1);
        writeOam(0, 0, 1, 0x20, 0);
        ppu.writeToMaskRegister((byte) (SHOW_BACKGROUND | SHOW_SPRITES));
        writeScroll(0, 0);

        Assertions.assertEquals(BACKGROUND_COLOR, renderFrame().getPixel(0, 1));
    }

    @Test
    void verifySpriteZeroHitIsFlaggedWhenSpriteZeroOverlapsTheBackground() {
        writeVram(0x2000, 1);
        writeOam(0, 0, 1, 0, 0);
        ppu.writeToMaskRegister((byte) (SHOW_BACKGROUND | SHOW_SPRITES));
        writeScroll(0, 0);

        renderFrame();

        Assertions.assertEquals(SPRITE_ZERO_HIT_FLAG, ppu.readStatus() & SPRITE_ZERO_HIT_FLAG);
    }

    @Test
    void verifyScrollingPastTheEndOfANameTableContinuesInTheNextOne() {
        ppu.reset(chrRom, Mirroring.VERTICAL);
        writeVram(0x3f00, BACKDROP, BACKGROUND_COLOR);
        writeVram(0x2400, 1); // the first tile of the second name table
        ppu.writeToMaskRegister((byte) SHOW_BACKGROUND);
        ppu.writeToControlRegister((byte) 0); // start out in the first name table
        writeScroll(248, 0); // eight pixels short of a whole screen

        var frame = renderFrame();

        Assertions.assertEquals(BACKDROP, frame.getPixel(0, 0));
        Assertions.assertEquals(BACKGROUND_COLOR, frame.getPixel(8, 0));
    }

    @Test
    void verifyTheControlRegisterPicksTheNameTableToRenderFrom() {
        ppu.reset(chrRom, Mirroring.VERTICAL);
        writeVram(0x3f00, BACKDROP, BACKGROUND_COLOR);
        writeVram(0x2000, 1); // a tile in the first name table only
        ppu.writeToMaskRegister((byte) SHOW_BACKGROUND);
        writeScroll(0, 0);

        ppu.writeToControlRegister((byte) 0b01); // the second name table, which is empty

        Assertions.assertEquals(BACKDROP, renderFrame().getPixel(0, 0));
    }

    /**
     * How a game keeps a still status bar while it is scrolled into the second name table: the
     * control register still selects that table, but because the two registers are shared, the
     * address write it makes during vblank points the next frame back at the first one.
     */
    @Test
    void verifyAnAddressWriteTakesTheNameTableBackOverFromTheControlRegister() {
        ppu.reset(chrRom, Mirroring.VERTICAL);
        writeVram(0x3f00, BACKDROP, BACKGROUND_COLOR);
        writeVram(0x2000, 1); // a tile in the first name table only
        ppu.writeToMaskRegister((byte) SHOW_BACKGROUND);

        ppu.writeToControlRegister((byte) 0b01); // select the second name table
        setAddress(0x2000); // and then point the address back at the first one
        writeScroll(0, 0);

        Assertions.assertEquals(BACKGROUND_COLOR, renderFrame().getPixel(0, 0));
    }

    @Test
    void verifyAnAddressWriteMovesTheVerticalScrollWithIt() {
        writeVram(0x2020, 1); // the tile in the second row
        ppu.writeToMaskRegister((byte) SHOW_BACKGROUND);
        setAddress(0x2020); // the same address puts the second row at the top of the screen

        Assertions.assertEquals(BACKGROUND_COLOR, renderFrame().getPixel(0, 0));
    }

    private Frame renderFrame() {
        // the vertical scroll is only taken over between frames, so let a whole frame pass first
        runScanlines(SCANLINES_PER_FRAME);
        runScanlines(VBLANK_SCANLINE);
        return ppu.getFrame();
    }

    private void runScanlines(int count) {
        for (int i = 0; i < count; ++i) {
            ppu.tick(CYCLES_PER_SCANLINE);
        }
    }

    private void fillNameTableColumn(int column) {
        for (int row = 0; row < 30; ++row) {
            writeVram(0x2000 + row * 32 + column, 1);
        }
    }

    private void writeVram(int address, int... values) {
        setAddress(address);
        for (var value : values) {
            ppu.write((byte) value);
        }
    }

    private int readVram(int address) {
        setAddress(address);
        ppu.read(); // reads outside the palette are delayed by one read
        return ppu.read() & 0xff;
    }

    private int readPalette(int address) {
        setAddress(address);
        return ppu.read() & 0xff;
    }

    private void setAddress(int address) {
        ppu.writeToAddressRegister((byte) (address >> 8));
        ppu.writeToAddressRegister((byte) address);
    }

    private void writeScroll(int x, int y) {
        ppu.writeToScrollRegister((byte) x);
        ppu.writeToScrollRegister((byte) y);
    }

    private void writeOam(int index, int y, int tile, int attributes, int x) {
        ppu.writeToOamAddress((byte) (index * 4));
        ppu.writeToOamData((byte) y);
        ppu.writeToOamData((byte) tile);
        ppu.writeToOamData((byte) attributes);
        ppu.writeToOamData((byte) x);
    }
}
