package nu.ygge.nes.emulator.memory;

public class Memory {

    private final byte[] ram = new byte[65536];

    public byte read(int address) {
        return ram[toAddress(address)];
    }

    public void write(int address, byte value) {
        ram[toAddress(address)] = value;
    }

    public void writeData(int gameCodeAddress, short[] gameCode) {
        for (int i = 0; i < gameCode.length; ++i) {
            ram[i + gameCodeAddress] = (byte) gameCode[i];
        }
    }

    public void writeData(int gameCodeAddress, byte[] gameCode) {
        System.arraycopy(gameCode, 0, ram, gameCodeAddress, gameCode.length);
    }

    private int toAddress(int address) {
        return address % ram.length;
    }
}
