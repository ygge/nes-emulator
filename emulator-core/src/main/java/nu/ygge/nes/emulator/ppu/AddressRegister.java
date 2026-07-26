package nu.ygge.nes.emulator.ppu;

public class AddressRegister {

    private static final int ADDRESS_MASK = 0x3fff;

    private final WriteLatch latch;
    private int value;

    public AddressRegister(WriteLatch latch) {
        this.latch = latch;
    }

    public int get() {
        return value;
    }

    public void write(byte data) {
        if (latch.isSecondWrite()) {
            value = (value & 0xff00) | (data & 0xff);
        } else {
            value = ((data & 0xff) << 8) | (value & 0xff);
        }
        latch.toggle();
        value &= ADDRESS_MASK;
    }

    public void add(int increment) {
        value = (value + increment) & ADDRESS_MASK;
    }
}
