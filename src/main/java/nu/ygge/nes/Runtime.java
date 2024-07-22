package nu.ygge.nes;

import lombok.Getter;
import nu.ygge.nes.cpu.CPU;
import nu.ygge.nes.cpu.OpCode;
import nu.ygge.nes.memory.Memory;

@Getter
public class Runtime {

    private final CPU cpu;
    private final Memory memory;

    public Runtime() {
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
}
