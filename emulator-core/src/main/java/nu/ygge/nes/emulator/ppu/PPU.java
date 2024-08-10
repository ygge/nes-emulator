package nu.ygge.nes.emulator.ppu;

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
    private byte[] paletteTable, vram, oamData, chrRom;

    public void reset(byte[] chrRom) {
        addressRegister = new AddressRegister();
        paletteTable = new byte[32];
        vram = new byte[2048];
        oamData = new byte[256];
        this.chrRom = chrRom;
    }

    public void writeToAddressRegister(byte value) {
        addressRegister.write(value);
    }

    public byte[] getCharacterROM() {
        return chrRom;
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

    private int getBit(int dataIndex, int index) {
        int bitMask = 1 << index;
        return (chrRom[dataIndex] & bitMask) > 0 ? 1 : 0;
    }
}
