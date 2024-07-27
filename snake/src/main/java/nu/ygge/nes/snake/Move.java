package nu.ygge.nes.snake;

import lombok.Getter;

@Getter
public enum Move {
    UP(0x77),
    RIGHT(0x64),
    DOWN(0x73),
    LEFT(0x61);

    private final byte code;

    Move(int code) {
        this.code = (byte) code;
    }
}
