package nu.ygge.nes.emulator;

import lombok.Getter;
import nu.ygge.nes.emulator.bus.Bus;
import nu.ygge.nes.emulator.bus.EmulatorBus;
import nu.ygge.nes.emulator.cpu.CPU;
import nu.ygge.nes.emulator.cpu.CPUUtil;
import nu.ygge.nes.emulator.cpu.InterruptAddress;
import nu.ygge.nes.emulator.cpu.OpCode;

import java.util.function.BooleanSupplier;

@Getter
public class NESRuntime {

    private final CPU cpu;
    private Bus bus;

    public NESRuntime() {
        this.cpu = new CPU();
        this.bus = new EmulatorBus(new byte[0]);
    }

    public void run(BooleanSupplier callback) {
        while (callback.getAsBoolean()) {
            performSingleInstruction();
        }
    }

    public void performSingleInstruction() {
        byte opCode = cpu.readInstruction(bus);
        var operation = OpCode.getOpCode(opCode);
        if (operation == null) {
            throw new IllegalArgumentException("Unknown op code: " + opCode);
        }
        var extraBytes = operation.getAddressingMode().getExtraBytes();
        var eb1 = extraBytes > 0 ? cpu.readInstruction(bus) : 0;
        var eb2 = extraBytes > 1 ? cpu.readInstruction(bus) : 0;
        operation.perform(this, eb1, eb2);
        cpu.addCycles(operation.getCycles());
    }

    public void reset() {
        cpu.reset();
        cpu.setStatusInterrupt();
        resetProgramCounter(InterruptAddress.RESET);
    }

    public void resetProgramCounter(InterruptAddress interruptAddress) {
        var lsb = bus.read(interruptAddress.getStartAddress());
        var msb = bus.read(interruptAddress.getStartAddress() + 1);
        cpu.setProgramCounter(CPUUtil.toAddress(msb, lsb));
    }

    public void loadGame(byte[] fileData) {
        var parsedData = new NesFileLoader(fileData);
        var emulatorBus = new EmulatorBus(parsedData.getPrgRom());
        emulatorBus.getPpu().reset(parsedData.getChrRom());
        loadGame(emulatorBus);
    }

    public void loadGame(Bus bus) {
        this.bus = bus;
    }
}
