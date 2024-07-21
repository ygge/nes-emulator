package nu.ygge.nes.cpu.instructions;

import lombok.Getter;

@Getter
public enum Instruction {
    SEC("Set carry flag", runtime -> runtime.getCpu().setStatusCarry()),
    CLC("Clear carry flag", runtime -> runtime.getCpu().clearStatusCarry()),
    CLV("Clear overflow flag", runtime -> runtime.getCpu().clearStatusOverflow()),
    SEI("Set interrupt disable flag", runtime -> runtime.getCpu().setStatusInterrupt()),
    CLI("Clear interrupt disable flag", runtime -> runtime.getCpu().clearStatusInterrupt()),
    SED("Set decimal mode", runtime -> runtime.getCpu().setStatusDecimal()),
    CLD("Clear decimal mode", runtime -> runtime.getCpu().clearStatusDecimal());

    private final String description;
    private final NoArgumentInstruction noArgumentInstruction;

    Instruction(String description, NoArgumentInstruction noArgumentInstruction) {
        this.description = description;
        this.noArgumentInstruction = noArgumentInstruction;
    }
}
