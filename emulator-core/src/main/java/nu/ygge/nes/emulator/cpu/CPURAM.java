package nu.ygge.nes.emulator.cpu;

import nu.ygge.nes.emulator.state.StateReader;
import nu.ygge.nes.emulator.state.StateWriter;

public class CPURAM {

    private final byte[] ram = new byte[0x2000];

    public void saveState(StateWriter writer) {
        writer.writeBytes(ram);
    }

    public void loadState(StateReader reader) {
        reader.readBytes(ram);
    }

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
