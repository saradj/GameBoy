package ch.epfl.gameboj.component.cartridge;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOError;
import java.io.IOException;
import java.io.InputStream;
import java.util.Objects;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.memory.Rom;


/**
 * CS-108 
 * Cartridge.java 
 * Purpose: Representing a cartridge, not directly attached to the {@link Bus}, containing the memory bank controller 
 * and the {@link Rom} attached to it
 * 
 * @author Sara Djambazovska
 * @author Marouane Jaakik
 *
 */
public final class Cartridge implements Component {

    private Component mbc0;
    public static final int ADDRESS_TYPE_OF_CARTRIDGE = 0x147;

    /**
     * Private constructor that constructs the cartridge containing the
     * controller and the memory attached to it passed as an argument
     * 
     * @param mbc0
     *            {@link Component} the memory bank controller of type 0 to be
     *            stored in the {@link Cartridge}
     */
    private Cartridge(Component mbc0) {
        Objects.nonNull(mbc0);
        this.mbc0 = mbc0;
    }

    /**
     * Returns a {@link Cartridge} whose read-only memory, contained in the
     * memory bank controller of type 0, contains the bytes read from the
     * {@link File} given as an argument
     * 
     * @param romFile
     *            {@link File} that gives the content of the read-only memory
     * @return {@link Cartridge} whose read-only memory, contained in the memory
     *         bank controller of type 0, contains the bytes read from the
     *         {@link File} given as an argument
     * @throws IOException
     *             if there is an {@link IOError} including if the file does not
     *             exist
     * @throws IllegalArgumentException
     *             if the {@link File} does not contain 0 at the address giving
     *             the type of the cartridge
     */
    public static Cartridge ofFile(File romFile) throws IOException {
        try (InputStream s = new BufferedInputStream(
                new FileInputStream(romFile))) {
            byte[] data = new byte[MBC0.ROM_SIZE];
            data = s.readAllBytes();
            Preconditions.checkArgument(data[ADDRESS_TYPE_OF_CARTRIDGE] == 0);
            return new Cartridge(new MBC0(new Rom(data)));
        }
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        return mbc0.read(address);
    }

    
    @Override
    public void write(int adress, int data) {
        Preconditions.checkBits16(adress);
        Preconditions.checkBits8(data);
        mbc0.write(adress, data);
    }

}
