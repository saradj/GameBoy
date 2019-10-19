package ch.epfl.gameboj.bits;

/**
 * CS-108 
 * Bit.java 
 * Purpose: To be implemented by the Enumerations representing a set of bits
 * 
 * @author Sara Djambazovska
 * @author Marouane Jaakik
 *
 */
public interface Bit {

    /**
     * Method that is automatically given by the {@link Enum}. Returns the
     * ordinal of this enumeration constant (its position in its enum
     * declaration, where the initial constant is assigned an ordinal of zero).
     * 
     * @return integer value, the ordinal of this enumeration constant
     */
    public abstract int ordinal();

    /**
     * Returns the same value as the method ordinal of {@link Enum}
     * 
     * @return integer value, the index of this enumeration constant
     */
    public default int index() {
        return ordinal();
    }

    /**
     * Returns the mask of the bit, an integer where the only 1 bit is at the
     * position given by the index of the {@link Bit}
     * 
     * @return integer value, the corresponding mask of the {@link Bit},
     *         containing only one 1 at the position determined by the index of
     *         the {@link Bit}
     */
    public default int mask() {
        return Bits.mask(this.index());
    }
}
