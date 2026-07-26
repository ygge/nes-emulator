package nu.ygge.nes.emulator.bus;

import lombok.Getter;
import nu.ygge.nes.emulator.cpu.CPURAM;
import nu.ygge.nes.emulator.ppu.Frame;
import nu.ygge.nes.emulator.ppu.PPU;

@Getter
public class EmulatorBus implements Bus {

    private static final int PRG_ROM_START = 0x8000;
    private static final int PRG_RAM_START = 0x6000;
    private static final int PRG_RAM_SIZE = 0x2000;
    private static final int OAM_DMA_PAGE_SIZE = 256;
    private static final int OAM_DMA_CYCLES = 513;

    private final CPURAM cpuRam;
    private final PPU ppu;
    private final byte[] prgRom;
    private final byte[] prgRam = new byte[PRG_RAM_SIZE];
    private final byte[] dmaPage = new byte[OAM_DMA_PAGE_SIZE];
    private int stallCycles;

    public EmulatorBus(byte[] prgRom) {
        cpuRam = new CPURAM();
        ppu = new PPU();
        this.prgRom = prgRom.length == 0 ? new byte[PRG_ROM_START] : prgRom;
    }

    @Override
    public byte read(int address) {
        if (address < 0x2000) {
            // mirroring for CPU RAM
            return cpuRam.read(address & 0x7FF);
        } else if (address < 0x4000) {
            // mirroring for PPU registers
            return readRegister(0x2000 + (address & 7));
        } else if (address < PRG_RAM_START) {
            // TODO: handle APU and input
            return 0;
        } else if (address < PRG_ROM_START) {
            return prgRam[address - PRG_RAM_START];
        }
        // a cartridge with a single 16k bank has it mapped into both halves of the ROM area
        return prgRom[(address - PRG_ROM_START) % prgRom.length];
    }

    @Override
    public void write(int address, byte data) {
        if (address < 0x2000) {
            // mirroring for CPU RAM
            cpuRam.write(address & 0x7FF, data);
        } else if (address < 0x4000) {
            // mirroring for PPU registers
            writeRegister(0x2000 + (address & 7), data);
        } else if (address == 0x4014) {
            performOamDma(data);
        } else if (address >= PRG_RAM_START && address < PRG_ROM_START) {
            prgRam[address - PRG_RAM_START] = data;
        }
        // APU, input and writes to cartridge ROM are ignored for now
    }

    @Override
    public PPUTickResult ppuTick(int cycles) {
        return ppu.tick(cycles);
    }

    @Override
    public Frame getFrame() {
        return ppu.getFrame();
    }

    @Override
    public int consumeStallCycles() {
        var result = stallCycles;
        stallCycles = 0;
        return result;
    }

    private byte readRegister(int address) {
        return switch (address) {
            case 0x2002 -> ppu.readStatus();
            case 0x2004 -> ppu.readOamData();
            case 0x2007 -> ppu.read();
            // the other registers are write only and would return whatever is left on the bus
            default -> 0;
        };
    }

    private void writeRegister(int address, byte data) {
        switch (address) {
            case 0x2000 -> ppu.writeToControlRegister(data);
            case 0x2001 -> ppu.writeToMaskRegister(data);
            case 0x2003 -> ppu.writeToOamAddress(data);
            case 0x2004 -> ppu.writeToOamData(data);
            case 0x2005 -> ppu.writeToScrollRegister(data);
            case 0x2006 -> ppu.writeToAddressRegister(data);
            case 0x2007 -> ppu.write(data);
            default -> { /* the status register is read only */ }
        }
    }

    private void performOamDma(byte data) {
        var base = (data & 0xff) << 8;
        for (int i = 0; i < OAM_DMA_PAGE_SIZE; ++i) {
            dmaPage[i] = read(base + i);
        }
        ppu.writeOamDma(dmaPage);
        stallCycles += OAM_DMA_CYCLES;
    }
}
