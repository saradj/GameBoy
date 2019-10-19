package ch.epfl.gameboj.component.cartridge;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;

/**
 * CS-108 
 * MBC0.java 
 * Purpose: Representing a memory bank controller of type 0, containing only a read-only memory of 0x8000 bytes
 * 
 * @author Sara Djambazovska
 * @author Marouane Jaakik
 *
 */
public final class MBC0 implements Component {

    private final Rom rom;
    public static final int ROM_SIZE = 0x8000;

    /**
     * Public constructor that constructs the memory bank controller of type 0
     * for the given read-only memory rom
     * 
     * @param rom
     *            {@link Rom} read-only memory for which the controller of type
     *            0 is constructed
     * @throws NullPointerException
     *             if the argument {@link Rom} rom is null
     * @throws IllegalArgumentException
     *             if the size of the given read-only memory is not equal to
     *             0x8000
     */
    public MBC0(Rom rom) {
        Objects.nonNull(rom);
        Preconditions.checkArgument(rom.size() == ROM_SIZE);
        this.rom = rom;
    }
   
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address >= 0 && address < ROM_SIZE) {
            return rom.read(address);
        } else
            return NO_DATA;
    }

    @Override
    public void write(int adress, int data) {
    }
}
