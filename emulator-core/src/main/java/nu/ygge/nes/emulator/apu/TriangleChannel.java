package nu.ygge.nes.emulator.apu;

import nu.ygge.nes.emulator.state.StateReader;
import nu.ygge.nes.emulator.state.StateWriter;

/**
 * The triangle channel walks a fixed 32-step staircase up and down, producing a triangle wave an
 * octave below a pulse channel of the same period. It has no volume control; a linear counter and a
 * length counter simply gate whether it is running.
 */
public class TriangleChannel {

    private static final int[] SEQUENCE = {
            15, 14, 13, 12, 11, 10, 9, 8, 7, 6, 5, 4, 3, 2, 1, 0,
            0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 10, 11, 12, 13, 14, 15
    };

    private final LengthCounter lengthCounter = new LengthCounter();

    private int timerPeriod;
    private int timer;
    private int sequenceStep;
    private boolean control;
    private int linearCounterPeriod;
    private int linearCounter;
    private boolean linearCounterReload;

    public void saveState(StateWriter writer) {
        writer.writeInt(timerPeriod);
        writer.writeInt(timer);
        writer.writeInt(sequenceStep);
        writer.writeBoolean(control);
        writer.writeInt(linearCounterPeriod);
        writer.writeInt(linearCounter);
        writer.writeBoolean(linearCounterReload);
        lengthCounter.saveState(writer);
    }

    public void loadState(StateReader reader) {
        timerPeriod = reader.readInt();
        timer = reader.readInt();
        sequenceStep = reader.readInt();
        control = reader.readBoolean();
        linearCounterPeriod = reader.readInt();
        linearCounter = reader.readInt();
        linearCounterReload = reader.readBoolean();
        lengthCounter.loadState(reader);
    }

    public void writeLinearCounter(byte data) {
        control = (data & 0x80) != 0;
        lengthCounter.setHalt(control);
        linearCounterPeriod = data & 0x7f;
    }

    public void writeTimerLow(byte data) {
        timerPeriod = (timerPeriod & 0x700) | (data & 0xff);
    }

    public void writeTimerHigh(byte data) {
        timerPeriod = (timerPeriod & 0x0ff) | ((data & 0x07) << 8);
        lengthCounter.load((data >> 3) & 0x1f);
        linearCounterReload = true;
    }

    public void setEnabled(boolean enabled) {
        lengthCounter.setEnabled(enabled);
    }

    public boolean isActive() {
        return lengthCounter.isActive();
    }

    /**
     * Clocked every CPU cycle, twice as often as the pulse channels, which is what puts the triangle
     * an octave lower for the same period.
     */
    public void clockTimer() {
        if (timer == 0) {
            timer = timerPeriod;
            if (linearCounter > 0 && lengthCounter.isActive()) {
                sequenceStep = (sequenceStep + 1) & 31;
            }
        } else {
            --timer;
        }
    }

    public void clockLinearCounter() {
        if (linearCounterReload) {
            linearCounter = linearCounterPeriod;
        } else if (linearCounter > 0) {
            --linearCounter;
        }
        if (!control) {
            linearCounterReload = false;
        }
    }

    public void clockLength() {
        lengthCounter.clock();
    }

    public int output() {
        return SEQUENCE[sequenceStep];
    }
}
