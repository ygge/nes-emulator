package nu.ygge.nes.emulator.cpu;

import lombok.Getter;
import lombok.Setter;
import nu.ygge.nes.emulator.bus.Bus;
import nu.ygge.nes.emulator.exception.NESException;

@Getter
@Setter
public class CPU {

    private int programCounter;
    private byte accumulator, registerX, registerY, stackPointer;
    private short statusRegister;
    private int cycles;

    public void reset() {
        accumulator = 0;
        registerX = 0;
        registerY = 0;
        stackPointer = (byte) 0xFF;
        statusRegister = 0;
        cycles = 0;
    }

    public byte readInstruction(Bus bus) {
        var value = bus.read(programCounter);
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

    public void setStatusIgnored() {
        statusRegister |= 0b100000;
    }

    public void clearStatusIgnored() {
        statusRegister &= 0b11011111;
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

    public byte getStatusRegister() {
        return (byte)statusRegister;
    }

    public void decrementStackPointer() {
        if (stackPointer == 0) {
            throw new NESException("Stack overflow");
        } else {
            --stackPointer;
        }
    }

    public void incrementStackPointer() {
        if (stackPointer == -1) {
            throw new NESException("Stack empty");
        } else {
            ++stackPointer;
        }
    }
}
