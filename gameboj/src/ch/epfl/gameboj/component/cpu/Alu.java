package ch.epfl.gameboj.component.cpu;

import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

/**
 * CS-108 
 * Alu.java 
 * Purpose: Representing the Arithmetic Logic Unit, containing
 * mostly static methods performing arithmetic operations on 8 bit and 16 bit
 * values, and returning the value packed with the flags produced
 * 
 * @author Sara Djambazovska
 * @author Marouane Jaakik
 *
 */
public final class Alu {

    
    private Alu() {}
    /**
     * Enumeration of the Flags
     *
     */
    public enum Flag implements Bit {
        UNUSED_0,
        UNUSED_1,
        UNUSED_2,
        UNUSED_3,
        /**
         * Flag C representing the carry produced by the addition or subtraction
         * of 8 bit values
         */
        C,
        /**
         * Flag H representing the half-carry produced by the addition or
         * subtraction of 4 least significant bits
         */
        H,
        /**
         * Flag N, true if the operation was a subtraction
         */
        N,
        /**
         * Flag Z - zero, true if the operation resulted in a zero
         */
        Z
    }

    /**
     * Enumeration of Rotation Directions
     *
     */
    public enum RotDir {
        /**
         * Left rotation direction
         */
        LEFT,
        /**
         * Right rotation direction
         */
        RIGHT
    }

    /**
     * Returns a value with the bits corresponding to the flags are 1 iff the
     * flag is true
     * 
     * @param z
     *            boolean value indicating whether the flag z of index 7 is true
     * @param n
     *            boolean value indicating whether the flag n of index 6 is true
     * @param h
     *            boolean value indicating whether the flag h of index 5 is true
     * @param c
     *            boolean value indicating whether the flag c of index 4 is true
     * @return integer whose bits corresponding to the index of the flags are 1
     *         only if the boolean value of the flag is true
     */
    public static int maskZNHC(boolean z, boolean n, boolean h, boolean c) {
        int R = packValueZNHC(0, z, n, h, c);
        return R;
    }

    /**
     * Returns the value in the packet value and flags
     * 
     * @param valueFlags
     *            integer containing the packet value and flags
     * @return integer the value contained in the packet valueFlags
     */
    public static int unpackValue(int valueFlags) {
        int R = valueFlags >> 8;
        return R;
    }

    /**
     * Returns the flags contained in the packet value and flags
     * 
     * @param valueFlags
     *            integer containing the packet value and flags
     * @return integer the flags from the valueFlags
     */
    public static int unpackFlags(int valueFlags) {
        int R = valueFlags & 0xff;
        return R;
    }

    /**
     * Returns the sum of the two given 8 bit integers taking into account the
     * initial carry c0, packed with the flags
     * 
     * @param l
     *            integer 8 bits to be summed
     * @param r
     *            integer 8 bits to be summed
     * @param c0
     *            boolean initial carry, adding 1 to the sum if true
     * @throws IllegalArgumentException
     *             if r or l are not 8 bit integers
     * @return integer the packed value and flags from the sum of l, r and c0
     */
    public static int add(int l, int r, boolean c0) {
        Preconditions.checkBits8(r);
        Preconditions.checkBits8(l);
        int sum = c0 ? r + l + 1 : r + l;
        boolean h = c0 ? Bits.clip(4, l) + Bits.clip(4, r) + 1 > 0xf
                : Bits.clip(4, l) + Bits.clip(4, r) > 0xf;
        return packValueZNHC(Bits.clip(8, sum), Bits.clip(8, sum) == 0, false,
                h, sum > 0xff);
    }

    /**
     * Returns the sum of the two given 8 bit integers not taking into account
     * the initial carry, packed with the flags
     * 
     * @param l
     *            integer 8 bits to be summed
     * @param r
     *            integer 8 bits to be summed
     * @throws IllegalArgumentException
     *             if r or l are not 8 bit integers
     * @return integer the packed value and flags from the sum of l and r
     */
    public static int add(int l, int r) {
        return add(l, r, false);
    }

