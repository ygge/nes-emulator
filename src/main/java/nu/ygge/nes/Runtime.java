package nu.ygge.nes;

import lombok.Getter;
import nu.ygge.nes.cpu.CPU;
import nu.ygge.nes.cpu.OpCode;
import nu.ygge.nes.memory.RAM;

@Getter
public class Runtime {

    private final CPU cpu;
    private final RAM ram;

    public Runtime() {
        this.cpu = new CPU();
        this.ram = new RAM();
    }

    public void performInstruction() {
        byte opCode = readInstruction();
        var operation = OpCode.OP_CODES.get(opCode);
        if (operation == null) {
            throw new IllegalArgumentException("Unknown op code: " + opCode);
        }
        var extraBytes = operation.getAddressingMode().getExtraBytes();
        var eb1 = extraBytes > 0 ? readInstruction() : 0;
        var eb2 = extraBytes > 1 ? readInstruction() : 0;
        operation.perform(this, eb1, eb2);
    }

    private byte readInstruction() {
        byte ins = ram.read(cpu.getProgramCounter());
        cpu.setProgramCounter(cpu.getProgramCounter() + 1);
        return ins;
    }
}
