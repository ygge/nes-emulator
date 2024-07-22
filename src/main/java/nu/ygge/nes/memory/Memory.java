package nu.ygge.nes.memory;

public class Memory {

    private final byte[] ram = new byte[2048];

    public byte read(int address) {
        return ram[address];
    }

    public void write(int address, byte value) {
        ram[address] = value;
    }
}