    /**
     * Returns the sum of the two given 16 bit integers, packed with the flags
     * 00HC corresponding to the addition of the 8 LSB
     * 
     * @param l
     *            integer 16 bits to be summed
     * @param r
     *            integer 16 bits to be summed
     * @throws IllegalArgumentException
     *             if r or l are not 16 bit integers
     * @return integer the packed value and flags from the 16 bit sum of l and r
     */
    public static int add16L(int l, int r) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);
        boolean h = Bits.clip(4, l) + Bits.clip(4, r) > 0xf;
        boolean c = Bits.clip(8, l) + Bits.clip(8, r) > 0xff;
        return packValueZNHC(Bits.clip(16, l + r), false, false, h, c);
    }

    /**
     * Returns the sum of the two given 16 bit integers, packed with the flags
     * 00HC corresponding to the addition of the 8 MSB taking into account the
     * carry from the addition of the 8 LSB
     * 
     * @param l
     *            integer 16 bits to be summed
     * @param r
     *            integer 16 bits to be summed
     * @throws IllegalArgumentException
     *             if r or l are not 16 bit integers
     * @return integer the packed value and flags from the 16 bit sum of l and r
     */
    public static int add16H(int l, int r) {
        Preconditions.checkBits16(l);
        Preconditions.checkBits16(r);
        int LSBsum = add(Bits.clip(8, l), Bits.clip(8, r));
        boolean carry = Bits.test(LSBsum, 4);
        int MSBsum = add(Bits.extract(l, 8, 8), Bits.extract(r, 8, 8), carry);
        return packValueZNHC(Bits.clip(16, l + r), false, false,
                Bits.test(MSBsum, 5), Bits.test(MSBsum, 4));
    }

    /**
     * Returns the subtraction of the two given 8 bit integers taking into
     * account the initial carry b0, packed with the flags
     * 
     * @param l
     *            integer 8 bits to be subtracted
     * @param r
     *            integer 8 bits to be subtracted
     * @param b0
     *            boolean initial carry, subtracting 1 to the sum if true
     * @throws IllegalArgumentException
     *             if r or l are not 8 bit integers
     * @return integer the packed value and flags from the subtraction of l, r
     *         and b0
     */
    public static int sub(int l, int r, boolean b0) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int sub = b0 ? Bits.clip(8, l - r - 1) : Bits.clip(8, l - r);
        boolean h = b0 ? Bits.clip(4, l) - 1 < Bits.clip(4, r)
                : Bits.clip(4, l) < Bits.clip(4, r);
        boolean c = b0 ? (l - 1) < r : l < r;
        return packValueZNHC(sub, sub == 0, true, h, c);
    }

    /**
     * Returns the subtraction of the two given 8 bit integers not taking into
     * account the initial carry b0, packed with the flags
     * 
     * @param l
     *            integer 8 bits to be subtracted
     * @param r
     *            integer 8 bits to be subtracted
     * @throws IllegalArgumentException
     *             if r or l are not 8 bit integers
     * @return integer the packed value and flags from the subtraction of l and
     *         r
     */
    public static int sub(int l, int r) {
        return sub(l, r, false);
    }

    /**
     * Returns the value v shifted to the left for one bit, packed with the
     * flags Z00C where C contains the bit ejected by the shifting
     * 
     * @param v
     *            integer 8 bits to be shifted for one bit to the left
     * @throws IllegalArgumentException
     *             if v is not an 8 bit integer
     * @return the packed value of the shifted integer v with the flags
     *         resulting from the shifting
     */
    public static int shiftLeft(int v) {
        Preconditions.checkBits8(v);
        int shifted = Bits.clip(8, v << 1);
        boolean c = Bits.test(v, 7);
        return packValueZNHC(shifted, shifted == 0, false, false, c);
    }

    /**
     * Returns the value v shifted to the right arithmetically for one bit,
     * packed with the flags Z00C where C contains the bit ejected by the
     * shifting
     * 
     * @param v
     *            integer 8 bits to be shifted for one bit to the right
     * @throws IllegalArgumentException
     *             if v is not an 8 bit integer
     * @return the packed value of the shifted integer v with the flags
     *         resulting from the shifting
     */
    public static int shiftRightA(int v) {
        Preconditions.checkBits8(v);
        int shifted = Bits.set(v >>> 1, 7, Bits.test(v >>> 1, 6));
        boolean c = Bits.test(v, 0);
        return packValueZNHC(shifted, shifted == 0, false, false, c);
    }

    /**
     * Returns the value v shifted to the right logically for one bit, packed
     * with the flags Z00C where C contains the bit ejected by the shifting
     * 
     * @param v
     *            integer 8 bits to be shifted for one bit to the right
     * @throws IllegalArgumentException
     *             if v is not an 8 bit integer
     * @return the packed value of the shifted integer v with the flags
     *         resulting from the shifting
     */
    public static int shiftRightL(int v) {
        Preconditions.checkBits8(v);
        int shifted = v >>> 1;
        boolean c = Bits.test(v, 0);
        return packValueZNHC(shifted, shifted == 0, false, false, c);
    }

    /**
     * Returns the value v rotated through carry c in the direction d, packed
     * with the flags Z00C where C is the MSB of the rotated value
     * 
     * @param d
     *            the direction of the rotation
     * @param v
     *            integer 8 bits, first turned into a 9 bit number by adding the
     *            carry c as MSB, then rotated
     * @param c
     *            boolean indicating if there is a carry during the rotation, if
     *            true c=1
     * @throws IllegalArgumentException
     *             if the integer v is not 8 bits
     * @return integer packed value and flags, 8 bit value of the 8 LSB of the
     *         rotated value through the carry c, and flags Z00C
     */
    public static int rotate(RotDir d, int v, boolean c) {
        Preconditions.checkBits8(v);
        int va = c ? (1 << 8 | v) : v;
        int rotated = (d == RotDir.LEFT) ? Bits.rotate(9, va, 1)
                : Bits.rotate(9, va, -1);
        boolean c1 = Bits.test(rotated, 8);
        return packValueZNHC(Bits.clip(8, rotated), Bits.clip(8, rotated) == 0,
                false, false, c1);
    }

    /**
     * Returns the value v rotated in the direction d, packed with the flags
     * Z00C where C is the bit that is passed from one side to the pther during
     * the rotation
     * 
     * @param d
     *            the direction of the rotation
     * @param v
     *            integer 8 bits, to be rotated
     * @throws IllegalArgumentException
     *             if the integer v is not 8 bits
     * @return integer the packed rotated value v with the flags resulting from
     *         the rotation
     */
    public static int rotate(RotDir d, int v) {
        Preconditions.checkBits8(v);
        int rotated = (d == RotDir.LEFT) ? Bits.clip(8, Bits.rotate(8, v, 1))
                : Bits.clip(8, Bits.rotate(8, v, -1));
        boolean c = (d == RotDir.LEFT) ? Bits.test(v, 7) : Bits.test(v, 0);
        return packValueZNHC(rotated, rotated == 0, false, false, c);
    }

    /**
     * Returns the packed value obtained by swapping the 4 LSB with the 4 MSB of
     * the given integer and the flags resulting from the swap Z000
     * 
     * @param v
     *            integer 8 bits to be swapped
     * @throws IllegalArgumentException
     *             if the value v is not 8 bits
     * @return integer the packed value obtained by swapping the 4 LSB with the
     *         4 MSB of the given integer v and the flags resulting from the
     *         swap Z000
     */
    public static int swap(int v) {
        Preconditions.checkBits8(v);
        int LSB = Bits.clip(4, v);
        int MSB = Bits.extract(v, 4, 4);
        int res = (LSB << 4) | MSB;
        return packValueZNHC(res, res == 0, false, false, false);
    }

    /**
     * Returns the value 0 and the flags Z010
     * 
     * @param v
     *            integer 8 bits to be tested if its bit at the index bitIndex
     *            is 0 or 1
     * @param bitIndex
     *            the index of the bit to be tested from the integer v
     * @throws IllegalArgumentException
     *             if the value v is not 8 bits
     * @throws IndexOutOfBoundsException
     *             if the bitIndex is not between 0 and 7 included
     * @return integer the packed value 0 and the flags Z010 where z is true if
     *         the tested bit from the value v at the index bitIndex is 0
     */
    public static int testBit(int v, int bitIndex) {
        Preconditions.checkBits8(v);
        Objects.checkIndex(bitIndex, 8);
        boolean z = !Bits.test(v, bitIndex);
        return packValueZNHC(0, z, false, true, false);
    }

    /**
     * Returns the value adjusted in the format DCB with the flags ZN0C
     * resulting
     * 
     * @param v
     *            integer 8 bits the value to be adjusted
     * @param n
     *            boolean the flag n
     * @param h
     *            boolean the flag h
     * @param c
     *            boolean the flag c
     * @throws IllegalArgumentException
     *             if the integer v is not 8 bits
     * @return Integer the adjusted value v in the format DCB with the flags
     *         ZN0C
     */
    public static int bcdAdjust(int v, boolean n, boolean h, boolean c) {
        Preconditions.checkBits8(v);
        boolean fixL = h | (!n & (Bits.clip(4, v) > 0x9));
        boolean fixH = c | (!n & v > 0x99);
        int fix = 0;
        if (fixH)
            fix += 0x60 * 1;
        if (fixL)
            fix += 0x06 * 1;
        int value = n ? v - fix : v + fix;
        return packValueZNHC(Bits.clip(8, value), Bits.clip(8, value) == 0, n,
                false, fixH);
    }

    /**
     * Returns the packed value of the bit to bit and of the two 8 bit integers
     * and the flags resulting Z010
     * 
     * @param l
     *            integer 8 bits
     * @param r
     *            integer 8 bits
     * @throws IllegalArgumentException
     *             if the integers l or r are not 8 bits
     * @return Integer the value of bit to bit and of l and r, packed with the
     *         resulting flags
     */
    public static int and(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int v = Bits.clip(8, l & r);
        return packValueZNHC(v, v == 0, false, true, false);
    }

    /**
     * Returns the packed value of the bit to bit or of the two 8 bit integers
     * and the flags resulting Z000
     * 
     * @param l
     *            integer 8 bits
     * @param r
     *            integer 8 bits
     * @throws IllegalArgumentException
     *             if the integers l or r are not 8 bits
     * @return Integer the value of bit to bit or of l and r, packed with the
     *         resulting flags
     */
    public static int or(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int v = Bits.clip(8, l | r);
        return packValueZNHC(v, v == 0, false, false, false);
    }

    /**
     * Returns the packed value of the bit to bit xor of the two 8 bit integers
     * and the flags resulting Z000
     * 
     * @param l
     *            integer 8 bits
     * @param r
     *            integer 8 bits
     * @throws IllegalArgumentException
     *             if the integers l or r are not 8 bits
     * @return Integer the value of bit to bit xor of l and r, packed with the
     *         resulting flags
     */
    public static int xor(int l, int r) {
        Preconditions.checkBits8(l);
        Preconditions.checkBits8(r);
        int v = Bits.clip(8, l ^ r);
        return packValueZNHC(v, v == 0, false, false, false);
    }

    // method to pack the value and the flags in one integer of 16 bits
    private static int packValueZNHC(int v, boolean z, boolean n, boolean h,
            boolean c) {
        int R = v << 8;
        if (c)
            R = R | Flag.C.mask();
        if (h)
            R = R | Flag.H.mask();
        if (n)
            R = R | Flag.N.mask();
        if (z)
            R = R | Flag.Z.mask();
        return R;
    }

}
