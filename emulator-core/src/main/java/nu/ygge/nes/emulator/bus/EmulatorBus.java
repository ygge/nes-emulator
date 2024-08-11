package nu.ygge.nes.emulator.bus;

import lombok.Getter;
import nu.ygge.nes.emulator.cpu.CPURAM;
import nu.ygge.nes.emulator.exception.NESException;
import nu.ygge.nes.emulator.ppu.PPU;

@Getter
public class EmulatorBus implements Bus {

    private final CPURAM cpuRam;
    private final PPU ppu;
    private byte[] prgRom;

    public EmulatorBus(byte[] prgRom) {
        cpuRam = new CPURAM();
        ppu = new PPU();
        this.prgRom = prgRom;
    }

    @Override
    public byte read(int address) {
        if (address < 0x2000) {
            // mirroring for CPU RAM
            return cpuRam.read(address & 0x7FF);
        } else if (address == 0x2000 || address == 0x2001 || address == 0x2003
                || address == 0x2005 || address == 0x2006 || address == 0x4014) {
            throw new NESException(String.format("Cannot read from write only registers %d", address));
        } else if (address == 0x2007) {
            return ppu.read();
        } else if (address >= 0x2008 && address <  0x4000) {
            // mirroring for PPU registers
            var mirroredAddress = address & 0b00100000_00000111;
            return read(mirroredAddress);
        } else if (address >= 0x8000) {
            return prgRom[address - 0x8000];
        }
        throw new NESException(String.format("Illegal read address access for %d", address));
    }

    @Override
    public void write(int address, byte data) {
        if (address < 0x2000) {
            // mirroring for CPU RAM
            cpuRam.write(address & 0x7FF, data);
        } else if (address == 0x2000) {
            ppu.writeToControlRegister(data);
        } else if (address == 0x2006) {
            ppu.writeToAddressRegister(data);
        } else if (address == 0x2007) {
            ppu.write(data);
        } else if (address >= 0x2008 && address <  0x4000) {
            // mirroring for PPU registers
            var mirroredAddress = address & 0b00100000_00000111;
            write(mirroredAddress, data);
        } else if (address < 0x4000) {
            // mirroring for PPU registers
            address &= 0x2007;
        } else {
            throw new NESException(String.format("Illegal write address access for %d", address));
        }
    }
}
