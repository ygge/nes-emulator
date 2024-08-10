package nu.ygge.nes.emulator.util;

import lombok.Getter;
import nu.ygge.nes.emulator.bus.Bus;
import nu.ygge.nes.emulator.cpu.CPURAM;

public final class MemoryWriter {

    private final Bus bus;
    @Getter
    private int address;

    public MemoryWriter(Bus bus, int startAddress) {
        this.bus = bus;
        this.address = startAddress;
    }

    public void write(byte data) {
        bus.write(address, data);
        ++address;
    }
}
