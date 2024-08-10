package nu.ygge.nes.emulator.cpu;

public class CPURAM {

    private final byte[] ram = new byte[0x2000];

    public byte read(int address) {
        return ram[address];
    }

    public void write(int address, byte value) {
        ram[address] = value;
    }

    public void writeData(int gameCodeAddress, byte[] gameCode) {
        System.arraycopy(gameCode, 0, ram, gameCodeAddress, gameCode.length);
    }
}
