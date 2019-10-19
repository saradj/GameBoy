package ch.epfl.gameboj.component.memory;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

/**
 * CS-108 
 * Ram.java 
 * Purpose: Representing the random access memory
 * 
 * @author Sara Djambazovska
 * @author Marouane Jaakik
 *
 */
public final class Ram {
    private final byte[] data;

    /**
     * Public constructor, constructs the random-access memory with the number
     * of bytes passed as an argument, throws {@link IllegalArgumentException}
     * if the argument size is negative
     * 
     * @param size
     *            integer value, positive, giving the number of bytes the
     *            random-access memory will be able to store
     * @throws IllegalArgumentException
     *             if the argument size is negative
     */
    public Ram(int size) {
        Preconditions.checkArgument(size >= 0);
        data = new byte[size];
    }

    /**
     * Returns the size of the random-access memory
     * 
     * @return integer value, the size of the random-access memory, meaning the
     *         size of the byte array it contains
     */
    public int size() {
        return data.length;
    }

    /**
     * Returns the 8 bit value stored at the position index in the random-access
     * memory
     * 
     * @param index
     *            integer value, the position from which we want to read the
     *            data stored in the random-access memory
     * @throws IndexOutOfBoundsException
     *             if the index is not in the range of the byte array
     *             representing the memory, meaning if it is not between 0 and
     *             the length of the memory (excluded)
     * @return integer value, the 8 bit value stored at the position index in
     *         the random-access memory
     */
    public int read(int index) {
        Objects.checkIndex(index, data.length);
        return Byte.toUnsignedInt(data[index]);
    }

    /**
     * Modifies the content of the random-access memory at the given index, by
     * writing the 8 bit value passed as a second argument
     * 
     * @param index
     *            integer value, the position in the byte array representing the
     *            random-access memory where we want to write the given value
     * @param value
     *            integer value of 8 bits to be written in the position index in
     *            the random-access memory
     * @throws IndexOutOfBoundsException
     *             if the index is not in the range of the byte array
     *             representing the memory, meaning if it is not between 0 and
     *             the length of the memory (excluded)
     * @throws IllegalArgumentException
     *             if the second argument value to be stored is not a 8 bit
     *             integer
     */
    public void write(int index, int value) {
        Objects.checkIndex(index, data.length);
        Preconditions.checkBits8(value);
        data[index] = (byte) value;
    }
}
