package nu.ygge.nes.cpu;

import lombok.Getter;
import lombok.Setter;
import nu.ygge.nes.memory.RAM;

public class CPU {

    @Getter
    @Setter
    private int programCounter;
    @Getter
    @Setter
    private short accumulator, registerX, registerY, stackPointer;
    private short statusRegister;

    @Getter
    private int cycles;

    public byte readInstruction(RAM ram) {
        var value = ram.read(programCounter);
        ++programCounter;
        return value;
    }

    public void addCycles(int cycles) {
        this.cycles += cycles;
    }

    public void setStatusNegative() {
        statusRegister |= 0b10000000;
    }

    public void clearStatusNegative() {
        statusRegister &= 0b01111111;
    }

    public boolean isStatusNegative() {
        return (statusRegister & 0b10000000) != 0;
    }

    public void setStatusOverflow() {
        statusRegister |= 0b1000000;
    }

    public void clearStatusOverflow() {
        statusRegister &= 0b10111111;
    }

    public boolean isStatusOverflow() {
        return (statusRegister & 0b1000000) != 0;
    }

    public void setStatusBreak() {
        statusRegister |= 0b10000;
    }

    public void clearStatusBreak() {
        statusRegister &= 0b11101111;
    }

    public boolean isStatusBreak() {
        return (statusRegister & 0b10000) != 0;
    }

    public void setStatusDecimal() {
        statusRegister |= 0b1000;
    }

    public void clearStatusDecimal() {
        statusRegister &= 0b11110111;
    }

    public boolean isStatusDecimal() {
        return (statusRegister & 0b1000) != 0;
    }

    public void setStatusInterrupt() {
        statusRegister |= 0b100;
    }

    public void clearStatusInterrupt() {
        statusRegister &= 0b11111011;
    }

    public boolean isStatusInterrupt() {
        return (statusRegister & 0b100) != 0;
    }

    public void setStatusZero() {
        statusRegister |= 0b10;
    }

    public void clearStatusZero() {
        statusRegister &= 0b11111101;
    }

    public boolean isStatusZero() {
        return (statusRegister & 0b10) != 0;
    }

    public void setStatusCarry() {
        statusRegister |= 0b1;
    }

    public void clearStatusCarry() {
        statusRegister &= 0b11111110;
    }

    public boolean isStatusCarry() {
        return (statusRegister & 0b1) != 0;
    }
}
