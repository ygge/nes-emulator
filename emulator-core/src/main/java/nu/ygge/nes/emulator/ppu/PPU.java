package nu.ygge.nes.emulator.ppu;

public class PPU {

    private byte[] chrRom;

    public PPU() {
        this.chrRom = new byte[0];
    }

    public void setCharacterROM(byte[] chrRom) {
        this.chrRom = chrRom;
    }

    public byte[] getCharacterROM() {
        return chrRom;
    }

    public Tile getTile(int bankIndex, int tileIndex) {
        var tile = new Tile();
        for (int i = 0; i < 64; ++i) {
            int dataIndex = (bankIndex * 0x1000) + (tileIndex * 16) + i / 8;
            int x = i % 8;
            int y = i / 8;
            byte value = (byte)((i / 32 + 1) * getBit(dataIndex, 7 - x));
            tile.add(x, y, value);
        }
        return tile;
    }

    private int getBit(int dataIndex, int index) {
        int bitMask = 1 << index;
        return (chrRom[dataIndex] & bitMask) > 0 ? 1 : 0;
    }
}
