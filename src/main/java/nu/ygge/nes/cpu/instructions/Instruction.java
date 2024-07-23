package nu.ygge.nes.cpu.instructions;

import lombok.Getter;

@Getter
public enum Instruction {
    AND("AND Memory with Accumulator", StatusFlagsAffected.SIMPLE, Instructions::andMemoryWithAccumulator, WriteValue.Accumulator),
    ASL("Shift Left One Bit", StatusFlagsAffected.SIMPLE, Instructions::shiftLeftOneBit, WriteValue.AccumulatorOrMemory),
    BIT("Test Bits in Memory with Accumulator", StatusFlagsAffected.ZERO, Instructions::testBitsWithAccumulator),
    CLC("Clear carry flag", runtime -> runtime.getCpu().clearStatusCarry()),
    CLD("Clear decimal mode", runtime -> runtime.getCpu().clearStatusDecimal()),
    CLI("Clear interrupt disable flag", runtime -> runtime.getCpu().clearStatusInterrupt()),
    CLV("Clear overflow flag", runtime -> runtime.getCpu().clearStatusOverflow()),
    LDA("Load accumulator with memory", StatusFlagsAffected.SIMPLE, Instructions::loadAccumulator),
    ORA("OR Memory with Accumulator", StatusFlagsAffected.SIMPLE, Instructions::orMemoryWithAccumulator, WriteValue.Accumulator),
    PHP("Push Processor Status on Stack", Instructions::pushProcessorStatusOnStack),
    PLP("Pull Processor Status from Stack", Instructions::pullProcessorStatusFromStack),
    ROL("Rotate One Bit Left", StatusFlagsAffected.SIMPLE, Instructions::rotateLeftOneBit, WriteValue.AccumulatorOrMemory),
    SEC("Set carry flag", runtime -> runtime.getCpu().setStatusCarry()),
    SED("Set decimal mode", runtime -> runtime.getCpu().setStatusDecimal()),
    SEI("Set interrupt disable flag", runtime -> runtime.getCpu().setStatusInterrupt());

    private final String description;
    private final StatusFlagsAffected statusFlagsAffected;
    private final NoArgumentInstruction noArgumentInstruction;
    private final SingleArgumentInstruction singleArgumentInstruction;
    private final WriteValue writeValue;

    Instruction(String description, NoArgumentInstruction noArgumentInstruction) {
        this.description = description;
        this.statusFlagsAffected = StatusFlagsAffected.NONE;
        this.noArgumentInstruction = noArgumentInstruction;
        this.singleArgumentInstruction = null;
        writeValue = WriteValue.None;
    }

    Instruction(String description, StatusFlagsAffected statusFlagsAffected, SingleArgumentInstruction singleArgumentInstruction) {
        this(description, statusFlagsAffected, singleArgumentInstruction, WriteValue.None);
    }

    Instruction(String description, StatusFlagsAffected statusFlagsAffected, SingleArgumentInstruction singleArgumentInstruction, WriteValue writeValue) {
        this.description = description;
        this.statusFlagsAffected = statusFlagsAffected;
        this.noArgumentInstruction = null;
        this.singleArgumentInstruction = singleArgumentInstruction;
        this.writeValue = writeValue;
    }
}
