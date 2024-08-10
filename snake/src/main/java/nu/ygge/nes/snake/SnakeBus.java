package nu.ygge.nes.snake;

import lombok.Getter;
import nu.ygge.nes.emulator.bus.Bus;

@Getter
public class SnakeBus implements Bus {

    private final byte[] memory = new byte[65536];

    @Override
    public byte read(int address) {
        return memory[address];
    }

    @Override
    public void write(int address, byte data) {
        memory[address] = data;
    }

    public void writeData(int gameCodeAddress, short[] gameCode) {
        for (int i = 0; i < gameCode.length; ++i) {
            memory[i + gameCodeAddress] = (byte) gameCode[i];
        }
    }
}
