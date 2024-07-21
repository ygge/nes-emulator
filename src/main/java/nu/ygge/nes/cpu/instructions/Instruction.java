package nu.ygge.nes.cpu.instructions;

import lombok.Getter;

@Getter
public enum Instruction {
    CLC("Clear carry flag", runtime -> runtime.getCpu().clearStatusCarry()),
    CLD("Clear decimal mode", runtime -> runtime.getCpu().clearStatusDecimal()),
    CLI("Clear interrupt disable flag", runtime -> runtime.getCpu().clearStatusInterrupt()),
    CLV("Clear overflow flag", runtime -> runtime.getCpu().clearStatusOverflow()),
    LDA("Load accumulator with memory", StatusFlagsAffected.SIMPLE, (runtime, value) -> runtime.getCpu().setAccumulator(value)),
    SEC("Set carry flag", runtime -> runtime.getCpu().setStatusCarry()),
    SED("Set decimal mode", runtime -> runtime.getCpu().setStatusDecimal()),
    SEI("Set interrupt disable flag", runtime -> runtime.getCpu().setStatusInterrupt());

    private final String description;
    private final StatusFlagsAffected statusFlagsAffected;
    private final NoArgumentInstruction noArgumentInstruction;
    private final SingleArgumentInstruction singleArgumentInstruction;

    Instruction(String description, NoArgumentInstruction noArgumentInstruction) {
        this.description = description;
        this.statusFlagsAffected = StatusFlagsAffected.NONE;
        this.noArgumentInstruction = noArgumentInstruction;
        this.singleArgumentInstruction = null;
    }

    Instruction(String description, StatusFlagsAffected statusFlagsAffected, SingleArgumentInstruction singleArgumentInstruction) {
        this.description = description;
        this.statusFlagsAffected = statusFlagsAffected;
        this.noArgumentInstruction = null;
        this.singleArgumentInstruction = singleArgumentInstruction;
    }
}
