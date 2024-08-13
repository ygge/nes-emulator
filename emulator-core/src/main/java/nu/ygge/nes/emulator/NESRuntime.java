package nu.ygge.nes.emulator;

import lombok.Getter;
import nu.ygge.nes.emulator.bus.Bus;
import nu.ygge.nes.emulator.bus.EmulatorBus;
import nu.ygge.nes.emulator.bus.PPUTickResult;
import nu.ygge.nes.emulator.cpu.*;
import nu.ygge.nes.emulator.ppu.Frame;
import nu.ygge.nes.emulator.ppu.Tile;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;
import java.util.function.Supplier;

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
        FileLogger.log(cpu.getProgramCounter()
                + " " + (opCode < 0 ? opCode+256 : opCode)
                + " " + (getCpu().getAccumulator() < 0 ? getCpu().getAccumulator()+256 : getCpu().getAccumulator())
                + " " + ((EmulatorBus)bus).getPpu().getAddressRegister().get());
        var operation = OpCode.getOpCode(opCode);
        if (operation == null) {
            throw new IllegalArgumentException("Unknown op code: " + opCode);
        }
        var extraBytes = operation.getAddressingMode().getExtraBytes();
        var eb1 = extraBytes > 0 ? cpu.readInstruction(bus) : 0;
        var eb2 = extraBytes > 1 ? cpu.readInstruction(bus) : 0;
        operation.perform(this, eb1, eb2);
        cpu.addCycles(operation.getCycles());
        var newCycles = cpu.getCycles() - cycles;
        cycles += newCycles;
        var result = bus.ppuTick(newCycles * 3);
        if (result == PPUTickResult.NMI) {
            performNMIInterrupt();
            if (frameConsumer != null) {
                frameConsumer.accept(bus.getFrame());
            }
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
        var emulatorBus = new EmulatorBus(parsedData.getPrgRom());
        emulatorBus.getPpu().reset(parsedData.getChrRom(), parsedData.getMirroring());
        loadGame(emulatorBus);
    }

    public void loadGame(Bus bus) {
        this.bus = bus;
    }

    private void performNMIInterrupt() {
        StackHelper.saveAddressToStack(this, cpu.getProgramCounter());
        var prevStatus = cpu.getStatusRegister();
        cpu.clearStatusBreak();
        cpu.setStatusIgnored();
        StackHelper.pushToStack(this, cpu.getStatusRegister());
        cpu.setStatusRegister(prevStatus);
        cpu.setStatusInterrupt();
        bus.ppuTick(2 * 3); // this takes two more cycles
        resetProgramCounter(InterruptAddress.NMI);
    }

    public void keyCallback(int bitValue, boolean set) {
        ((EmulatorBus)bus).getJoypad().keyCallback(bitValue, set);
    }
}
