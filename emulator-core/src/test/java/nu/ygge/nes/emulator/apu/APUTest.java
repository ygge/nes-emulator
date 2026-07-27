package nu.ygge.nes.emulator.apu;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class APUTest {

    private static final int CYCLES_PER_FRAME = 29830;

    private APU apu;

    @BeforeEach
    void setUp() {
        apu = new APU(address -> 0);
    }

    @Test
    void verifyAnEnabledChannelWithALoadedLengthReportsAsActive() {
        // enable pulse one, then load its length counter through the high timer byte
        apu.writeRegister(0x4015, (byte) 0x01);
        apu.writeRegister(0x4003, (byte) 0x08);

        Assertions.assertEquals(0x01, apu.readStatus() & 0x01);
    }

    @Test
    void verifyDisablingAChannelClearsItsLengthCounter() {
        apu.writeRegister(0x4015, (byte) 0x01);
        apu.writeRegister(0x4003, (byte) 0x08);
        Assertions.assertEquals(0x01, apu.readStatus() & 0x01);

        apu.writeRegister(0x4015, (byte) 0x00);

        Assertions.assertEquals(0x00, apu.readStatus() & 0x01);
    }

    @Test
    void verifyTheFrameCounterRaisesAnInterruptOncePerFrameInFourStepMode() {
        var irq = apu.tick(CYCLES_PER_FRAME);

        Assertions.assertTrue(irq);
        Assertions.assertTrue(apu.isIrqAsserted());
        Assertions.assertEquals(0x40, apu.readStatus() & 0x40);
    }

    @Test
    void verifyReadingTheStatusAcknowledgesTheFrameInterrupt() {
        apu.tick(CYCLES_PER_FRAME);
        Assertions.assertTrue(apu.isIrqAsserted());

        apu.readStatus();

        Assertions.assertFalse(apu.isIrqAsserted());
    }

    @Test
    void verifyInhibitingTheFrameInterruptKeepsTheLineHigh() {
        // bit six inhibits the interrupt
        apu.writeRegister(0x4017, (byte) 0x40);

        Assertions.assertFalse(apu.tick(2 * CYCLES_PER_FRAME));
    }

    @Test
    void verifyFiveStepModeNeverRaisesTheFrameInterrupt() {
        // bit seven selects the five step sequence, which has no interrupt
        apu.writeRegister(0x4017, (byte) 0x80);

        Assertions.assertFalse(apu.tick(2 * CYCLES_PER_FRAME));
    }

    @Test
    void verifyAudioSamplesAreProducedAtRoughlyTheSampleRate() {
        apu.tick(CYCLES_PER_FRAME);

        var samples = new short[4096];
        var count = apu.readSamples(samples);

        // one frame at the CPU clock should yield close to 44100 / 60 samples
        Assertions.assertTrue(count > 700 && count < 760, "unexpected sample count: " + count);
    }

    @Test
    void verifyAPlayingPulseChannelProducesANonZeroWaveform() {
        apu.writeRegister(0x4015, (byte) 0x01);
        apu.writeRegister(0x4000, (byte) 0x1f); // constant volume of fifteen, duty zero
        apu.writeRegister(0x4002, (byte) 0xff); // timer period low
        apu.writeRegister(0x4003, (byte) 0x08); // timer high plus a length load

        apu.tick(CYCLES_PER_FRAME);

        var samples = new short[4096];
        var count = apu.readSamples(samples);
        var hasSound = false;
        for (int i = 0; i < count; ++i) {
            if (samples[i] != 0) {
                hasSound = true;
                break;
            }
        }
        Assertions.assertTrue(hasSound, "expected the pulse channel to produce audible output");
    }

    @Test
    void verifyTheDmcFetchesSampleBytesFromMemory() {
        var reads = new int[]{0};
        var dmcApu = new APU(address -> {
            ++reads[0];
            return (byte) 0xaa;
        });
        dmcApu.writeRegister(0x4012, (byte) 0x00); // sample at $c000
        dmcApu.writeRegister(0x4013, (byte) 0x01); // a couple of bytes
        dmcApu.writeRegister(0x4015, (byte) 0x10); // enable the DMC

        dmcApu.tick(CYCLES_PER_FRAME);

        Assertions.assertTrue(reads[0] > 0, "expected the DMC to fetch from memory");
    }
}
