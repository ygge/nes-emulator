package nu.ygge.nes.emulator;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public final class FileLogger {

    private static final boolean LOG = false;
    private static final BufferedOutputStream OUT;

    static {
        if (LOG) {
            try {
                OUT = new BufferedOutputStream(new FileOutputStream("smb7_mine.log"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } else {
            OUT = null;
        }
    }

    private FileLogger() {}

    public static void log(String message) {
        if (!LOG) {
            return;
        }
        try {
            OUT.write((message + "\n").getBytes(StandardCharsets.UTF_8));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
}
