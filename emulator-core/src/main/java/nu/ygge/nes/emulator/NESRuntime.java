package nu.ygge.nes.emulator;

import lombok.Getter;
import nu.ygge.nes.emulator.bus.Bus;
import nu.ygge.nes.emulator.bus.EmulatorBus;
import nu.ygge.nes.emulator.bus.PPUTickResult;
import nu.ygge.nes.emulator.cpu.*;
import nu.ygge.nes.emulator.mapper.MapperFactory;
import nu.ygge.nes.emulator.ppu.Frame;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

@Getter
public class NESRuntime {

    private final CPU cpu;
    private final Consumer<Frame> frameConsumer;
    private Bus bus;
    private int cycles;

    public NESRuntime() {
        this(null);
    }

    public NESRuntime(Consumer<Frame> frameConsumer) {
        this.cpu = new CPU();
        this.bus = new EmulatorBus(new byte[0]);
        this.frameConsumer = frameConsumer;
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
        cpu.addCycles(operation.getCycles() + bus.consumeStallCycles());
        var newCycles = cpu.getCycles() - cycles;
        cycles += newCycles;
        var irq = bus.apuTick(newCycles);
        var result = bus.ppuTick(newCycles * 3);
        if (result == PPUTickResult.NMI) {
            performNMIInterrupt();
        } else if ((irq || bus.isMapperIrqAsserted()) && !cpu.isStatusInterrupt()) {
            performIRQInterrupt();
        }
        if (result != PPUTickResult.NORMAL && frameConsumer != null) {
            frameConsumer.accept(bus.getFrame());
        }
    }

    public void reset() {
        cpu.reset();
        cpu.setStatusInterrupt();
        resetProgramCounter(InterruptAddress.RESET);
        cycles = 0;
    }

    public void resetProgramCounter(InterruptAddress interruptAddress) {
        var lsb = bus.read(interruptAddress.getStartAddress());
        var msb = bus.read(interruptAddress.getStartAddress() + 1);
        cpu.setProgramCounter(CPUUtil.toAddress(msb, lsb));
    }

    public void loadGame(byte[] fileData) {
        var parsedData = new NesFileLoader(fileData);
        var mapper = MapperFactory.create(parsedData);
        loadGame(new EmulatorBus(mapper));
    }

    public void loadGame(Bus bus) {
        this.bus = bus;
    }

    private void performNMIInterrupt() {
        performInterrupt(InterruptAddress.NMI);
    }

    private void performIRQInterrupt() {
        performInterrupt(InterruptAddress.IRQ);
    }

    private void performInterrupt(InterruptAddress interruptAddress) {
        StackHelper.saveAddressToStack(this, cpu.getProgramCounter());
        var prevStatus = cpu.getStatusRegister();
        cpu.clearStatusBreak();
        cpu.setStatusIgnored();
        StackHelper.pushToStack(this, cpu.getStatusRegister());
        cpu.setStatusRegister(prevStatus);
        cpu.setStatusInterrupt();
        bus.ppuTick(2 * 3); // this takes two more cycles
        resetProgramCounter(interruptAddress);
    }
}
