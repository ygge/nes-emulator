package nu.ygge.nes.emulator.apu;

import nu.ygge.nes.emulator.state.StateReader;
import nu.ygge.nes.emulator.state.StateWriter;

/**
 * The frequency sweep unit of a pulse channel. Once per half frame it slides the channel timer
 * period up or down by a shifted copy of itself, which either raises or lowers the pitch over time.
 * When the resulting period would fall outside the playable range the channel is muted.
 */
public class Sweep {

    private final boolean negateAddsOne;

    private boolean enabled;
    private boolean negate;
    private int period;
    private int shift;
    private boolean reload;
    private int divider;
    private int targetPeriod;
    private boolean muting;

    /**
     * The two pulse channels differ in how a negative sweep is computed: the first subtracts one
     * extra, so a fixed sweep on channel one drifts by a single step compared to channel two.
     */
    public Sweep(boolean negateAddsOne) {
        this.negateAddsOne = negateAddsOne;
    }

    public void saveState(StateWriter writer) {
        writer.writeBoolean(enabled);
        writer.writeBoolean(negate);
        writer.writeInt(period);
        writer.writeInt(shift);
        writer.writeBoolean(reload);
        writer.writeInt(divider);
        writer.writeInt(targetPeriod);
        writer.writeBoolean(muting);
    }

    public void loadState(StateReader reader) {
        enabled = reader.readBoolean();
        negate = reader.readBoolean();
        period = reader.readInt();
        shift = reader.readInt();
        reload = reader.readBoolean();
        divider = reader.readInt();
        targetPeriod = reader.readInt();
        muting = reader.readBoolean();
    }

    public void write(byte data) {
        enabled = (data & 0x80) != 0;
        period = (data >> 4) & 0x07;
        negate = (data & 0x08) != 0;
        shift = data & 0x07;
        reload = true;
    }

    public void clock(PulseChannel channel) {
        computeTarget(channel.getTimerPeriod());
        if (divider == 0 && enabled && shift > 0 && !muting) {
            channel.setTimerPeriod(targetPeriod);
        }
        if (divider == 0 || reload) {
            divider = period;
            reload = false;
        } else {
            --divider;
        }
    }

    /**
     * Recomputes the muting flag against the current period even between sweep clocks, since a
     * channel with a period below eight or an overflowing target is silenced immediately.
     */
    public void updateMuting(int currentPeriod) {
        computeTarget(currentPeriod);
    }

    public boolean isMuting() {
        return muting;
    }

    private void computeTarget(int currentPeriod) {
        var change = currentPeriod >> shift;
        if (negate) {
            change = -change;
            if (negateAddsOne) {
                --change;
            }
        }
        targetPeriod = currentPeriod + change;
        muting = currentPeriod < 8 || targetPeriod > 0x7ff;
    }
}
