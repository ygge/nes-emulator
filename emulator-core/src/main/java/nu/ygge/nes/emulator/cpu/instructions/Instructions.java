package nu.ygge.nes.emulator.cpu.instructions;

import lombok.Getter;
import nu.ygge.nes.emulator.NESRuntime;
import nu.ygge.nes.emulator.cpu.CPU;

@Getter
public enum Instructions {
    ADC("Add Memory to Accumulator with Carry", StatusFlagsAffected.STANDARD, InstructionFunctions::addMemoryToAccumulator, WriteValue.Accumulator),
    AND("AND Memory with Accumulator", StatusFlagsAffected.STANDARD, InstructionFunctions::andMemoryWithAccumulator, WriteValue.Accumulator),
    ASL("Shift Left One Bit", StatusFlagsAffected.STANDARD, InstructionFunctions::shiftLeftOneBit, WriteValue.AccumulatorOrMemory),
    BCC("Branch on Carry Clear", (CPU cpu) -> !cpu.isStatusCarry()),
    BCS("Branch on Carry Set", CPU::isStatusCarry),
    BEQ("Branch on Result Zero", CPU::isStatusZero),
    BIT("Test Bits in Memory with Accumulator", StatusFlagsAffected.ZERO, InstructionFunctions::testBitsWithAccumulator),
    BMI("Branch on Result Minus", CPU::isStatusNegative),
    BNE("Branch on Result not Zero", (CPU cpu) -> !cpu.isStatusZero()),
    BPL("Branch on Result Plus", (CPU cpu) -> !cpu.isStatusNegative()),
    BRK("Force Break", InstructionFunctions::forceBreak),
    BVC("Branch on Overflow Clear", (CPU cpu) -> !cpu.isStatusOverflow()),
    BVS("Branch on Overflow Set", CPU::isStatusOverflow),
    CLC("Clear carry flag", (NESRuntime runtime) -> runtime.getCpu().clearStatusCarry()),
    CLD("Clear decimal mode", (NESRuntime runtime) -> runtime.getCpu().clearStatusDecimal()),
    CLI("Clear interrupt disable flag", (NESRuntime runtime) -> runtime.getCpu().clearStatusInterrupt()),
    CLV("Clear overflow flag", (NESRuntime runtime) -> runtime.getCpu().clearStatusOverflow()),
    CMP("Compare Memory with Accumulator", StatusFlagsAffected.STANDARD, InstructionFunctions::compareMemoryWithAccumulator),
    CPX("Compare Memory and Index X", StatusFlagsAffected.STANDARD, InstructionFunctions::compareMemoryWithRegisterX),
    CPY("Compare Memory and Index Y", StatusFlagsAffected.STANDARD, InstructionFunctions::compareMemoryWithRegisterY),
    DEC("Decrement Memory by One", StatusFlagsAffected.STANDARD, InstructionFunctions::decrementMemory, WriteValue.Memory),
    DEX("Decrement Index X by One", InstructionFunctions::decrementRegisterX, StatusFlagsAffected.STANDARD),
    DEY("Decrement Index Y by One", InstructionFunctions::decrementRegisterY, StatusFlagsAffected.STANDARD),
    EOR("Exclusive-OR Memory with Accumulator", StatusFlagsAffected.STANDARD, InstructionFunctions::exclusiveOrMemoryWithAccumulator, WriteValue.Accumulator),
    INC("Increment Memory by One", StatusFlagsAffected.STANDARD, InstructionFunctions::incrementMemory, WriteValue.Memory),
    INX("Increment Index X by One", InstructionFunctions::incrementRegisterX, StatusFlagsAffected.STANDARD),
    INY("Increment Index Y by One", InstructionFunctions::incrementRegisterY, StatusFlagsAffected.STANDARD),
    JMP("Jump to New Location", InstructionFunctions::jumpToNewLocation),
    JSR("Jump to New Location Saving Return Address", InstructionFunctions::jumpToNewLocationSavingReturnAddress),
    LDA("Load Accumulator with memory", StatusFlagsAffected.STANDARD, InstructionFunctions::loadAccumulator),
    LDX("Load Index X with memory", StatusFlagsAffected.STANDARD, InstructionFunctions::loadRegisterX),
    LDY("Load Index Y with memory", StatusFlagsAffected.STANDARD, InstructionFunctions::loadRegisterY),
    LSR("Shift Right One Bit", StatusFlagsAffected.STANDARD, InstructionFunctions::shiftRightOneBit, WriteValue.AccumulatorOrMemory),
    NOP("No Operation", runtime -> {}),
    ORA("OR Memory with Accumulator", StatusFlagsAffected.STANDARD, InstructionFunctions::orMemoryWithAccumulator, WriteValue.Accumulator),
    PHA("Push Accumulator on Stack", InstructionFunctions::pushAccumulatorOnStack),
    PHP("Push Processor Status on Stack", InstructionFunctions::pushProcessorStatusOnStack),
    PLA("Pull Accumulator from Stack", InstructionFunctions::pullAccumulatorFromStack, StatusFlagsAffected.STANDARD),
    PLP("Pull Processor Status from Stack", InstructionFunctions::pullProcessorStatusFromStack),
    ROL("Rotate One Bit Left", StatusFlagsAffected.STANDARD, InstructionFunctions::rotateLeftOneBit, WriteValue.AccumulatorOrMemory),
    ROR("Rotate One Bit Right", StatusFlagsAffected.STANDARD, InstructionFunctions::rotateRightOneBit, WriteValue.AccumulatorOrMemory),
    RTI("Return from Interrupt", InstructionFunctions::returnFromInterrupt),
    RTS("Return from Subroutine", InstructionFunctions::returnFromSubroutine),
    SBC("Subtract Memory from Accumulator with Borrow", StatusFlagsAffected.STANDARD, InstructionFunctions::subtractMemoryFromAccumulator, WriteValue.Accumulator),
    SEC("Set carry flag", (NESRuntime runtime) -> runtime.getCpu().setStatusCarry()),
    SED("Set decimal mode", (NESRuntime runtime) -> runtime.getCpu().setStatusDecimal()),
    SEI("Set interrupt disable flag", (NESRuntime runtime) -> runtime.getCpu().setStatusInterrupt()),
    STA("Store Accumulator in Memory", (NESRuntime runtime) -> runtime.getCpu().getAccumulator(), StatusFlagsAffected.NONE, WriteValue.Memory),
    STX("Store Index X in Memory", (NESRuntime runtime) -> runtime.getCpu().getRegisterX(), StatusFlagsAffected.NONE, WriteValue.Memory),
    STY("Store Index Y in Memory", (NESRuntime runtime) -> runtime.getCpu().getRegisterY(), StatusFlagsAffected.NONE, WriteValue.Memory),
    TAX("Transfer Accumulator to Index X", InstructionFunctions::transferAccumulatorToRegisterX, StatusFlagsAffected.STANDARD),
    TAY("Transfer Accumulator to Index Y", InstructionFunctions::transferAccumulatorToRegisterY, StatusFlagsAffected.STANDARD),
    TSX("Transfer Stack Pointer to Index X", InstructionFunctions::transferStackPointerToRegisterX, StatusFlagsAffected.STANDARD),
    TXA("Transfer Index X to Accumulator", InstructionFunctions::transferRegisterXToAccumulator, StatusFlagsAffected.STANDARD),
    TXS("Transfer Index X to Stack Pointer", InstructionFunctions::transferRegisterXToStackPointer),
    TYA("Transfer Index Y to Accumulator", InstructionFunctions::transferRegisterYToAccumulator, StatusFlagsAffected.STANDARD);

