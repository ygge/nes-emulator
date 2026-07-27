package nu.ygge.nes.emulator.bus;

import lombok.Getter;
import nu.ygge.nes.emulator.apu.APU;
import nu.ygge.nes.emulator.cpu.CPURAM;
import nu.ygge.nes.emulator.input.Controller;
import nu.ygge.nes.emulator.mapper.Mapper;
import nu.ygge.nes.emulator.mapper.NromMapper;
import nu.ygge.nes.emulator.ppu.Frame;
import nu.ygge.nes.emulator.ppu.Mirroring;
import nu.ygge.nes.emulator.ppu.PPU;
import nu.ygge.nes.emulator.state.StateReader;
import nu.ygge.nes.emulator.state.StateWriter;

@Getter
public class EmulatorBus implements Bus {

    private static final int CARTRIDGE_START = 0x4020;
    private static final int OAM_DMA_PAGE_SIZE = 256;
    private static final int OAM_DMA_CYCLES = 513;
    private static final int DMC_DMA_CYCLES = 4;
    private static final int APU_STATUS = 0x4015;
    private static final int CONTROLLER_ONE = 0x4016;
    private static final int APU_FRAME_COUNTER = 0x4017;

    private final CPURAM cpuRam;
    private final PPU ppu;
    private final APU apu;
    private final Mapper mapper;
    private final Controller controller = new Controller();
    private final byte[] dmaPage = new byte[OAM_DMA_PAGE_SIZE];
    private int stallCycles;

    /**
     * Builds a bus around a bare program ROM, wrapping it in the simplest mapper. Handy for tests and
     * for the many cartridges that need no banking at all.
     */
    public EmulatorBus(byte[] prgRom) {
        this(new NromMapper(prgRom, new byte[0], Mirroring.HORIZONTAL));
    }

    public EmulatorBus(Mapper mapper) {
        this.mapper = mapper;
        cpuRam = new CPURAM();
        ppu = new PPU();
        ppu.reset(mapper);
        // the DMC channel fetches samples straight from CPU memory, stalling the CPU as it does
        apu = new APU(address -> {
            stallCycles += DMC_DMA_CYCLES;
            return read(address);
        });
    }

    @Override
    public byte read(int address) {
        if (address < 0x2000) {
            // mirroring for CPU RAM
            return cpuRam.read(address & 0x7FF);
        } else if (address < 0x4000) {
            // mirroring for PPU registers
            return readRegister(0x2000 + (address & 7));
        } else if (address == APU_STATUS) {
            return apu.readStatus();
        } else if (address == CONTROLLER_ONE) {
            return controller.read();
        } else if (address < CARTRIDGE_START) {
            // the remaining registers, including the second controller port, are not driven yet
            return 0;
        }
        return mapper.cpuRead(address);
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
        } else if (address == CONTROLLER_ONE) {
            // the strobe is shared by both controller ports
            controller.write(data);
        } else if (address <= 0x4013 || address == APU_STATUS || address == APU_FRAME_COUNTER) {
            apu.writeRegister(address, data);
        } else if (address >= CARTRIDGE_START) {
            mapper.cpuWrite(address, data);
        }
    }

    @Override
    public PPUTickResult ppuTick(int cycles) {
        return ppu.tick(cycles);
    }

    @Override
    public boolean apuTick(int cycles) {
        return apu.tick(cycles);
    }

    @Override
    public boolean isMapperIrqAsserted() {
        return mapper.isIrqAsserted();
    }

    @Override
    public void saveState(StateWriter writer) {
        cpuRam.saveState(writer);
        ppu.saveState(writer);
        apu.saveState(writer);
        controller.saveState(writer);
        mapper.saveState(writer);
        writer.writeInt(stallCycles);
    }

    @Override
    public void loadState(StateReader reader) {
        cpuRam.loadState(reader);
        ppu.loadState(reader);
        apu.loadState(reader);
        controller.loadState(reader);
        mapper.loadState(reader);
        stallCycles = reader.readInt();
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
