package nu.ygge.nes.emulator.ppu;

public class ControlRegister {

    private static final short NAMETABLE1             = 0b00000001;
    private static final short NAMETABLE2             = 0b00000010;
    private static final short VRAM_ADD_INCREMENT     = 0b00000100;
    private static final short SPRITE_PATTERN_ADDR    = 0b00001000;
    private static final short BACKROUND_PATTERN_ADDR = 0b00010000;
    private static final short SPRITE_SIZE            = 0b00100000;
    private static final short MASTER_SLAVE_SELECT    = 0b01000000;
    private static final short GENERATE_NMI           = 0b10000000;

    private short register = 0;

    public void update(byte data) {
        register = data;
    }

    public byte getVramAddrIncrement() {
        return (register&VRAM_ADD_INCREMENT) == 0 ? (byte)32 : 1;
    }
}
