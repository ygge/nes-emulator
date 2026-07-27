package nu.ygge.nes.emulator.apu;

import nu.ygge.nes.emulator.state.StateReader;
import nu.ygge.nes.emulator.state.StateWriter;

/**
 * The Audio Processing Unit. It owns the five sound channels, the frame counter that clocks their
 * envelopes and length counters, and the mixer that folds everything down into a single stream. The
 * chip runs at the CPU clock, so it is ticked one CPU cycle at a time, and it resamples its output
 * down to a host friendly sample rate as it goes.
 */
public class APU {

    private static final double CPU_CLOCK_HZ = 1_789_773.0;
    private static final int DEFAULT_SAMPLE_RATE = 44_100;
    private static final int STEP1 = 7457;
    private static final int STEP2 = 14913;
    private static final int STEP3 = 22371;
    private static final int STEP4 = 29829;
    private static final int STEP5 = 37281;

    private final PulseChannel pulse1 = new PulseChannel(true);
    private final PulseChannel pulse2 = new PulseChannel(false);
    private final TriangleChannel triangle = new TriangleChannel();
    private final NoiseChannel noise = new NoiseChannel();
    private final DmcChannel dmc;

    private final float[] pulseTable = new float[31];
    private final float[] tndTable = new float[203];

    private final double cyclesPerSample;
    private final short[] sampleBuffer;
    private int sampleCount;
    private double sampleAccumulator;

    private boolean fiveStepMode;
    private boolean irqInhibit;
    private boolean frameIrq;
    private int frameCycle;
    private boolean evenCycle;

    private float highPass90, highPass90Prev;
    private float highPass440, highPass440Prev;
    private float lowPass;

    public APU(DmcMemoryReader memoryReader) {
        this(memoryReader, DEFAULT_SAMPLE_RATE);
    }

    public APU(DmcMemoryReader memoryReader, int sampleRate) {
        this.dmc = new DmcChannel(memoryReader);
        this.cyclesPerSample = CPU_CLOCK_HZ / sampleRate;
        // a tenth of a second of headroom is far more than a single frame ever produces
        this.sampleBuffer = new short[sampleRate / 10];
        buildMixerTables();
    }

    /**
     * Captures the whole chip but the outgoing sample buffer, which is transient and simply refills
     * as emulation continues.
     */
    public void saveState(StateWriter writer) {
        pulse1.saveState(writer);
        pulse2.saveState(writer);
        triangle.saveState(writer);
        noise.saveState(writer);
        dmc.saveState(writer);
        writer.writeBoolean(fiveStepMode);
        writer.writeBoolean(irqInhibit);
        writer.writeBoolean(frameIrq);
        writer.writeInt(frameCycle);
        writer.writeBoolean(evenCycle);
        writer.writeDouble(sampleAccumulator);
        writer.writeFloat(highPass90);
        writer.writeFloat(highPass90Prev);
        writer.writeFloat(highPass440);
        writer.writeFloat(highPass440Prev);
        writer.writeFloat(lowPass);
    }

    public void loadState(StateReader reader) {
        pulse1.loadState(reader);
        pulse2.loadState(reader);
        triangle.loadState(reader);
        noise.loadState(reader);
        dmc.loadState(reader);
        fiveStepMode = reader.readBoolean();
        irqInhibit = reader.readBoolean();
        frameIrq = reader.readBoolean();
        frameCycle = reader.readInt();
        evenCycle = reader.readBoolean();
        sampleAccumulator = reader.readDouble();
        highPass90 = reader.readFloat();
        highPass90Prev = reader.readFloat();
        highPass440 = reader.readFloat();
        highPass440Prev = reader.readFloat();
        lowPass = reader.readFloat();
    }

    public void writeRegister(int address, byte data) {
        switch (address) {
            case 0x4000 -> pulse1.writeControl(data);
            case 0x4001 -> pulse1.writeSweep(data);
            case 0x4002 -> pulse1.writeTimerLow(data);
            case 0x4003 -> pulse1.writeTimerHigh(data);
            case 0x4004 -> pulse2.writeControl(data);
            case 0x4005 -> pulse2.writeSweep(data);
            case 0x4006 -> pulse2.writeTimerLow(data);
            case 0x4007 -> pulse2.writeTimerHigh(data);
            case 0x4008 -> triangle.writeLinearCounter(data);
            case 0x400a -> triangle.writeTimerLow(data);
            case 0x400b -> triangle.writeTimerHigh(data);
            case 0x400c -> noise.writeControl(data);
            case 0x400e -> noise.writePeriod(data);
            case 0x400f -> noise.writeLength(data);
            case 0x4010 -> dmc.writeControl(data);
            case 0x4011 -> dmc.writeDirectLoad(data);
            case 0x4012 -> dmc.writeSampleAddress(data);
            case 0x4013 -> dmc.writeSampleLength(data);
            case 0x4015 -> writeStatus(data);
            case 0x4017 -> writeFrameCounter(data);
            default -> { /* $4009 and $400d are unused, everything else is not the APU */ }
        }
    }

