package ch.epfl.gameboj;

/**
 * CS-108 
 * Register.java
 * Purpose: To be implemented by the {@link Enum}erations
 * representing the registers of the same bank
 * 
 * @author Sara Djambazovska
 * @author Marouane Jaakik
 *
 */
public interface Register {

    /**
     * @see java.lang.Enum.ordinal()
     */
    public abstract int ordinal();

    /**
     * Returns the same value as the method ordinal of the {@link Enum} class,
     * the index of the register
     * 
     * @return integer value, the index of the register
     */
    public default int index() {
        return ordinal();
    }
}