    private final String description;
    private final StatusFlagsAffected statusFlagsAffected;
    private final NoArgumentInstruction noArgumentInstruction;
    private final NoArgumentWithReturnInstruction noArgumentWithReturnInstruction;
    private final SingleArgumentInstruction singleArgumentInstruction;
    private final BranchingInstruction branchingInstruction;
    private final AddressInstruction addressInstruction;
    private final WriteValue writeValue;

    Instructions(String description, NoArgumentInstruction noArgumentInstruction) {
        this.description = description;
        this.statusFlagsAffected = StatusFlagsAffected.NONE;
        this.noArgumentInstruction = noArgumentInstruction;
        noArgumentWithReturnInstruction = null;
        singleArgumentInstruction = null;
        branchingInstruction = null;
        addressInstruction = null;
        writeValue = WriteValue.None;
    }

    Instructions(String description, NoArgumentWithReturnInstruction noArgumentWithReturnInstruction, StatusFlagsAffected statusFlagsAffected) {
        this(description, noArgumentWithReturnInstruction, statusFlagsAffected, WriteValue.None);
    }

    Instructions(String description, NoArgumentWithReturnInstruction noArgumentWithReturnInstruction, StatusFlagsAffected statusFlagsAffected, WriteValue writeValue) {
        this.description = description;
        this.statusFlagsAffected = statusFlagsAffected;
        noArgumentInstruction = null;
        this.noArgumentWithReturnInstruction = noArgumentWithReturnInstruction;
        singleArgumentInstruction = null;
        branchingInstruction = null;
        addressInstruction = null;
        this.writeValue = writeValue;
    }

    Instructions(String description, StatusFlagsAffected statusFlagsAffected, SingleArgumentInstruction singleArgumentInstruction) {
        this(description, statusFlagsAffected, singleArgumentInstruction, WriteValue.None);
    }

    Instructions(String description, StatusFlagsAffected statusFlagsAffected, SingleArgumentInstruction singleArgumentInstruction, WriteValue writeValue) {
        this.description = description;
        this.statusFlagsAffected = statusFlagsAffected;
        noArgumentInstruction = null;
        noArgumentWithReturnInstruction = null;
        this.singleArgumentInstruction = singleArgumentInstruction;
        branchingInstruction = null;
        addressInstruction = null;
        this.writeValue = writeValue;
    }

    Instructions(String description, BranchingInstruction branchingInstruction) {
        this.description = description;
        this.statusFlagsAffected = StatusFlagsAffected.NONE;
        noArgumentInstruction = null;
        noArgumentWithReturnInstruction = null;
        singleArgumentInstruction = null;
        this.branchingInstruction = branchingInstruction;
        addressInstruction = null;
        this.writeValue = WriteValue.None;
    }

    Instructions(String description, AddressInstruction addressInstruction) {
        this.description = description;
        this.statusFlagsAffected = StatusFlagsAffected.NONE;
        noArgumentInstruction = null;
        noArgumentWithReturnInstruction = null;
        singleArgumentInstruction = null;
        this.branchingInstruction = null;
        this.addressInstruction = addressInstruction;
        this.writeValue = WriteValue.None;
    }
}
