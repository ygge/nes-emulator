package nu.ygge.nes.cpu.util;

import lombok.Getter;
import nu.ygge.nes.memory.Memory;

public final class MemoryWriter {

    private final Memory memory;
    @Getter
    private int address;

    public MemoryWriter(Memory memory, int startAddress) {
        this.memory = memory;
        this.address = startAddress;
    }

    public void write(byte data) {
        memory.write(address, data);
        ++address;
    }
}
