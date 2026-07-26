package nu.ygge.nes.emulator.ppu;

public class ControlRegister {

    private static final int VRAM_ADD_INCREMENT     = 0b00000100;
    private static final int SPRITE_PATTERN_ADDR    = 0b00001000;
    private static final int BACKROUND_PATTERN_ADDR = 0b00010000;
    private static final int SPRITE_SIZE            = 0b00100000;
    private static final int MASTER_SLAVE_SELECT    = 0b01000000;
    private static final int GENERATE_NMI           = 0b10000000;

    private int register = 0;

    public void update(byte data) {
        register = data & 0xff;
    }

    public int getVramAddressIncrement() {
        return (register & VRAM_ADD_INCREMENT) == 0 ? 1 : 32;
    }

    public int getSpritePatternAddress() {
        return (register & SPRITE_PATTERN_ADDR) == 0 ? 0 : 0x1000;
    }

    public int getBackgroundPatternAddress() {
        return (register & BACKROUND_PATTERN_ADDR) == 0 ? 0 : 0x1000;
    }

    public int getSpriteSize() {
        return (register & SPRITE_SIZE) == 0 ? 8 : 16;
    }

    public int getMasterSlaveSelect() {
        return (register & MASTER_SLAVE_SELECT) == 0 ? 0 : 1;
    }

    public boolean canGenerateNMI() {
        return (register & GENERATE_NMI) != 0;
    }
}
