package nu.ygge.nes.cpu.instructions;

import lombok.Getter;
import nu.ygge.nes.NESRuntime;
import nu.ygge.nes.cpu.CPU;

@Getter
public enum Instructions {
    ADC("Add Memory to Accumulator with Carry", StatusFlagsAffected.STANDARD, InstructionFunctions::addMemoryToAccumulator, WriteValue.Accumulator),
    AND("AND Memory with Accumulator", StatusFlagsAffected.STANDARD, InstructionFunctions::andMemoryWithAccumulator, WriteValue.Accumulator),
    ASL("Shift Left One Bit", StatusFlagsAffected.STANDARD, InstructionFunctions::shiftLeftOneBit, WriteValue.AccumulatorOrMemory),
    BMI("Branch on Result Minus", CPU::isStatusNegative),
    BCC("Branch on Carry Clear", (CPU cpu) -> !cpu.isStatusCarry()),
    BCS("Branch on Carry Set", CPU::isStatusCarry),
    BEQ("Branch on Result Zero", CPU::isStatusZero),
    BIT("Test Bits in Memory with Accumulator", StatusFlagsAffected.ZERO, InstructionFunctions::testBitsWithAccumulator),
    BNE("Branch on Result not Zero", (CPU cpu) -> !cpu.isStatusZero()),
    BPL("Branch on Result Plus", (CPU cpu) -> !cpu.isStatusNegative()),
    BVC("Branch on Overflow Clear", (CPU cpu) -> !cpu.isStatusOverflow()),
    BVS("Branch on Overflow Set", CPU::isStatusOverflow),
    CLC("Clear carry flag", (NESRuntime runtime) -> runtime.getCpu().clearStatusCarry()),
    CLD("Clear decimal mode", (NESRuntime runtime) -> runtime.getCpu().clearStatusDecimal()),
    CLI("Clear interrupt disable flag", (NESRuntime runtime) -> runtime.getCpu().clearStatusInterrupt()),
    CLV("Clear overflow flag", (NESRuntime runtime) -> runtime.getCpu().clearStatusOverflow()),
    EOR("Exclusive-OR Memory with Accumulator", StatusFlagsAffected.STANDARD, InstructionFunctions::exclusiveOrMemoryWithAccumulator, WriteValue.Accumulator),
    LDA("Load accumulator with memory", StatusFlagsAffected.STANDARD, InstructionFunctions::loadAccumulator),
    LSR("Shift Right One Bit", StatusFlagsAffected.STANDARD, InstructionFunctions::shiftRightOneBit, WriteValue.AccumulatorOrMemory),
    ORA("OR Memory with Accumulator", StatusFlagsAffected.STANDARD, InstructionFunctions::orMemoryWithAccumulator, WriteValue.Accumulator),
    PHA("Push Accumulator on Stack", InstructionFunctions::pushAccumulatorOnStack),
    PHP("Push Processor Status on Stack", InstructionFunctions::pushProcessorStatusOnStack),
    PLA("Pull Accumulator from Stack", InstructionFunctions::pullAccumulatorFromStack),
    PLP("Pull Processor Status from Stack", InstructionFunctions::pullProcessorStatusFromStack),
    ROL("Rotate One Bit Left", StatusFlagsAffected.STANDARD, InstructionFunctions::rotateLeftOneBit, WriteValue.AccumulatorOrMemory),
    ROR("Rotate One Bit Right", StatusFlagsAffected.STANDARD, InstructionFunctions::rotateRightOneBit, WriteValue.AccumulatorOrMemory),
    SEC("Set carry flag", (NESRuntime runtime) -> runtime.getCpu().setStatusCarry()),
    SED("Set decimal mode", (NESRuntime runtime) -> runtime.getCpu().setStatusDecimal()),
    SEI("Set interrupt disable flag", (NESRuntime runtime) -> runtime.getCpu().setStatusInterrupt());

    private final String description;
    private final StatusFlagsAffected statusFlagsAffected;
    private final NoArgumentInstruction noArgumentInstruction;
    private final SingleArgumentInstruction singleArgumentInstruction;
    private final BranchingInstruction branchingInstruction;
    private final WriteValue writeValue;

    Instructions(String description, NoArgumentInstruction noArgumentInstruction) {
        this.description = description;
        this.statusFlagsAffected = StatusFlagsAffected.NONE;
        this.noArgumentInstruction = noArgumentInstruction;
        this.singleArgumentInstruction = null;
        branchingInstruction = null;
        writeValue = WriteValue.None;
    }

    Instructions(String description, StatusFlagsAffected statusFlagsAffected, SingleArgumentInstruction singleArgumentInstruction) {
        this(description, statusFlagsAffected, singleArgumentInstruction, WriteValue.None);
    }

    Instructions(String description, StatusFlagsAffected statusFlagsAffected, SingleArgumentInstruction singleArgumentInstruction, WriteValue writeValue) {
        this.description = description;
        this.statusFlagsAffected = statusFlagsAffected;
        this.noArgumentInstruction = null;
        this.singleArgumentInstruction = singleArgumentInstruction;
        branchingInstruction = null;
        this.writeValue = writeValue;
    }

    Instructions(String description, BranchingInstruction branchingInstruction) {
        this.description = description;
        this.statusFlagsAffected = StatusFlagsAffected.NONE;
        this.noArgumentInstruction = null;
        this.singleArgumentInstruction = null;
        this.branchingInstruction = branchingInstruction;
        this.writeValue = WriteValue.None;
    }
}
