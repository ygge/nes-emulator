package nu.ygge.nes.emulator.apu;

/**
 * The delta modulation channel plays back one bit delta encoded samples fetched from CPU memory.
 * Each incoming bit nudges a seven bit output level up or down by two, which lets games stream short
 * digitised sounds such as drums or speech. Finishing a sample can raise an IRQ or loop forever.
 */
public class DmcChannel {

    private static final int[] RATE_TABLE = {
            428, 380, 340, 320, 286, 254, 226, 214, 190, 160, 142, 128, 106, 84, 72, 54
    };

    private final DmcMemoryReader memoryReader;

    private boolean irqEnabled;
    private boolean loop;
    private int timerPeriod;
    private int timer;
    private int outputLevel;

    private int sampleAddress;
    private int sampleLength;
    private int currentAddress;
    private int bytesRemaining;

    private int shiftRegister;
    private int bitsRemaining;
    private boolean silence = true;
    private boolean sampleBufferFilled;
    private int sampleBuffer;

    private boolean irqPending;

    public DmcChannel(DmcMemoryReader memoryReader) {
        this.memoryReader = memoryReader;
    }

    public void writeControl(byte data) {
        irqEnabled = (data & 0x80) != 0;
        loop = (data & 0x40) != 0;
        timerPeriod = RATE_TABLE[data & 0x0f];
        if (!irqEnabled) {
            irqPending = false;
        }
    }

    public void writeDirectLoad(byte data) {
        outputLevel = data & 0x7f;
    }

    public void writeSampleAddress(byte data) {
        sampleAddress = 0xc000 | ((data & 0xff) << 6);
    }

    public void writeSampleLength(byte data) {
        sampleLength = ((data & 0xff) << 4) | 1;
    }

    public void setEnabled(boolean enabled) {
        irqPending = false;
        if (!enabled) {
            bytesRemaining = 0;
        } else if (bytesRemaining == 0) {
            restartSample();
        }
    }

    public boolean isActive() {
        return bytesRemaining > 0;
    }

    public boolean isIrqPending() {
        return irqPending;
    }

    public void clearIrq() {
        irqPending = false;
    }

    /**
     * Clocked every CPU cycle. Whenever the timer expires one output bit is consumed, and a fresh
     * byte is pulled from memory once the previous one has been shifted out completely.
     */
    public void clockTimer() {
        if (!sampleBufferFilled && bytesRemaining > 0) {
            fillSampleBuffer();
        }
        if (timer == 0) {
            timer = timerPeriod;
            clockOutput();
        } else {
            --timer;
        }
    }

    public int output() {
        return outputLevel;
    }

    private void clockOutput() {
        if (!silence) {
            if ((shiftRegister & 1) != 0) {
                if (outputLevel <= 125) {
                    outputLevel += 2;
                }
            } else if (outputLevel >= 2) {
                outputLevel -= 2;
            }
        }
        shiftRegister >>= 1;
        if (bitsRemaining > 0) {
            --bitsRemaining;
        }
        if (bitsRemaining == 0) {
            bitsRemaining = 8;
            if (sampleBufferFilled) {
                silence = false;
                shiftRegister = sampleBuffer;
                sampleBufferFilled = false;
            } else {
                silence = true;
            }
        }
    }

    private void fillSampleBuffer() {
        sampleBuffer = memoryReader.read(currentAddress) & 0xff;
        sampleBufferFilled = true;
        currentAddress = 0x8000 | ((currentAddress + 1) & 0x7fff);
        --bytesRemaining;
        if (bytesRemaining == 0) {
            if (loop) {
                restartSample();
            } else if (irqEnabled) {
                irqPending = true;
            }
        }
    }

    private void restartSample() {
        currentAddress = sampleAddress;
        bytesRemaining = sampleLength;
    }
}
