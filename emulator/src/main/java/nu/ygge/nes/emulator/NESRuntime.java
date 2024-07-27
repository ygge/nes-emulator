package nu.ygge.nes.emulator;

import lombok.Getter;
import nu.ygge.nes.emulator.cpu.CPU;
import nu.ygge.nes.emulator.cpu.CPUUtil;
import nu.ygge.nes.emulator.cpu.InterruptAddress;
import nu.ygge.nes.emulator.cpu.OpCode;
import nu.ygge.nes.emulator.memory.Memory;

import java.util.function.BooleanSupplier;
import java.util.function.Supplier;

@Getter
public class NESRuntime {

    private final CPU cpu;
    private final Memory memory;

    public NESRuntime() {
        this.cpu = new CPU();
        this.memory = new Memory();
    }

    public void run(BooleanSupplier callback) {
        while (callback.getAsBoolean()) {
            performSingleInstruction();
        }
    }

    public void performSingleInstruction() {
        byte opCode = cpu.readInstruction(memory);
        var operation = OpCode.getOpCode(opCode);
        if (operation == null) {
            throw new IllegalArgumentException("Unknown op code: " + opCode);
        }
        var extraBytes = operation.getAddressingMode().getExtraBytes();
        var eb1 = extraBytes > 0 ? cpu.readInstruction(memory) : 0;
        var eb2 = extraBytes > 1 ? cpu.readInstruction(memory) : 0;
        operation.perform(this, eb1, eb2);
        cpu.addCycles(operation.getCycles());
    }

    public void reset() {
        cpu.reset();
        cpu.setStatusInterrupt();
        resetProgramCounter(InterruptAddress.RESET);
    }

    public void resetProgramCounter(InterruptAddress interruptAddress) {
        var lsb = memory.read(interruptAddress.getStartAddress());
        var msb = memory.read(interruptAddress.getStartAddress() + 1);
        cpu.setProgramCounter(CPUUtil.toAddress(msb, lsb));
    }

    public void loadGame(byte[] gameCode) {
        memory.writeData(0x8000, gameCode);
    }

    public void loadGame(short[] gameCode, int gameCodeAddress, int startAddress) {
        memory.writeData(gameCodeAddress, gameCode);
        memory.write(InterruptAddress.RESET.getStartAddress(), (byte)(startAddress & 0xFF));
        memory.write(InterruptAddress.RESET.getStartAddress() + 1, (byte)(startAddress >> 8));
    }
}
