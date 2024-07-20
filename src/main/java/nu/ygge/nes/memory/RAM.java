package nu.ygge.nes.memory;

public class RAM {

    private final byte[] memory = new byte[2048];

    public byte read(int address) {
        return memory[address];
    }

    public void write(int address, byte value) {
        memory[address] = value;
    }
}
