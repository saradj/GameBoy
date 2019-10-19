package ch.epfl.gameboj.component.memory;

import java.util.Arrays;
import java.lang.Byte;

import java.util.Objects;

/**
 * CS-108 
 * Rom.java 
 * Purpose: Representing the read-only memory
 * 
 * @author Sara Djambazovska
 * @author Marouane Jaakik
 *
 */
public final class Rom {

    private final byte[] data;

    /**
     * Public constructor that constructs a read-only memory, where the content
     * and size are equal to the ones passed by the byte array argument data,
     * throws {@link NullPointerException} if the argument is null
     * 
     * @param data
     *            byte array that determines the content and the size of the
     *            read-only memory we are constructing
     * @throws NullPointerException
     *             if the argument byte array is null
     */
    public Rom(byte[] data) {
        Objects.requireNonNull(data);
        this.data = Arrays.copyOf(data, data.length);
    }

    /**
     * Returns the size of the read-only memory, that is the length of the array
     * passed as an argument to it's constructor
     * 
     * @return integer value, the size of the read-only memory, meaning the size
     *         of the byte array it contains
     */
    public int size() {
        return data.length;
    }

    /**
     * Returns the 8 bit value stored at the position index in the read-only
     * memory
     * 
     * @param index
     *            integer value, the position from which we want to read the
     *            data stored in the read-only memory
     * @throws IndexOutOfBoundsException
     *             if the index is not in the range of the byte array
     *             representing the memory, meaning if it is not between 0 and
     *             the length of the memory (excluded)
     * @return integer value, the 8 bit value stored at the position index in
     *         the read-only memory
     */
    public int read(int index) {
        Objects.checkIndex(index, data.length);
        return Byte.toUnsignedInt(data[index]);
    }

}
