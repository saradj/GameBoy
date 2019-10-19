package ch.epfl.gameboj.bits;

import java.util.Objects;

import static ch.epfl.gameboj.Preconditions.*;

/**
 * CS-108 
 * Bits.java
 * Purpose: Offers useful public static methods, performing
 * bitwise operations
 * 
 * @author Sara Djambazovska
 * @author Marouane Jaakik
 *
 */
public final class Bits {
    private static int[] reverse = new int[] { 0x00, 0x80, 0x40, 0xC0, 0x20, 0xA0, 0x60,
            0xE0, 0x10, 0x90, 0x50, 0xD0, 0x30, 0xB0, 0x70, 0xF0, 0x08,
            0x88, 0x48, 0xC8, 0x28, 0xA8, 0x68, 0xE8, 0x18, 0x98, 0x58,
            0xD8, 0x38, 0xB8, 0x78, 0xF8, 0x04, 0x84, 0x44, 0xC4, 0x24,
            0xA4, 0x64, 0xE4, 0x14, 0x94, 0x54, 0xD4, 0x34, 0xB4, 0x74,
            0xF4, 0x0C, 0x8C, 0x4C, 0xCC, 0x2C, 0xAC, 0x6C, 0xEC, 0x1C,
            0x9C, 0x5C, 0xDC, 0x3C, 0xBC, 0x7C, 0xFC, 0x02, 0x82, 0x42,
            0xC2, 0x22, 0xA2, 0x62, 0xE2, 0x12, 0x92, 0x52, 0xD2, 0x32,
            0xB2, 0x72, 0xF2, 0x0A, 0x8A, 0x4A, 0xCA, 0x2A, 0xAA, 0x6A,
            0xEA, 0x1A, 0x9A, 0x5A, 0xDA, 0x3A, 0xBA, 0x7A, 0xFA, 0x06,
            0x86, 0x46, 0xC6, 0x26, 0xA6, 0x66, 0xE6, 0x16, 0x96, 0x56,
            0xD6, 0x36, 0xB6, 0x76, 0xF6, 0x0E, 0x8E, 0x4E, 0xCE, 0x2E,
            0xAE, 0x6E, 0xEE, 0x1E, 0x9E, 0x5E, 0xDE, 0x3E, 0xBE, 0x7E,
            0xFE, 0x01, 0x81, 0x41, 0xC1, 0x21, 0xA1, 0x61, 0xE1, 0x11,
            0x91, 0x51, 0xD1, 0x31, 0xB1, 0x71, 0xF1, 0x09, 0x89, 0x49,
            0xC9, 0x29, 0xA9, 0x69, 0xE9, 0x19, 0x99, 0x59, 0xD9, 0x39,
            0xB9, 0x79, 0xF9, 0x05, 0x85, 0x45, 0xC5, 0x25, 0xA5, 0x65,
            0xE5, 0x15, 0x95, 0x55, 0xD5, 0x35, 0xB5, 0x75, 0xF5, 0x0D,
            0x8D, 0x4D, 0xCD, 0x2D, 0xAD, 0x6D, 0xED, 0x1D, 0x9D, 0x5D,
            0xDD, 0x3D, 0xBD, 0x7D, 0xFD, 0x03, 0x83, 0x43, 0xC3, 0x23,
            0xA3, 0x63, 0xE3, 0x13, 0x93, 0x53, 0xD3, 0x33, 0xB3, 0x73,
            0xF3, 0x0B, 0x8B, 0x4B, 0xCB, 0x2B, 0xAB, 0x6B, 0xEB, 0x1B,
            0x9B, 0x5B, 0xDB, 0x3B, 0xBB, 0x7B, 0xFB, 0x07, 0x87, 0x47,
            0xC7, 0x27, 0xA7, 0x67, 0xE7, 0x17, 0x97, 0x57, 0xD7, 0x37,
            0xB7, 0x77, 0xF7, 0x0F, 0x8F, 0x4F, 0xCF, 0x2F, 0xAF, 0x6F,
            0xEF, 0x1F, 0x9F, 0x5F, 0xDF, 0x3F, 0xBF, 0x7F, 0xFF, };

    // making the class non instantiable, by having a private constructor
    private Bits() {
    }

    /**
     * returns a mask given an index
     * 
     * @param index
     *            the index to be masked
     * @throws IndexOutOfBoundsException
     *             if index is not between 0 and 31 included
     * @return integer whose only bit 1 is at position index
     */
    public static int mask(int index) {
        Objects.checkIndex(index, Integer.SIZE);
        return 1 << index;
    }

    /**
     * Tests if the bit of the integer bits at position index is 1, if so it
     * returns true, else returns false
     * 
     * @param bits
     *            the integer whose bit we want to test
     * @param index
     *            the position of the bit to be tested in the integer bits
     * @throws IndexOutOfBoundsException
     *             if index is not between 0 and 31 included
     * @return boolean true of the given bit at position index is 1, false if it
     *         is 0
     */
    public static boolean test(int bits, int index) {
        Objects.checkIndex(index, Integer.SIZE);
    return ((bits >> index) & 1) != 0;
    }

