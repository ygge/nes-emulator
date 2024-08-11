package nu.ygge.nes.emulator.util;

import nu.ygge.nes.emulator.bus.Bus;
import nu.ygge.nes.emulator.bus.PPUTickResult;

public class TestBus implements Bus {

    private final byte[] ram = new byte[65536];

    @Override
    public byte read(int address) {
        return ram[toAddress(address)];
    }

    @Override
    public void write(int address, byte data) {
        ram[toAddress(address)] = data;
    }

    @Override
    public PPUTickResult ppuTick(int cycles) {
        return PPUTickResult.NORMAL;
    }

    public void writeData(int gameCodeAddress, byte[] gameCode) {
        System.arraycopy(gameCode, 0, ram, gameCodeAddress, gameCode.length);
    }

    private int toAddress(int address) {
        return address % ram.length;
    }
}
