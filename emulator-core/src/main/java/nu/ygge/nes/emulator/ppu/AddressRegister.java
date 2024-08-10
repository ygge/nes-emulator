package nu.ygge.nes.emulator.ppu;

public class AddressRegister {

    private final byte[] value = new byte[2];
    private int currentIndex = 0;

    public int get() {
        return value[0] << 8 | value[1];
    }

    public void write(byte value) {
        this.value[currentIndex] = value;
        currentIndex = 1 - currentIndex;
        wrapValue();
    }

    public void add(byte value) {
        var newValue = (byte)(this.value[1] + value);
        if (newValue < this.value[1]) {
            ++this.value[0];
        }
        this.value[1] = newValue;
        wrapValue();
    }

    private void set(int value) {
        this.value[0] = (byte)(value&0xFF00);
        this.value[1] = (byte)(value&0xFF);
    }

    private void wrapValue() {
        if (get() > 0x3fff) {
            set(get() & 0x3fff);
        }
    }
}
