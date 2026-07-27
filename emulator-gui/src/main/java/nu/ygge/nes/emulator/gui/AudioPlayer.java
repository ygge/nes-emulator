package nu.ygge.nes.emulator.gui;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.DataLine;
import javax.sound.sampled.LineUnavailableException;
import javax.sound.sampled.SourceDataLine;

/**
 * Streams the APU's samples to the host's speakers. The samples arrive as signed 16 bit mono at the
 * sample rate the APU was built for, and writing them into a modestly sized line buffer both plays
 * them and gently paces the emulation to the sound card's clock.
 */
public class AudioPlayer {

    public static final int SAMPLE_RATE = 44_100;
    private static final int BITS_PER_SAMPLE = 16;
    private static final int CHANNELS = 1;
    private static final int BYTES_PER_SAMPLE = 2;

    private SourceDataLine line;
    private byte[] byteBuffer = new byte[0];

    public AudioPlayer() {
        try {
            var format = new AudioFormat(SAMPLE_RATE, BITS_PER_SAMPLE, CHANNELS, true, false);
            var info = new DataLine.Info(SourceDataLine.class, format);
            line = (SourceDataLine) AudioSystem.getLine(info);
            // around a fifth of a second of buffering keeps playback smooth without much lag
            line.open(format, SAMPLE_RATE / 5 * BYTES_PER_SAMPLE);
            line.start();
        } catch (LineUnavailableException | IllegalArgumentException e) {
            line = null;
        }
    }

    public boolean isActive() {
        return line != null;
    }

    public void play(short[] samples, int count) {
        if (line == null || count == 0) {
            return;
        }
        var needed = count * BYTES_PER_SAMPLE;
        if (byteBuffer.length < needed) {
            byteBuffer = new byte[needed];
        }
        for (int i = 0; i < count; ++i) {
            var sample = samples[i];
            byteBuffer[i * 2] = (byte) (sample & 0xff);
            byteBuffer[i * 2 + 1] = (byte) ((sample >> 8) & 0xff);
        }
        line.write(byteBuffer, 0, needed);
    }

    public void close() {
        if (line != null) {
            line.drain();
            line.stop();
            line.close();
        }
    }
}
