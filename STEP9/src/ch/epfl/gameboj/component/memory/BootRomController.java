package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cartridge.Cartridge;

/**
 * CS-108 
 * BootRamController.java 
 * Purpose: Representing a {@link Ram} controller, controlling the access to the cartridge,
 * by intercepting the reading in the range from 0 to 0xFF, 
 * until the boot {@link Ram} is not deactivated by writing in the address 0x147
 * 
 * @author Sara Djambazovska
 * @author Marouane Jaakik
 *
 */
public final class BootRomController implements Component {

    private Cartridge cartridge;
    private boolean disable = false;

    /**
     * Public constructor that constructs a boot read-only memory controller
     * that has attached the {@link Cartridge} given as an argument
     * 
     * @param cartridge
     *            {@link Cartridge} to be attached to the boot read-only memory
     *            controller
     * @throws NullPointerException
     *             if the argument {@link Cartridge} is null
     */
    public BootRomController(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);
        this.cartridge = cartridge;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        if (address >= 0 && address <= 0xFF && !disable)
            return Byte.toUnsignedInt(BootRom.DATA[address]);
        return cartridge.read(address);
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int adress, int data) {
        if (adress == AddressMap.REG_BOOT_ROM_DISABLE)
            disable = true;
        else
            cartridge.write(adress, data);
    }
}
