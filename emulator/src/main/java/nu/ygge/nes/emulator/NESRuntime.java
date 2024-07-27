package nu.ygge.nes.emulator;

import lombok.Getter;
import nu.ygge.nes.emulator.cpu.CPU;
import nu.ygge.nes.emulator.cpu.CPUUtil;
import nu.ygge.nes.emulator.cpu.OpCode;
import nu.ygge.nes.emulator.memory.Memory;

@Getter
public class NESRuntime {

    private static final int ADDRESS_NMI = 0xFFFA;
    private static final int ADDRESS_RESET = 0xFFFC;
    private static final int ADDRESS_BREAK = 0xFFFE;

    private final CPU cpu;
    private final Memory memory;

    public NESRuntime() {
        this.cpu = new CPU();
        this.memory = new Memory();
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
        var lsb = memory.read(ADDRESS_RESET);
        var msb = memory.read(ADDRESS_RESET + 1);
        cpu.setProgramCounter(CPUUtil.toAddress(msb, lsb));
    }
}
