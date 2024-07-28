package nu.ygge.nes.emulator;

import nu.ygge.nes.emulator.cpu.CPUUtil;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

public class NESRuntime_nestest {

    @Test
    void runTest() throws IOException {
        var bytes = getClass().getResource("/nestest.nes").openStream().readAllBytes();
        var logIn = new BufferedReader(new InputStreamReader(getClass().getResource("/nestest.log").openStream()));

        var runtime = new NESRuntime();
        runtime.loadGame(bytes, 0xC000, 0xC000);
        runtime.reset();
        runtime.getCpu().setStatusIgnored(); // this needs to be set for the logging to work, for some reason
        runtime.getCpu().setStackPointer((byte) 0xFD); // stack pointer does not seem to start at 0xFF
        runtime.getCpu().setCycles(7);

        String logRow;
        int instructions = 0;
        while ((logRow = logIn.readLine()) != null) {
            var expectedCounter = Integer.parseInt(logRow.substring(0, 4), 16);
            Assertions.assertEquals(expectedCounter, runtime.getCpu().getProgramCounter(), String.format("Wrong program counter after %d instructions", instructions));

            var expectedAccumulator = Integer.parseInt(logRow.substring(50, 52), 16);
            Assertions.assertEquals((byte) expectedAccumulator, runtime.getCpu().getAccumulator(), String.format("Accumulator is incorrect after %d instructions", instructions));
            var expectedRegisterX = Integer.parseInt(logRow.substring(55, 57), 16);
            Assertions.assertEquals((byte) expectedRegisterX, runtime.getCpu().getRegisterX(), String.format("Register X is incorrect after %d instructions", instructions));
            var expectedRegisterY = Integer.parseInt(logRow.substring(60, 62), 16);
            Assertions.assertEquals((byte) expectedRegisterY, runtime.getCpu().getRegisterY(), String.format("Register Y is incorrect after %d instructions", instructions));
            var expectedStatus = Integer.parseInt(logRow.substring(65, 67), 16);
            Assertions.assertEquals((byte) expectedStatus, runtime.getCpu().getStatusRegister(),
                    String.format("Status register is incorrect after %d instructions (expected %8s but got %8s)", instructions, Integer.toBinaryString(expectedStatus), Integer.toBinaryString(CPUUtil.toInt(runtime.getCpu().getStatusRegister()))));
            var expectedStackPointer = Integer.parseInt(logRow.substring(71, 73), 16);
            Assertions.assertEquals((byte) expectedStackPointer, runtime.getCpu().getStackPointer(), String.format("Stack pointer is incorrect after %d instructions", instructions));
            var expectedCycles = Integer.parseInt(logRow.substring(90));
            Assertions.assertEquals(expectedCycles, runtime.getCpu().getCycles(), String.format("Wrong number of cycles after %d instructions", instructions));

            runtime.performSingleInstruction();
            ++instructions;
        }
    }
}
