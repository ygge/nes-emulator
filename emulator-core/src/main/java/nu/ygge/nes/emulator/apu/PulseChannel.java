package nu.ygge.nes.emulator.apu;

import nu.ygge.nes.emulator.state.StateReader;
import nu.ygge.nes.emulator.state.StateWriter;

/**
 * One of the two square wave channels. A pulse is a duty-cycled square wave whose pitch comes from
 * an eleven bit timer, whose volume comes from an {@link Envelope}, and which can slide in pitch
 * through a {@link Sweep} and be cut off by a {@link LengthCounter}.
 */
public class PulseChannel {

    private static final int[][] DUTY_SEQUENCES = {
            {0, 1, 0, 0, 0, 0, 0, 0},
            {0, 1, 1, 0, 0, 0, 0, 0},
            {0, 1, 1, 1, 1, 0, 0, 0},
            {1, 0, 0, 1, 1, 1, 1, 1}
    };

    private final Envelope envelope = new Envelope();
    private final Sweep sweep;
    private final LengthCounter lengthCounter = new LengthCounter();

    private int duty;
    private int timerPeriod;
    private int timer;
    private int sequenceStep;

    public PulseChannel(boolean firstChannel) {
        this.sweep = new Sweep(firstChannel);
    }

    public void saveState(StateWriter writer) {
        writer.writeInt(duty);
        writer.writeInt(timerPeriod);
        writer.writeInt(timer);
        writer.writeInt(sequenceStep);
        envelope.saveState(writer);
        sweep.saveState(writer);
        lengthCounter.saveState(writer);
    }

    public void loadState(StateReader reader) {
        duty = reader.readInt();
        timerPeriod = reader.readInt();
        timer = reader.readInt();
        sequenceStep = reader.readInt();
        envelope.loadState(reader);
        sweep.loadState(reader);
        lengthCounter.loadState(reader);
    }

    public void writeControl(byte data) {
        duty = (data >> 6) & 0x03;
        var halt = (data & 0x20) != 0;
        lengthCounter.setHalt(halt);
        envelope.setLoop(halt);
        envelope.write(data);
    }

    public void writeSweep(byte data) {
        sweep.write(data);
    }

    public void writeTimerLow(byte data) {
        timerPeriod = (timerPeriod & 0x700) | (data & 0xff);
    }

    public void writeTimerHigh(byte data) {
        timerPeriod = (timerPeriod & 0x0ff) | ((data & 0x07) << 8);
        lengthCounter.load((data >> 3) & 0x1f);
        sequenceStep = 0;
        envelope.restart();
    }

    public void setEnabled(boolean enabled) {
        lengthCounter.setEnabled(enabled);
    }

    public boolean isActive() {
        return lengthCounter.isActive();
    }

    public int getTimerPeriod() {
        return timerPeriod;
    }

    public void setTimerPeriod(int timerPeriod) {
        this.timerPeriod = timerPeriod;
    }

    /**
     * Clocked once per APU cycle, i.e. every other CPU cycle.
     */
    public void clockTimer() {
        if (timer == 0) {
            timer = timerPeriod;
            sequenceStep = (sequenceStep + 1) & 7;
        } else {
            --timer;
        }
    }

    public void clockEnvelope() {
        envelope.clock();
    }

    public void clockLengthAndSweep() {
        lengthCounter.clock();
        sweep.clock(this);
    }

    public int output() {
        sweep.updateMuting(timerPeriod);
        if (sweep.isMuting() || !lengthCounter.isActive() || DUTY_SEQUENCES[duty][sequenceStep] == 0) {
            return 0;
        }
        return envelope.getVolume();
    }
}
