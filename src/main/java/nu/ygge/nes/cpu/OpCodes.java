package nu.ygge.nes.cpu;

import lombok.Getter;

@Getter
public enum OpCodes {
    CLC(0x18),
    SEC(0x38),
    CLI(0x58),
    SEI(0x78),
    CLV(0xB8),
    CLD(0xD8),
    SED(0xF8);

    private final byte code;

    OpCodes(int code) {
        this.code = (byte)code;
    }
}
