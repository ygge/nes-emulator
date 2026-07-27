package nu.ygge.nes.emulator.state;

import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * The counterpart to {@link StateWriter}: each component reads its state back out in exactly the same
 * order it wrote it. Reading past the end or hitting a mismatched block length signals a corrupt or
 * incompatible snapshot, which surfaces as an unchecked exception the caller can catch.
 */
public final class StateReader {

    private final DataInputStream in;

    public StateReader(byte[] data) {
        this.in = new DataInputStream(new ByteArrayInputStream(data));
    }

    public boolean readBoolean() {
        return supply(in::readBoolean);
    }

    public int readUnsignedByte() {
        return supply(in::readUnsignedByte);
    }

    public int readInt() {
        return supply(in::readInt);
    }

    public long readLong() {
        return supply(in::readLong);
    }

    public float readFloat() {
        return supply(in::readFloat);
    }

    public double readDouble() {
        return supply(in::readDouble);
    }

    /**
     * Reads a length prefixed block straight into the given array. The length has to match, which
     * doubles as a guard against restoring a snapshot that belongs to a different cartridge.
     */
    public void readBytes(byte[] target) {
        var length = readInt();
        if (length != target.length) {
            throw new IllegalStateException(
                    "Snapshot block length " + length + " does not match expected " + target.length);
        }
        supply(() -> {
            in.readFully(target);
            return null;
        });
    }

    private <T> T supply(IoSupplier<T> supplier) {
        try {
            return supplier.get();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private interface IoSupplier<T> {
        T get() throws IOException;
    }
}
