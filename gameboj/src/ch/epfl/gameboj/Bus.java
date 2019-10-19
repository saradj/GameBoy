package ch.epfl.gameboj;

import java.util.*;

import ch.epfl.gameboj.component.Component;

/**
 * CS-108 
 * Bus.java 
 * Purpose: Representing the bus of addresses and data,
 * connecting the {@link Component}s of {@link GameBoy} 
 * 
 * @author Sara Djambazovska
 * @author Marouane Jaakik
 *
 */
public final class Bus {

    private final ArrayList<Component> bus = new ArrayList<>();

    /**
     * Attaches the given argument component to the bus, throws
     * {@link NullPointerException} if the argument is null
     * 
     * @param component
     *            {@link Component} than should be attached to the bus if not
     *            null
     * @throws NullPointerException
     *             if the given argument component is null
     */
    public void attach(Component component) {
        Objects.requireNonNull(component);
        bus.add(component);
    }

    /**
     * Returns the value stored at the 16 bit address passed as an argument if
     * at least one of the components attached to the bus has a value stored at
     * this address, returns 0xFF otherwise
     * 
     * @param address
     *            integer value of 16 bits, the address at which we want to read
     *            the stored value
     * @throws IllegalArgumentException
     *             if the given argument address is not a value of 16 bits
     * @return the value stored at the address passed as an argument, if at
     *         least one of the components attached to the bus has a value
     *         stored at this address, returns 0xFF otherwise
     */
    public int read(int address) {
        Preconditions.checkBits16(address);
        int r;
        for (Component c : bus) {
            r=c.read(address);
            if (r != Component.NO_DATA)
                return r;
        }
        return 0xFF;
    }

    /**
     * Writes in all of the components attached to the bus, at the given
     * argument address the value data passed as a second argument, if the value
     * is 8 bits and the address is 16 bits
     * 
     * @param address
     *            integer value of 16 bits, the address at which we want to
     *            write the value data in all of the components attached to the
     *            bus
     *
     * @param data
     *            integer value of 8 bits that we want to store at the given
     *            address in all of the components attached to the bus
     * @throws IllegalArgumentException
     *             if the address is not a 16 bit value or if the data is not a
     *             value of 8 bits
     */
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        for (Component c : bus)
            c.write(address, data);
    }
}
