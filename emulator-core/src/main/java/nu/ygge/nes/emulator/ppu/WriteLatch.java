package nu.ygge.nes.emulator.ppu;

/**
 * The scroll register ($2005) and the address register ($2006) share a single toggle deciding
 * whether a write is the first or the second one. Reading the status register ($2002) resets it.
 */
public class WriteLatch {

    private boolean secondWrite;

    public boolean isSecondWrite() {
        return secondWrite;
    }

    public void toggle() {
        secondWrite = !secondWrite;
    }

    public void reset() {
        secondWrite = false;
    }
}
