package nu.ygge.nes.emulator.ppu;

public class ControlRegister {

    private static final short VRAM_ADD_INCREMENT     = 0b00000100;
    private static final short SPRITE_PATTERN_ADDR    = 0b00001000;
    private static final short BACKROUND_PATTERN_ADDR = 0b00010000;
    private static final short SPRITE_SIZE            = 0b00100000;
    private static final short MASTER_SLAVE_SELECT    = 0b01000000;
    private static final short GENERATE_NMI           = 0b10000000;

    private short register = 0;

    public void update(byte data) {
        register = data;
        if (register < 0) {
            register += 256;
        }
    }

    public int getNameTableAddress() {
        return switch (register & 3) {
            case 0 -> 0x2000;
            case 1 -> 0x2400;
            case 2 -> 0x2800;
            case 3 -> 0x2c00;
            default -> throw new IllegalStateException("Unexpected value: " + (register & 3));
        };
    }

    public byte getVramAddressIncrement() {
        return (register & VRAM_ADD_INCREMENT) == 0 ? (byte)32 : 1;
    }

    public int getSpritePatternAddress() {
        return (register & SPRITE_PATTERN_ADDR) == 0 ? 0x1000 : 0;
    }

    public int getBackgroundPatternAddress() {
        return (register & BACKROUND_PATTERN_ADDR) == 0 ? 0x1000 : 0;
    }

    public byte getSpriteSize() {
        return (register & SPRITE_SIZE) == 0 ? (byte)16 : 8;
    }

    public byte getMasterSlaveSelect() {
        return (register & MASTER_SLAVE_SELECT) == 0 ? (byte)0 : 1;
    }

    public boolean canGenerateNMI() {
        return (register & GENERATE_NMI) != 0;
    }
}
