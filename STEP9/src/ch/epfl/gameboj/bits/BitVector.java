package ch.epfl.gameboj.bits;

import java.util.Arrays;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;

public final class BitVector {
    private final int size;
    private final int[] vector;

    private BitVector(int[] v) {
        size = v.length * Integer.SIZE;
        vector = v;
    }

    public BitVector(int sizeOfBits, boolean value) {
        Preconditions.checkArgument(sizeOfBits % 32 == 0 && sizeOfBits > 0);
        this.size = sizeOfBits;
        vector = new int[Math.floorDiv(sizeOfBits, 32)];
        if (value) {
            Arrays.fill(vector, 0xffffffff);
        }
    }

    public BitVector(int sizeOfBits) {
        this(sizeOfBits, false);
    }

    public int size() {
        return size;
    }

    public boolean testBit(int index) {// ??????
        Objects.checkIndex(index, size);
         return Bits.test(vector[Math.floorDiv(index, Integer.SIZE)],
                index % Integer.SIZE);
    }

    public BitVector not() {
        int[] v = new int[vector.length];
        for (int i = 0; i < vector.length; i++)
            v[i] = ~vector[i];

        return new BitVector(v);
    }

    public BitVector and(BitVector v2) {
        Preconditions.checkArgument(v2.vector.length == this.vector.length);
        int[] res = new int[vector.length];
        for (int i = 0; i < vector.length; i++)
            res[i] = this.vector[i] & v2.vector[i];
        return new BitVector(res);
    }

    public BitVector or(BitVector v2) {
        Preconditions.checkArgument(v2.size() == this.size());
        int[] res = new int[v2.vector.length];
        for (int i = 0; i < vector.length; i++)
            res[i] = this.vector[i] | v2.vector[i];
        return new BitVector(res);
    }

    public enum ExtractionType {
        ZeroExtended, Wrapped
    }

    public BitVector extract(int index, int sizeOfArray, ExtractionType type) {
        int div = Math.floorDiv(index, 32);
        int mod = Math.floorMod(index, 32);
        int[] extracted = new int[sizeOfArray];

        if (Math.floorMod(index, 32) == 0) {
            for (int i = 0; i < sizeOfArray; i++)
                extracted[i] = elementOfInfinite(type, div + i);
            return new BitVector(extracted);
        } else
            for (int i = 0; i < sizeOfArray; i++)
                extracted[i] = elementOfInfinite(type, (div + i)) >>> mod
                        | elementOfInfinite(type, (div + 1 + i)) << (32 - mod);

        return new BitVector(extracted);

    }

    public int elementOfInfinite(ExtractionType type, int index) {
        if (index >= 0 && index < vector.length)
            return vector[index];
        if (type == ExtractionType.ZeroExtended) {
            return 0;
        } else {
            return vector[Math.floorMod(index, vector.length)];
        }
    }

    public BitVector extractZeroExtended(int index, int sizeOfBits) {
        Preconditions.checkArgument(sizeOfBits % 32 == 0 && sizeOfBits > 0);
        return extract(index, Math.floorDiv(sizeOfBits, 32),
                ExtractionType.ZeroExtended);
    }

    public BitVector extractWrapped(int index, int sizeOfBits) {
        Preconditions.checkArgument(sizeOfBits % 32 == 0 && sizeOfBits > 0);
        return extract(index, Math.floorDiv(sizeOfBits, 32),
                ExtractionType.Wrapped);

    }

    public BitVector shift(int i) {
        return extractZeroExtended(-i, size);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        // for (int i = 0; i < vector.length; i++)
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

    public int hashCode() {
        return Arrays.hashCode(this.vector);
    }

    public static final class Builder {
        private final int sizeOfBits;
        private int[] vector;

        public Builder(int sizeOfBits) {
            Preconditions.checkArgument(
                    sizeOfBits % Integer.SIZE == 0 && sizeOfBits > 0);
            this.sizeOfBits = sizeOfBits;
            vector = new int[sizeOfBits / 32];
        }

        public Builder setByte(int index, int valueByte) {
            Objects.checkIndex(index, sizeOfBits / 8);// ?????
            Preconditions.checkBits8(valueByte);
            try {
                vector[Math.floorDiv(index,
                        Integer.SIZE / Byte.SIZE)] = (vector[Math
                                .floorDiv(index, Integer.SIZE / Byte.SIZE)]
                                & ~(0xff << (index * 8) % 32))
                                | (valueByte << (index * 8) % 32);
                return this;
            } catch (NullPointerException e) {
                throw new IllegalStateException();
            }
        }

        public BitVector build() {
            if (!Objects.nonNull(vector))
                throw new IllegalStateException();
            BitVector build = new BitVector(vector);
            this.vector = null;
            return build;
        }
    }
}
