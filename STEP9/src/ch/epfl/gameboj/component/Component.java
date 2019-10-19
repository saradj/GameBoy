package ch.epfl.gameboj.component;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.GameBoy;

/**
 * CS-108
 * Component.java
 * Purpose: Representing a component of {@link GameBoy},
 * connected to the {@link Bus} of addresses and data, implemented by all of the
 * classes representing such a component
 * 
 * @author Sara Djambazovska
 * @author Marouane Jaakik
 *
 */
public interface Component {

    public static final int NO_DATA = 0x100;

    /**
     * Returns the value stored at the 16 bit address passed as an argument if
     * the component has a value stored at this address, returns the final
     * attribute NO_DATA otherwise
     * 
     * @param address
     *            integer value of 16 bits, the address at which we want to read
     *            the stored value
     * @throws IllegalArgumentException
     *             if the given argument address is not a value of 16 bits
     * @return the value stored at the address passed as an argument, if the
     *         component has a value stored at this address, returns NO_DATA
     *         otherwise
     */
    public abstract int read(int address);

    /**
     * Writes at the given argument address in the component, the value data
     * passed as a second argument, or it does not do anything if the component
     * does not allow to store data at this address, if the value is not 8 bits
     * or the address is not 16 bits throws {@link IllegalArgumentException}
     * 
     * @param address
     *            integer value of 16 bits, the address at which we want to
     *            write the value data in the component
     * 
     * @param data
     *            integer value of 8 bits that we want to store at the given
     *            address in all of the components attached to the bus
     * @throws IllegalArgumentException
     *             if the address is not a 16 bit value or if the data is not a
     *             value of 8 bits
     */
    public abstract void write(int adress, int data);

    /**
     * Attaches the component to the bus by calling the method attach from
     * {@link Bus}
     * 
     * @param bus
     *            {@link Bus} on which we want to attach the component
     */
    public default void attachTo(Bus bus) {
        bus.attach(this);
    }
}
