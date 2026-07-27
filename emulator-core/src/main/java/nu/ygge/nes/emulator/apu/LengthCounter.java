package nu.ygge.nes.emulator.apu;

import nu.ygge.nes.emulator.state.StateReader;
import nu.ygge.nes.emulator.state.StateWriter;

/**
 * Silences a channel after a programmable amount of time. Loading a value looks up a duration from a
 * fixed table, and every half frame the counter ticks down towards zero unless it is halted.
 */
public class LengthCounter {

    private static final int[] LENGTH_TABLE = {
            10, 254, 20, 2, 40, 4, 80, 6, 160, 8, 60, 10, 14, 12, 26, 14,
            12, 16, 24, 18, 48, 20, 96, 22, 192, 24, 72, 26, 16, 28, 32, 30
    };

    private boolean enabled;
    private boolean halt;
    private int counter;

    public void saveState(StateWriter writer) {
        writer.writeBoolean(enabled);
        writer.writeBoolean(halt);
        writer.writeInt(counter);
    }

    public void loadState(StateReader reader) {
        enabled = reader.readBoolean();
        halt = reader.readBoolean();
        counter = reader.readInt();
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) {
            counter = 0;
        }
    }

    public void setHalt(boolean halt) {
        this.halt = halt;
    }

    public void load(int index) {
        if (enabled) {
            counter = LENGTH_TABLE[index & 0x1f];
        }
    }

    public void clock() {
        if (!halt && counter > 0) {
            --counter;
        }
    }

    public boolean isActive() {
        return counter > 0;
    }
}
