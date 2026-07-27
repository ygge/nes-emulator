package nu.ygge.nes.emulator.state;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;

/**
 * A small append only sink that each component writes its state into when a snapshot is taken. It is
 * backed by an in-memory buffer, so the checked exceptions the underlying streams declare can never
 * actually fire; they are rewrapped as unchecked ones to keep the component code tidy.
 */
public final class StateWriter {

    private final ByteArrayOutputStream buffer = new ByteArrayOutputStream();
    private final DataOutputStream out = new DataOutputStream(buffer);

    public void writeBoolean(boolean value) {
        run(() -> out.writeBoolean(value));
    }

    public void writeByte(int value) {
        run(() -> out.writeByte(value));
    }

    public void writeInt(int value) {
        run(() -> out.writeInt(value));
    }

    public void writeLong(long value) {
        run(() -> out.writeLong(value));
    }

    public void writeFloat(float value) {
        run(() -> out.writeFloat(value));
    }

    public void writeDouble(double value) {
        run(() -> out.writeDouble(value));
    }

    /**
     * Writes a length prefixed block of raw bytes, so the reader can check it against the array it is
     * restoring into.
     */
    public void writeBytes(byte[] value) {
        writeInt(value.length);
        run(() -> out.write(value));
    }

    public byte[] toByteArray() {
        run(out::flush);
        return buffer.toByteArray();
    }

    private void run(IoAction action) {
        try {
            action.run();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }

    private interface IoAction {
        void run() throws IOException;
    }
}
