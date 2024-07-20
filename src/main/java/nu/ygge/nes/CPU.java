package nu.ygge.nes;

import lombok.Getter;
import lombok.Setter;

public class CPU {

    private final byte[] programCounter = new byte[2];
    @Getter
    @Setter
    private short accumulator, registerX, registerY, stackPointer;
    private short statusRegister;

    public void setStatusNegative() {
        statusRegister |= 0b10000000;
    }

    public void clearStatusNegative() {
        statusRegister &= 0b01111111;
    }

    public void setStatusOverflow() {
        statusRegister |= 0b1000000;
    }

    public void clearStatusOverflow() {
        statusRegister &= 0b10111111;
    }

    public void setStatusBreak() {
        statusRegister |= 0b10000;
    }

    public void clearStatusBreak() {
        statusRegister &= 0b11101111;
    }

    public void setStatusDecimal() {
        statusRegister |= 0b1000;
    }

    public void clearStatusDecimal() {
        statusRegister &= 0b11110111;
    }

    public void setStatusInterrupt() {
        statusRegister |= 0b100;
    }

    public void clearStatusInterrupt() {
        statusRegister &= 0b11111011;
    }

    public void setStatusZero() {
        statusRegister |= 0b10;
    }

    public void clearStatusZero() {
        statusRegister &= 0b11111101;
    }

    public void setStatusCarry() {
        statusRegister |= 0b1;
    }

    public void clearStatusCarry() {
        statusRegister &= 0b11111110;
    }
}
