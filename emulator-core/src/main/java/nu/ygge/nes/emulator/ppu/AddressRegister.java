package nu.ygge.nes.emulator.ppu;

import nu.ygge.nes.emulator.FileLogger;
import nu.ygge.nes.emulator.cpu.CPUUtil;

public class AddressRegister {

    private final byte[] value = new byte[2];
    private int currentIndex = 0;

    public int get() {
        return CPUUtil.toAddress(value[0], value[1]);
    }

    public void write(byte value) {
        this.value[currentIndex] = value;
        currentIndex = 1 - currentIndex;
        wrapValue();
    }

    public void add(byte value) {
        var newValue = (byte)(this.value[1] + value);
        if (newValue >= 0 && this.value[1] < 0) {
            ++this.value[0];
        }
        this.value[1] = newValue;
        wrapValue();
    }

    public void resetLatch() {
        currentIndex = 0;
    }

    private void set(int value) {
        this.value[0] = (byte)(value >> 8);
        this.value[1] = (byte)(value&0xFF);
    }

    private void wrapValue() {
        if (get() > 0x3fff) {
            set(get() & 0x3fff);
        }
    }
}
