package nu.ygge.nes.emulator.cpu;

import nu.ygge.nes.emulator.NESRuntime;

public final class StackHelper {

    private StackHelper() { }

    public static void saveAddressToStack(NESRuntime runtime, int address) {
        pushToStack(runtime, (byte) (address >> 8));
        pushToStack(runtime, (byte) (address & 0xFF));
    }

    public static byte pullFromStack(NESRuntime runtime) {
        runtime.getCpu().incrementStackPointer();
        var address = getStackPointerAddress(runtime);
        return runtime.getBus().read(address);
    }

    public static void pushToStack(NESRuntime runtime, byte value) {
        var address = getStackPointerAddress(runtime);
        runtime.getCpu().decrementStackPointer();
        runtime.getBus().write(address, value);
    }

    public static int getStackPointerAddress(NESRuntime runtime) {
        int address = CPUUtil.toInt(runtime.getCpu().getStackPointer());
        return 0x100 | address;
    }
}
