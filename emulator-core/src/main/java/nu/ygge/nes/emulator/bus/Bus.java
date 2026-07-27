package nu.ygge.nes.emulator.bus;

import nu.ygge.nes.emulator.apu.APU;
import nu.ygge.nes.emulator.input.Controller;
import nu.ygge.nes.emulator.ppu.Frame;

public interface Bus {

    byte read(int address);
    void write(int address, byte data);

    /**
     * The controller plugged into the first port.
     */
    Controller getController();

    PPUTickResult ppuTick(int cycles);
    default Frame getFrame() {
        return null;
    }

    /**
     * Advances the audio hardware by the given number of CPU cycles and reports whether it is
     * currently holding the IRQ line low.
     */
    default boolean apuTick(int cycles) {
        return false;
    }

    /**
     * Whether the cartridge mapper is currently holding the CPU's interrupt line low.
     */
    default boolean isMapperIrqAsserted() {
        return false;
    }

    /**
     * The audio processing unit, or {@code null} on buses without sound.
     */
    default APU getApu() {
        return null;
    }

    /**
     * Extra CPU cycles spent on DMA since the last call, which the CPU has to be stalled for.
     */
    default int consumeStallCycles() {
        return 0;
    }
}
