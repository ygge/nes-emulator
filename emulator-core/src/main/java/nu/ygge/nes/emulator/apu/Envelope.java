package nu.ygge.nes.emulator.apu;

/**
 * The volume envelope shared by the pulse and noise channels. It either passes a constant volume
 * through, or generates a saw-shaped decay that is clocked once per quarter frame and can be set to
 * loop back to full volume.
 */
public class Envelope {

    private static final int MAX_VOLUME = 15;

    private boolean start;
    private boolean loop;
    private boolean constantVolume;
    private int dividerPeriod;
    private int divider;
    private int decayLevel;

    /**
     * The lower five bits of $4000/$4004/$400C carry the volume/period together with the constant
     * volume flag.
     */
    public void write(byte data) {
        constantVolume = (data & 0x10) != 0;
        dividerPeriod = data & 0x0f;
    }

    public void setLoop(boolean loop) {
        this.loop = loop;
    }

    public void restart() {
        start = true;
    }

    public void clock() {
        if (start) {
            start = false;
            decayLevel = MAX_VOLUME;
            divider = dividerPeriod;
        } else if (divider > 0) {
            --divider;
        } else {
            divider = dividerPeriod;
            if (decayLevel > 0) {
                --decayLevel;
            } else if (loop) {
                decayLevel = MAX_VOLUME;
            }
        }
    }

    public int getVolume() {
        return constantVolume ? dividerPeriod : decayLevel;
    }
}
