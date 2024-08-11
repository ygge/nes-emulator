package nu.ygge.nes.emulator.bus;

import nu.ygge.nes.emulator.ppu.Frame;

public interface Bus {

    byte read(int address);
    void write(int address, byte data);

    PPUTickResult ppuTick(int cycles);
    default Frame getFrame() {
        return null;
    }
}
