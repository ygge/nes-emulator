package nu.ygge.nes.emulator.input;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class ControllerTest {

    private Controller controller;

    @BeforeEach
    void setUp() {
        controller = new Controller();
    }

    @Test
    void verifyButtonsAreClockedOutInHardwareOrder() {
        controller.setPressed(Button.A, true);
        controller.setPressed(Button.START, true);
        controller.setPressed(Button.RIGHT, true);

        latch();

        Assertions.assertArrayEquals(new int[]{1, 0, 0, 1, 0, 0, 0, 1}, readAllButtons());
    }

    @Test
    void verifyReleasingAButtonIsPickedUpByTheNextLatch() {
        controller.setPressed(Button.A, true);
        latch();
        Assertions.assertEquals(1, controller.read());

        controller.setPressed(Button.A, false);
        latch();
        Assertions.assertEquals(0, controller.read());
    }

    @Test
    void verifyTheFirstButtonIsRepeatedWhileTheStrobeIsHigh() {
        controller.setPressed(Button.A, true);
        controller.write((byte) 1);

        Assertions.assertEquals(1, controller.read());
        Assertions.assertEquals(1, controller.read());
        Assertions.assertEquals(1, controller.read());
    }

    @Test
    void verifyReadingPastTheLastButtonReportsAPressedButton() {
        latch();
        readAllButtons();

        Assertions.assertEquals(1, controller.read());
        Assertions.assertEquals(1, controller.read());
    }

    private void latch() {
        controller.write((byte) 1);
        controller.write((byte) 0);
    }

    private int[] readAllButtons() {
        var values = new int[8];
        for (int i = 0; i < values.length; ++i) {
            values[i] = controller.read();
        }
        return values;
    }
}
