package nu.ygge.nes.emulator.ppu;

public class StatusRegister {

    private static final int VBLANK_STARTED  = 0b10000000;

    private short register = 0;

    public void update(byte data) {
        register = data;
        if (register < 0) {
            register += 256;
        }
    }

    public void setVBlankStatus(boolean status) {
        if (status) {
            register &= 0x7f;
        } else {
            register |= 0x80;
        }
    }

    public void setSpriteZeroHit(boolean status) {
        if (status) {
            register &= 0xbf;
        } else {
            register |= 0x40;
        }
    }

    public void setSpriteOverflow(boolean status) {
        if (status) {
            register &= 0xdf;
        } else {
            register |= 0x20;
        }
    }

    public void resetVBlankStatus() {
        setVBlankStatus(false);
    }

    public boolean isInVBlankStatus() {
        return (register & VBLANK_STARTED) == VBLANK_STARTED;
    }

    public byte getSnapshot() {
        return (byte) (register);
    }
}
