package nu.ygge.nes.emulator;

import lombok.Getter;
import nu.ygge.nes.emulator.bus.Bus;
import nu.ygge.nes.emulator.bus.EmulatorBus;
import nu.ygge.nes.emulator.bus.PPUTickResult;
import nu.ygge.nes.emulator.cpu.*;
import nu.ygge.nes.emulator.mapper.MapperFactory;
import nu.ygge.nes.emulator.ppu.Frame;
import nu.ygge.nes.emulator.state.StateReader;
import nu.ygge.nes.emulator.state.StateWriter;

import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

@Getter
public class NESRuntime {

    private static final int SNAPSHOT_MAGIC = 0x4E455353; // the ASCII letters NESS
    private static final int SNAPSHOT_VERSION = 1;

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

    /**
     * Takes a snapshot of the entire machine so it can be resumed later. Call it between instructions;
     * the returned bytes are self contained apart from the game ROM, which is reloaded separately.
     */
    public byte[] saveState() {
        var writer = new StateWriter();
        writer.writeInt(SNAPSHOT_MAGIC);
        writer.writeInt(SNAPSHOT_VERSION);
        cpu.saveState(writer);
        writer.writeInt(cycles);
        bus.saveState(writer);
        return writer.toByteArray();
    }

    /**
     * Restores a snapshot produced by {@link #saveState()} into the currently loaded game. Throws if
     * the data is not a snapshot this build understands, or belongs to a different cartridge.
     */
    public void loadState(byte[] data) {
        var reader = new StateReader(data);
        if (reader.readInt() != SNAPSHOT_MAGIC) {
            throw new IllegalStateException("Not a valid save state file");
        }
        if (reader.readInt() != SNAPSHOT_VERSION) {
            throw new IllegalStateException("Unsupported save state version");
        }
        cpu.loadState(reader);
        cycles = reader.readInt();
        bus.loadState(reader);
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
