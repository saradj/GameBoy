package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.Component;

/**
 * CS-108
 * RamController.java 
 * Purpose: Represents a component controlling the access to the random access memory, 
 * implements {@link Component}
 * 
 * @author Sara Djambazovska
 * @author Marouane Jaakik
 *
 */
public final class RamController implements Component {
    
    private final Ram ram;
    private final int start, end;

    /**
     * Public constructor that constructs a controller for the random-access
     * memory, accessible from the argument startAddress (included) to the
     * second argument endAddress (excluded)
     * 
     * @param ram
     *            {@link Ram} random-access memory for which the controller is
     *            created
     * @param startAddress
     *            integer value, the start address from which we want to make
     *            the random-access memory accessible
     * @param endAddress
     *            integer value, the end address, excluded, up to which we want
     *            to make the random-access memory accessible
     * @throws NullPointerException
     *             if the {@link Ram} passed as an argument is null
     * @throws IllegalArgumentException
     *             if the startAddress or the endAddress are not values of 16
     *             bits, or if the range that they define is not included in the
     *             range of the random-access memory
     */
    public RamController(Ram ram, int startAddress, int endAddress) {
        Objects.requireNonNull(ram);
        Preconditions.checkBits16(endAddress);
        Preconditions.checkBits16(startAddress);
        Preconditions.checkArgument(endAddress - startAddress >= 0
                && endAddress - startAddress <= ram.size());
        start = startAddress;
        end = endAddress;
        this.ram = ram;
    }

    /**
     * Public constructor that constructs a controller for the random-access
     * memory, accessible from the argument startAddress (included) to the end
     * of the memory
     * 
     * @param ram
     *            {@link Ram} random-access memory for which the controller is
     *            created
     * @param startAddress
     *            integer value, the start address from which we want to make
     *            the random-access memory accessible
     * @throws NullPointerException
     *             if the {@link Ram} passed as an argument is null
     * @throws IllegalArgumentException
     *             if the startAddress is not a value of 16 bits
     */
    public RamController(Ram ram, int startAddress) {
        this(ram, startAddress, ram.size() + startAddress);

    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address >= start && address < end) {
            return ram.read(address - start);
        } else
            return NO_DATA;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        if (address >= start && address < end) {
            ram.write(address - start, data);
        }
    }
}