    /**
     * Tests if the given bit in the integer bits is 1 or 0
     * 
     * @param bits
     *            an integer whose bit we want to test
     * @param bit
     *            a {@link Bit} that we want to test
     * @throws IndexOutOfBoundsException
     *             if index of the bit is not between 0 and 31 included
     * @return boolean true if the tested bit is 1, false if it is 0
     */
    public static boolean test(int bits, Bit bit) {
        Objects.checkIndex(bit.index(), Integer.SIZE);
        return test(bits, bit.index());
    }

    /**
     * Returns an integer where every bit is the same as the given argument
     * bits, except the bit at position index that is equal to the given
     * newValue
     * 
     * @param bits
     *            integer whose one bit we want to set to a newValue
     * @param index
     *            position of the bit we want to set in bits
     * @param newValue
     *            boolean if true we set the bit to 1, if false to 0
     * @throws IndexOutOfBoundsException
     *             if index not between 0 and 31 included
     * @return the modified integer bits, with the bit at position index set to
     *         newValue
     */
    public static int set(int bits, int index, boolean newValue) {
        Objects.checkIndex(index, Integer.SIZE);
        if (newValue)
            return (bits | (1 << index));
        return (bits & (~(1 << index)));
    }

    /**
     * Clips the integer bits, so that it returns only the size least
     * significant bits
     * 
     * @param size
     *            integer giving the number of least significant bits to clip
     * @param bits
     *            integer whose size least significant bits are extracted
     * @throws IllegalArgumentException
     *             if size is not between 0 and 32 included
     * @return integer having only the size least significant bits from bits and
     *         the rest of the bits are 0
     */
    public static int clip(int size, int bits) {
        checkArgument(size >= 0 && size <= Integer.SIZE);
        if (size == Integer.SIZE)
            return bits;
        return bits & (~(-1 << size));
    }

    /**
     * Extracts size bits starting from position start, from the integer bits,
     * 
     * @param bits
     *            integer whose bits we want to extract
     * @param start
     *            the start position from which we want to extract the bits
     * @param size
     *            the number of bits we want to extract
     * @throws IndexOutOfBoundsException
     *             if start+size is bigger than the last position of a bit, 31
     * @return integer whose least significant bits are the ones extracted from
     *         bits
     */
    public static int extract(int bits, int start, int size) {
        Objects.checkFromIndexSize(start, size, Integer.SIZE);
        int isolatedBits = bits >> (start);
        return clip(size, isolatedBits);

    }

    /**
     * Rotates the size least significant bits from the integer bits in the
     * given direction and returns the result
     * 
     * @param size
     *            integer giving the number of least significant bits from bits
     *            to be rotated
     * @param bits
     *            integer whose size least significant bits are rotated
     * @param distance
     *            integer giving the direction and distance of rotation, to the
     *            left if positive, to the right if negative
     * @throws IllegalArgumentException
     *             if the size is not between 0 and 32 included, or if the value
     *             bits does not contain exactly size bits
     * @return integer whose bits are obtained by rotating the size least
     *         significant bits from bits
     */
    public static int rotate(int size, int bits, int distance) {
        checkArgument(size > 0 && size <= 32);
        if (size != Integer.SIZE)
            checkArgument((bits >> size) == 0);
        int modDistance = Math.floorMod(distance, size);
        int isolatedBits = clip(size, (bits << modDistance));
        return (isolatedBits | (bits >>> (size - modDistance)));
    }

    /**
     * Extends the sign of the 8 bits integer b, by copying the 7th bit to the
     * bits 8 to 31 of the returned value
     * 
     * @param b
     *            integer of 8 bits, whose sign we want to extend
     * @throws IllegalArgumentException
     *             if b is not 8 bits
     * @return integer with the same 8 least significant bits as b, but the bits
     *         8 to 31 are the same as the bit 7
     */
    public static int signExtend8(int b) {
        checkBits8(b);
        byte y = (byte) b;
        int z = (int) y;
        return z;
    }

    /**
     * Reverses the 8 least significant bits of the given value b, swaps bit 0
     * and bit 7, bit 1 and 6, 2 and 5, 3 and 4.
     * 
     * @param b
     *            integer of 8 bits whose bits we are swapping
     * @throws IllegalArgumentException
     *             if b is not 8 bits
     * @return integer with the same 8 LSB of b, but they are reversed
     */
    public static int reverse8(int b) {
        checkBits8(b);
        return reverse[b];

    }

    /**
     * Returns one's complement if the given 8 bit value
     * 
     * @param b
     *            integer of 8 bits whose 8 LSB will be switched 1 to 0 and 0 to
     *            1
     * @throws IllegalArgumentException
     *             if b is not 8 bits
     * @return integer one's complement of b for the 8 LSB
     */
    public static int complement8(int b) {
        checkBits8(b);
        return ~b & ((1 << 8) - 1);
    }

    /**
     * Returns a value of 16 bits by concatenating the 8 bits from highB as MSB,
     * and the 8 bits from lowB as LSB
     * 
     * @param highB
     *            integer of 8 bits, that we use as least significant bits in
     *            the returned value
     * @param lowB
     *            integer of 8 bits, that we use as most significant bits in the
     *            returned value
     * @throws IllegalArgumentException
     *             if lowB or highB is not 8 bits
     * @return integer of 16 bits whose LSB are the 8 bits from lowB, and MSB
     *         are the ones from highB
     */
    public static int make16(int highB, int lowB) {
        checkBits8(lowB);
        checkBits8(highB);
        return (highB << 8) | (lowB);
    }

}
