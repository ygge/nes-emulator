package nu.ygge.nes.emulator.apu;

/**
 * Lets the DMC channel fetch its sample bytes straight from CPU memory. The implementation is also
 * where the CPU gets stalled for the few cycles such a fetch steals.
 */
@FunctionalInterface
public interface DmcMemoryReader {

    byte read(int address);
}
