package ch.epfl.gameboj.bits;

import java.util.Arrays;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

/**
 * CS-108 BitVector.java 
 * Purpose: A vector of bits of length multiple of 32
 * 
 * @author Sara Djambazovska
 * @author Marouane Jaakik
 *
 */
public final class BitVector {

    private final int[] vector;

    private BitVector(int[] v) {
        Objects.requireNonNull(v);
        vector = v;
    }

    /**
     * Constructs a vector of bits of the given size
     * 
     * @param sizeOfBits
     * @param value
     *            boolean, giving the initial value of the vector, either a
     *            vector of zeros or ones
     */
    public BitVector(int sizeOfBits, boolean value) {
        Preconditions.checkArgument(
                sizeOfBits % Integer.SIZE == 0 && sizeOfBits > 0);
        vector = new int[Math.floorDiv(sizeOfBits, Integer.SIZE)];
        if (value) {
            Arrays.fill(vector, -1);
        }
    }

    /**Constructs a vector of zeros of the given size
     * @param sizeOfBits
     */
    public BitVector(int sizeOfBits) {
        this(sizeOfBits, false);
    }

    /**
     * Returns the size of the Bit Vector
     * 
     * @return integer: number of bits in the Bit Vector
     */
    public int size() {
        return vector.length * Integer.SIZE;
    }

    /**
     * Tests the value of the bit of the given index in the Bit Vector
     * 
     * @param index:
     *            integer the index of the bit to be tested
     * @throws IndexOutOfBoundsException
     *             if the given index is not in the range of the vector
     * @return boolean: true if the tested bit is 1, false otherwise
     */
    public boolean testBit(int index) {
        Objects.checkIndex(index, size());
        return Bits.test(vector[Math.floorDiv(index, Integer.SIZE)],
                index % Integer.SIZE);
    }

    /**
     * Returns the complement of the Bit Vector
     * 
     * @return {@link BitVector}: the complement of the Bit Vector
     */
    public BitVector not() {
        int[] v = new int[vector.length];
        for (int i = 0; i < vector.length; i++)
            v[i] = ~vector[i];

        return new BitVector(v);
    }

    /**
     * Returns the conjunction of two Bit Vectors
     * 
     * @param v2
     *            {@link BitVector}: the Bit Vector to be conjuncted
     * @throws IllegalArgumentException
     *             if the two Bit Vectors have different number of bits
     * @return {@link BitVector}: the resulting conjunction of the two Bit
     *         Vectors
     */
    public BitVector and(BitVector v2) {
        Preconditions.checkArgument(v2.vector.length == this.vector.length);
        int[] res = new int[vector.length];
        for (int i = 0; i < vector.length; i++)
            res[i] = this.vector[i] & v2.vector[i];
        return new BitVector(res);
    }

    /**
     * Returns the disjunction of two Bit Vectors
     * 
     * @param v2
     *            {@link BitVector}: the Bit Vector with which we are doing the
     *            disjunction
     * @throws IllegalArgumentException
     *             if the two Bit Vectors have different number of bits
     * @return {@link BitVector}: the resulting disjunction of the two Bit
     *         Vectors
     */

    public BitVector or(BitVector v2) {
        Preconditions.checkArgument(v2.size() == this.size());
        int[] res = new int[v2.vector.length];
        for (int i = 0; i < vector.length; i++)
            res[i] = this.vector[i] | v2.vector[i];
        return new BitVector(res);
    }

    /**
     * Enumeration of the Extraction Types
     *
     */
    public enum ExtractionType {
        ZeroExtended, Wrapped
    }

    private BitVector extract(int index, int sizeOfArray, ExtractionType type) {
        int div = Math.floorDiv(index, Integer.SIZE);
        int mod = Math.floorMod(index, Integer.SIZE);
        int[] extracted = new int[sizeOfArray];

        if (Math.floorMod(index, Integer.SIZE) == 0) {
            for (int i = 0; i < sizeOfArray; i++)
                extracted[i] = elementOfInfinite(type, div + i);
            return new BitVector(extracted);
        } else
            for (int i = 0; i < sizeOfArray; i++)
                extracted[i] = elementOfInfinite(type, (div + i)) >>> mod
                        | elementOfInfinite(type,
                                (div + 1 + i)) << (Integer.SIZE - mod);
        return new BitVector(extracted);

    }

    private int elementOfInfinite(ExtractionType type, int index) {
        if (index >= 0 && index < vector.length)
            return vector[index];
        if (type == ExtractionType.ZeroExtended) {
            return 0;
        } else {
            return vector[Math.floorMod(index, vector.length)];
        }
    }

