package nu.ygge.nes.emulator.bus;

public interface Bus {
    byte read(int address);
    void write(int address, byte data);
}
