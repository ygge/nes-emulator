package nu.ygge.nes.emulator.ppu;

import nu.ygge.nes.emulator.state.StateReader;
import nu.ygge.nes.emulator.state.StateWriter;

public class StatusRegister {

    private static final int SPRITE_OVERFLOW = 0b00100000;
    private static final int SPRITE_ZERO_HIT = 0b01000000;
    private static final int VBLANK_STARTED  = 0b10000000;

    private int register = 0;

    public void saveState(StateWriter writer) {
        writer.writeInt(register);
    }

    public void loadState(StateReader reader) {
        register = reader.readInt();
    }

    public void setVBlankStatus(boolean status) {
        setFlag(VBLANK_STARTED, status);
    }

    public void setSpriteZeroHit(boolean status) {
        setFlag(SPRITE_ZERO_HIT, status);
    }

    public void setSpriteOverflow(boolean status) {
        setFlag(SPRITE_OVERFLOW, status);
    }

    public void resetVBlankStatus() {
        setVBlankStatus(false);
    }

    public boolean isInVBlankStatus() {
        return (register & VBLANK_STARTED) != 0;
    }

    public byte getSnapshot() {
        return (byte) register;
    }

    private void setFlag(int mask, boolean status) {
        if (status) {
            register |= mask;
        } else {
            register &= ~mask;
        }
    }
}
