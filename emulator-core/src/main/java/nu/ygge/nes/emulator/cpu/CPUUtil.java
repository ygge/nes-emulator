package nu.ygge.nes.emulator.cpu;

public final class CPUUtil {

    private CPUUtil() {
    }

    public static int toInt(byte value) {
        int v = value;
        if (v < 0) {
            v += 256;
        }
        return v;
    }

    public static int toAddress(byte eb1, byte eb2) {
        int address = toInt(eb1);
        return (address << 8) | toInt(eb2);
    }
}