    /**
     * Returns the extracted vector by doing a zero extension
     * 
     * @param index
     *            integer: the index at which the extraction starts
     * @param sizeOfBits
     *            integer: the number of bits to be extracted
     * @throws IllegalArgumentException
     *             if the number of bits to be extracted is not a positive
     *             multiple of 32
     * @return {@link BitVector}: the extracted Bit Vector
     */
    public BitVector extractZeroExtended(int index, int sizeOfBits) {
        Preconditions.checkArgument(
                sizeOfBits % Integer.SIZE == 0 && sizeOfBits > 0);
        return extract(index, Math.floorDiv(sizeOfBits, Integer.SIZE),
                ExtractionType.ZeroExtended);
    }

    /**
     * Returns the extracted vector by wrapping
     * 
     * @param index
     *            integer: the index at which the extraction starts
     * @param sizeOfBits
     *            integer: the number of bits to be extracted
     * @throws IllegalArgumentException
     *             if the number of bits to be extracted is not a positive
     *             multiple of 32
     * @return {@link BitVector}: the extracted Bit Vector
     */
    public BitVector extractWrapped(int index, int sizeOfBits) {
        Preconditions.checkArgument(
                sizeOfBits % Integer.SIZE == 0 && sizeOfBits > 0);
        return extract(index, Math.floorDiv(sizeOfBits, Integer.SIZE),
                ExtractionType.Wrapped);
    }

    /**
     * Shifts the given Bit Vector for i places
     * 
     * @param i
     *            integer: giving the amount of shifting, the shifting will be
     *            towards the left if the argument is positive, otherwise
     *            towards the right
     * @return {@link BitVector}: the shifted vector
     */
    public BitVector shift(int distance) {
        return extractZeroExtended(-distance, size());
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (int i = vector.length - 1; i >= 0; i--)
            sb.append(String.format("%32s", Integer.toBinaryString(vector[i]))
                    .replace(' ', '0'));
        return sb.toString();
    }

    @Override
    public boolean equals(Object o) {
        return (o instanceof BitVector)
                && Arrays.equals(this.vector, ((BitVector) o).vector);
    }

    @Override
    public int hashCode() {
        return Arrays.hashCode(this.vector);
    }

    /**
     * Represents a Builder of the {@link BitVector} allowing the construction
     * of the {@link BitVector} byte by byte
     */
    public static final class Builder {

        private int[] vector;
        private static final int BYTE_MASK = 0xFF;

        private static final int NUMBER_OF_BYTES = Integer.SIZE / Byte.SIZE;

        public Builder(int sizeOfBits) {
            Preconditions.checkArgument(
                    sizeOfBits % Integer.SIZE == 0 && sizeOfBits > 0);
            vector = new int[sizeOfBits / Integer.SIZE];
        }

        /**
         * Sets the value of the byte passed as a second argument at the given
         * byte index in the {@link BitVector}, and returns the builder itself
         * 
         * @param index
         *            integer: the position where to set the byte value
         * @param valueByte
         *            integer: 8 bits value to be set
         * @throws IndexOutOfBoundsException
         *             if the index is not in the range of the number of bytes
         *             of the {@link BitVector}
         * @throws IllegalArgumentException
         *             if the second argument, the value is not of 8 bits
         * @throws IllegalStateException
         *             when trying to set a byte in the {@link BitVector} after
         *             building it
         * @return {@link Builder}
         */
        public Builder setByte(int index, int valueByte) {
            if (!Objects.nonNull(vector))
                throw new IllegalStateException();
            Objects.checkIndex(index, vector.length * NUMBER_OF_BYTES);
            Preconditions.checkBits8(valueByte);
            if (!Objects.nonNull(vector))
                throw new IllegalStateException();
            else {
                vector[Math.floorDiv(index, NUMBER_OF_BYTES)] = (vector[Math
                        .floorDiv(index, NUMBER_OF_BYTES)]
                        & ~(BYTE_MASK << (index * Byte.SIZE) % Integer.SIZE))
                        | (valueByte << (index * Byte.SIZE) % Integer.SIZE);
                return this;
            }
        }

        /**
         * Builds the {@link BitVector}
         * 
         * @return {@link BitVector}: the builded Bit Vector
         */
        public BitVector build() {
            if (!Objects.nonNull(vector))
                throw new IllegalStateException();
            BitVector build = new BitVector(vector);
            this.vector = null;
            return build;
        }
    }
}
