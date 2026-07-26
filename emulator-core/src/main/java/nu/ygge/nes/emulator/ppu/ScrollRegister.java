package nu.ygge.nes.emulator.ppu;

import lombok.Getter;

@Getter
public class ScrollRegister {

    private final WriteLatch latch;
    private int x, y;

    public ScrollRegister(WriteLatch latch) {
        this.latch = latch;
    }

    public void write(byte data) {
        if (latch.isSecondWrite()) {
            y = data & 0xff;
        } else {
            x = data & 0xff;
        }
        latch.toggle();
    }
}
