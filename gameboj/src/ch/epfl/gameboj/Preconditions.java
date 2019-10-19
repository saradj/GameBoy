package ch.epfl.gameboj;

/**
 * CS-108 
 * Preconditions.java 
 * Purpose: Facilitate the writing of preconditions
 * 
 * @author Sara Djambazovska
 * @author Marouane Jaakik
 *
 */
public interface Preconditions {

    /**
     * Checks if the argument is true, throws {@link IllegalArgumentException}
     * otherwise
     * 
     * @param b
     *            boolean value to be checked if true
     * @throws IllegalArgumentException
     *             if the given argument b is false
     */
    public static void checkArgument(boolean b) {
        if (!b)
            throw new IllegalArgumentException();
    }

    /**
     * Returns it's argument if it is in the range between 0 and 0xFF included,
     * meaning if it is an 8 bit value, throws {@link IllegalArgumentException}
     * otherwise
     * 
     * @param v
     *            integer value to be checked if it is 8 bits
     * @throws IllegalArgumentException
     *             if the argument v is not in the given range, if it is smaller
     *             than 0 or bigger than 0xFF, meaning if it is not of 8 bits
     * @return integer value, it's argument if it is 8 bits
     */
    public static int checkBits8(int v) {
         checkArgument(v >= 0x00 && v <= 0xFF);
         return v;
    }

    /**
     * Returns it's argument if it is in the range between 0 and 0xFFFF
     * included, meaning if it is an 16 bit value, throws
     * {@link IllegalArgumentException} otherwise
     * 
     * @param v
     *            integer value to be checked if it is 16 bits
     * @throws IllegalArgumentException
     *             if the argument v is not in the given range, if it is smaller
     *             than 0 or bigger than 0xFFFF, meaning if it is not of 16 bits
     * @return integer value, it's argument if it is 16 bits
     */
    public static int checkBits16(int v) {
        checkArgument(v >= 0x0000 && v <= 0xFFFF);
        return v;
    }

}
