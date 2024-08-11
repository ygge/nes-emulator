package nu.ygge.nes.emulator.bus;

import lombok.Getter;
import nu.ygge.nes.emulator.cpu.CPURAM;
import nu.ygge.nes.emulator.exception.NESException;
import nu.ygge.nes.emulator.ppu.Frame;
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
        // TODO: handle writing to memory that should not be read, today memory is always read first
        if (address < 0x2000) {
            // mirroring for CPU RAM
            return cpuRam.read(address & 0x7FF);
        } else if (address == 0x2000 || address == 0x2001 || address == 0x2003
                || address == 0x2005 || address == 0x2006 || address == 0x4014) {
            //throw new NESException(String.format("Cannot read from write only registers %d", address));
            return 0;
        } else if (address == 0x2002) {
            return ppu.readStatus();
        } else if (address == 0x2004) {
            return ppu.readOamData();
        } else if (address == 0x2007) {
            return ppu.read();
        } else if (address >= 0x2008 && address <  0x4000) {
            // mirroring for PPU registers
            var mirroredAddress = address & 0b00100000_00000111;
            return read(mirroredAddress);
        } else if (address <= 0x4015) {
            // TODO: handle APU
            return 0;
        } else if (address == 0x4016 || address == 0x4017) {
            // TODO: handle input
            return 0;
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
        } else if (address == 0x2001) {
            ppu.writeToMaskRegister(data);
        } else if (address == 0x2002) {
            throw new NESException("Cannot write to status registers 2002");
        } else if (address == 0x2003) {
            ppu.writeToOamAddress(data);
        } else if (address == 0x2004) {
            ppu.writeToOamData(data);
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
            write(address, data);
        } else if (address <= 0x4013 || address == 0x4015) {
            // TODO: handle APU
        } else if (address == 0x4014) {
            // https://wiki.nesdev.com/w/index.php/PPU_programmer_reference#OAM_DMA_.28.244014.29_.3E_write
            // TODO: implement this
        } else if (address == 0x4016 || address == 0x4017) {
            // TODO: handle input
        } else {
            throw new NESException(String.format("Illegal write address access for %d", address));
        }
    }

    @Override
    public PPUTickResult ppuTick(int cycles) {
        return ppu.tick(cycles);
    }

    @Override
    public Frame getFrame() {
        return ppu.getFrame();
    }
}