    public byte readStatus() {
        var status = 0;
        if (pulse1.isActive()) status |= 0x01;
        if (pulse2.isActive()) status |= 0x02;
        if (triangle.isActive()) status |= 0x04;
        if (noise.isActive()) status |= 0x08;
        if (dmc.isActive()) status |= 0x10;
        if (frameIrq) status |= 0x40;
        if (dmc.isIrqPending()) status |= 0x80;
        // reading the status acknowledges the frame interrupt but leaves the DMC one alone
        frameIrq = false;
        return (byte) status;
    }

    /**
     * Advances the APU by a batch of CPU cycles and reports whether an interrupt line is being held
     * low afterwards, which the CPU polls to decide on an IRQ.
     */
    public boolean tick(int cpuCycles) {
        for (int i = 0; i < cpuCycles; ++i) {
            clockCpuCycle();
        }
        return isIrqAsserted();
    }

    public boolean isIrqAsserted() {
        return frameIrq || dmc.isIrqPending();
    }

    /**
     * Copies the samples produced since the last call into the destination and returns how many
     * were written. Any that do not fit are kept for the next call.
     */
    public int readSamples(short[] dest) {
        var count = Math.min(sampleCount, dest.length);
        System.arraycopy(sampleBuffer, 0, dest, 0, count);
        var remaining = sampleCount - count;
        if (remaining > 0) {
            System.arraycopy(sampleBuffer, count, sampleBuffer, 0, remaining);
        }
        sampleCount = remaining;
        return count;
    }

    private void writeStatus(byte data) {
        pulse1.setEnabled((data & 0x01) != 0);
        pulse2.setEnabled((data & 0x02) != 0);
        triangle.setEnabled((data & 0x04) != 0);
        noise.setEnabled((data & 0x08) != 0);
        dmc.setEnabled((data & 0x10) != 0);
    }

    private void writeFrameCounter(byte data) {
        fiveStepMode = (data & 0x80) != 0;
        irqInhibit = (data & 0x40) != 0;
        if (irqInhibit) {
            frameIrq = false;
        }
        frameCycle = 0;
        // switching to the five step sequence immediately clocks everything once
        if (fiveStepMode) {
            clockQuarterFrame();
            clockHalfFrame();
        }
    }

    private void clockCpuCycle() {
        triangle.clockTimer();
        dmc.clockTimer();
        if (evenCycle) {
            pulse1.clockTimer();
            pulse2.clockTimer();
            noise.clockTimer();
        }
        evenCycle = !evenCycle;
        clockFrameCounter();

        sampleAccumulator += 1.0;
        if (sampleAccumulator >= cyclesPerSample) {
            sampleAccumulator -= cyclesPerSample;
            emitSample();
        }
    }

    private void clockFrameCounter() {
        ++frameCycle;
        switch (frameCycle) {
            case STEP1, STEP3 -> clockQuarterFrame();
            case STEP2 -> {
                clockQuarterFrame();
                clockHalfFrame();
            }
            case STEP4 -> {
                if (!fiveStepMode) {
                    clockQuarterFrame();
                    clockHalfFrame();
                    if (!irqInhibit) {
                        frameIrq = true;
                    }
                    frameCycle = 0;
                }
            }
            case STEP5 -> {
                if (fiveStepMode) {
                    clockQuarterFrame();
                    clockHalfFrame();
                    frameCycle = 0;
                }
            }
            default -> { /* an ordinary cycle with nothing to clock */ }
        }
    }

    private void clockQuarterFrame() {
        pulse1.clockEnvelope();
        pulse2.clockEnvelope();
        noise.clockEnvelope();
        triangle.clockLinearCounter();
    }

    private void clockHalfFrame() {
        pulse1.clockLengthAndSweep();
        pulse2.clockLengthAndSweep();
        triangle.clockLength();
        noise.clockLength();
    }

    private void emitSample() {
        var pulseOut = pulseTable[pulse1.output() + pulse2.output()];
        var tndOut = tndTable[3 * triangle.output() + 2 * noise.output() + dmc.output()];
        var sample = filter(pulseOut + tndOut);
        var value = (int) (sample * Short.MAX_VALUE);
        value = Math.max(Short.MIN_VALUE, Math.min(Short.MAX_VALUE, value));
        if (sampleCount < sampleBuffer.length) {
            sampleBuffer[sampleCount++] = (short) value;
        }
    }

    /**
     * Approximates the RC filters that sit on the console's audio output: two high passes to strip
     * the large DC offset the mixer carries, and a low pass to soften the highest frequencies.
     */
    private float filter(float input) {
        highPass90 = 0.996f * (highPass90 + input - highPass90Prev);
        highPass90Prev = input;
        highPass440 = 0.983f * (highPass440 + highPass90 - highPass440Prev);
        highPass440Prev = highPass90;
        lowPass += 0.815f * (highPass440 - lowPass);
        return lowPass;
    }

    private void buildMixerTables() {
        pulseTable[0] = 0f;
        for (int i = 1; i < pulseTable.length; ++i) {
            pulseTable[i] = (float) (95.52 / (8128.0 / i + 100));
        }
        tndTable[0] = 0f;
        for (int i = 1; i < tndTable.length; ++i) {
            tndTable[i] = (float) (163.67 / (24329.0 / i + 100));
        }
    }
}
