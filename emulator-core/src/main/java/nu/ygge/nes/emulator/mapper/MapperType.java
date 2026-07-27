package nu.ygge.nes.emulator.mapper;

/**
 * Identifiers stamped at the front of a mapper's saved state so that a snapshot cannot be restored
 * into a cartridge of the wrong kind, which would otherwise misread the bytes that follow.
 */
final class MapperType {

    static final int NROM = 0;
    static final int MMC1 = 1;
    static final int MMC3 = 4;

    private MapperType() {
    }

    static void verify(int actual, int expected) {
        if (actual != expected) {
            throw new IllegalStateException(
                    "Snapshot belongs to a different mapper (found " + actual + ", expected " + expected + ")");
        }
    }
}
