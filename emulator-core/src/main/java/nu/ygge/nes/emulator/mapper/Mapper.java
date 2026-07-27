package nu.ygge.nes.emulator.mapper;

import nu.ygge.nes.emulator.ppu.Mirroring;

/**
 * A cartridge mapper. Real cartridges carry a chip that decides which slice of a larger ROM is
 * visible to the console at any moment, and often controls the name table mirroring and its own
 * interrupt too. The mapper therefore owns both the program ROM the CPU sees above $6000 and the
 * character ROM the PPU reads its tiles from.
 */
public interface Mapper {

    /**
     * A read from the CPU address space, only ever called for addresses at or above $4020.
     */
    byte cpuRead(int address);

    /**
     * A write from the CPU address space, at or above $4020. This is also how the CPU reconfigures
     * the mapper's bank registers.
     */
    void cpuWrite(int address, byte data);

    /**
     * A read from the PPU pattern tables, in the range $0000-$1FFF.
     */
    byte ppuRead(int address);

    /**
     * A write to the PPU pattern tables. Only cartridges with character RAM react to it.
     */
    void ppuWrite(int address, byte data);

    Mirroring getMirroring();

    /**
     * Whether the mapper is currently holding the CPU's interrupt line low.
     */
    default boolean isIrqAsserted() {
        return false;
    }

    /**
     * Called by the PPU once per rendered scanline, which is how counters such as the MMC3's keep
     * track of the beam position.
     */
    default void onScanline() {
    }
}
