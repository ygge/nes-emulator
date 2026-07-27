package nu.ygge.nes.emulator.apu;

/**
 * The noise channel produces pseudo random tones from a fifteen bit shift register. A short mode
 * taps a different bit so that the sequence repeats far sooner, giving a more tonal, buzzy sound.
 * Like the pulse channels it has an {@link Envelope} for volume and a {@link LengthCounter} to gate
 * it.
 */
public class NoiseChannel {

    private static final int[] PERIOD_TABLE = {
            4, 8, 16, 32, 64, 96, 128, 160, 202, 254, 380, 508, 762, 1016, 2034, 4068
    };

    private final Envelope envelope = new Envelope();
    private final LengthCounter lengthCounter = new LengthCounter();

    private boolean mode;
    private int timerPeriod;
    private int timer;
    private int shiftRegister = 1;

    public void writeControl(byte data) {
        var halt = (data & 0x20) != 0;
        lengthCounter.setHalt(halt);
        envelope.setLoop(halt);
        envelope.write(data);
    }

    public void writePeriod(byte data) {
        mode = (data & 0x80) != 0;
        timerPeriod = PERIOD_TABLE[data & 0x0f];
    }

    public void writeLength(byte data) {
        lengthCounter.load((data >> 3) & 0x1f);
        envelope.restart();
    }

    public void setEnabled(boolean enabled) {
        lengthCounter.setEnabled(enabled);
    }

    public boolean isActive() {
        return lengthCounter.isActive();
    }

    /**
     * Clocked once per APU cycle. On each expiry a feedback bit is fed back into the top of the
     * shift register, from either bit one or bit six depending on the mode.
     */
    public void clockTimer() {
        if (timer == 0) {
            timer = timerPeriod;
            var tap = mode ? (shiftRegister >> 6) : (shiftRegister >> 1);
            var feedback = (shiftRegister ^ tap) & 1;
            shiftRegister = (shiftRegister >> 1) | (feedback << 14);
        } else {
            --timer;
        }
    }

    public void clockEnvelope() {
        envelope.clock();
    }

    public void clockLength() {
        lengthCounter.clock();
    }

    public int output() {
        if ((shiftRegister & 1) != 0 || !lengthCounter.isActive()) {
            return 0;
        }
        return envelope.getVolume();
    }
}
