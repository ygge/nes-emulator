package nu.ygge.nes.emulator.ppu;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

class AddressRegisterTest {

    @Test
    void givenAddOneTo9727ThenGet9728() {
        var register = new AddressRegister();
        register.write((byte)0x25);
        register.write((byte)0xFF);
        Assertions.assertEquals(9727, register.get());

        register.add((byte)1);

        Assertions.assertEquals(9728, register.get());
    }

    @Test
    void givenAddOneTo255ThenGet256() {
        var register = new AddressRegister();
        register.write((byte)0);
        register.write((byte)0xFF);
        Assertions.assertEquals(255, register.get());

        register.add((byte)1);

        Assertions.assertEquals(256, register.get());
    }

    @Test
    void givenAddOneTo127ThenGet128() {
        var register = new AddressRegister();
        register.write((byte)0);
        register.write((byte)0x7F);
        Assertions.assertEquals(127, register.get());

        register.add((byte)1);

        Assertions.assertEquals(128, register.get());
    }
}
