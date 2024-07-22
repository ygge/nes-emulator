package nu.ygge.nes.cpu.instructions;

import lombok.Getter;

@Getter
public enum Instruction {
    ASL("Shift Left One Bit", StatusFlagsAffected.SIMPLE, Instructions::shiftLeftOneBit, true),
    CLC("Clear carry flag", runtime -> runtime.getCpu().clearStatusCarry()),
    CLD("Clear decimal mode", runtime -> runtime.getCpu().clearStatusDecimal()),
    CLI("Clear interrupt disable flag", runtime -> runtime.getCpu().clearStatusInterrupt()),
    CLV("Clear overflow flag", runtime -> runtime.getCpu().clearStatusOverflow()),
    LDA("Load accumulator with memory", StatusFlagsAffected.SIMPLE, Instructions::loadAccumulator),
    ORA("OR Memory with Accumulator", StatusFlagsAffected.SIMPLE, Instructions::orMemoryWithAccumulator),
    SEC("Set carry flag", runtime -> runtime.getCpu().setStatusCarry()),
    SED("Set decimal mode", runtime -> runtime.getCpu().setStatusDecimal()),
    SEI("Set interrupt disable flag", runtime -> runtime.getCpu().setStatusInterrupt());

    private final String description;
    private final StatusFlagsAffected statusFlagsAffected;
    private final NoArgumentInstruction noArgumentInstruction;
    private final SingleArgumentInstruction singleArgumentInstruction;
    private final boolean storeValueBack;

    Instruction(String description, NoArgumentInstruction noArgumentInstruction) {
        this.description = description;
        this.statusFlagsAffected = StatusFlagsAffected.NONE;
        this.noArgumentInstruction = noArgumentInstruction;
        this.singleArgumentInstruction = null;
        storeValueBack = false;
    }

    Instruction(String description, StatusFlagsAffected statusFlagsAffected, SingleArgumentInstruction singleArgumentInstruction) {
        this(description, statusFlagsAffected, singleArgumentInstruction, false);
    }

    Instruction(String description, StatusFlagsAffected statusFlagsAffected, SingleArgumentInstruction singleArgumentInstruction, boolean storeValueBack) {
        this.description = description;
        this.statusFlagsAffected = statusFlagsAffected;
        this.noArgumentInstruction = null;
        this.singleArgumentInstruction = singleArgumentInstruction;
        this.storeValueBack = storeValueBack;
    }
}
