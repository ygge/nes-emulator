package nu.ygge.nes.emulator.ppu;

import lombok.Getter;

@Getter
public class Frame {

    private final Tile[][] background, sprites;

    public Frame(Tile[][] background, Tile[][] sprites) {
        this.background = background;
        this.sprites = sprites;
    }
}
