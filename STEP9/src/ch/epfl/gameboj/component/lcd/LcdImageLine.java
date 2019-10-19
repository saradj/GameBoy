package ch.epfl.gameboj.component.lcd;

import java.util.Arrays;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.BitVector.Builder;
import ch.epfl.gameboj.bits.Bits;

public final class LcdImageLine {

    private final BitVector msb, lsb, opacity;
    private final int size;

    public LcdImageLine(BitVector msb, BitVector lsb, BitVector opacity) {
        Preconditions.checkArgument(
                msb.size() == lsb.size() && lsb.size() == opacity.size());
        this.msb = msb;
        this.lsb = lsb;
        this.opacity = opacity;
        size = msb.size();
    }

    public int size() {
        return size;
    }

    public BitVector msb() {
        return msb;
    }

    public BitVector lsb() {
        return lsb;
    }

    public BitVector opacity() {
        return opacity;
    }

    public LcdImageLine shift(int i) {
        return new LcdImageLine(msb.shift(i), lsb.shift(i), opacity.shift(i));
    }

    public LcdImageLine extractWrapped(int pixel, int size) {
        return new LcdImageLine(msb.extractWrapped(pixel, size),
                lsb.extractWrapped(pixel, size),
                opacity.extractWrapped(pixel, size));

    }

    public LcdImageLine mapColors(int palette) {
        Preconditions.checkBits8(palette);
        if (palette == 0b11100100)
            return this;
        else {
            // BitVector[] masks = new BitVector[4];
            BitVector newLsb = new BitVector(size());
            BitVector newMsb = new BitVector(size());
            BitVector mask;
            for (int i = 0; i < 4; i++) {
                mask = Bits.test(i, 0) ? lsb : lsb.not();
                mask = Bits.test(i, 1) ? mask.and(msb) : mask.and(msb.not());
                newLsb = Bits.test(palette, i * 2) ? newLsb.or(mask)
                        : newLsb;
                newMsb = Bits.test(palette, i * 2 + 1) ? newMsb.or(mask)
                        : newMsb;
            }
            return new LcdImageLine(newMsb, newLsb, opacity);
        }
    }

    public LcdImageLine below(LcdImageLine aboveLine) {// ?????
        Preconditions.checkArgument(this.size() == aboveLine.size());
        BitVector newLsb = new BitVector(size());
        BitVector newMsb = new BitVector(size());
        newLsb = aboveLine.opacity.and(aboveLine.lsb)
                .or(aboveLine.opacity.not().and(lsb));
        newMsb = aboveLine.opacity.and(aboveLine.msb)
                .or(aboveLine.opacity.not().and(msb));
        return new LcdImageLine(newMsb, newLsb, aboveLine.opacity.or(opacity));// which
        // opacity to
        // use?
    }

    public LcdImageLine below(LcdImageLine aboveLine, BitVector opacity) {// ?????
        Preconditions.checkArgument(this.size() == aboveLine.size());
        BitVector newLsb = opacity.and(aboveLine.lsb)
                .or(opacity.not().and(lsb));
        BitVector newMsb = opacity.and(aboveLine.msb)
                .or(opacity.not().and(msb));
        return new LcdImageLine(newMsb, newLsb, opacity.or(this.opacity));// if
                                                                          // opacity
                                                                          // is
                                                                          // 1
                                                                          // we
                                                                          // take
                                                                          // opacity
                                                                          // of
                                                                          // above
                                                                          // if
                                                                          // 0
                                                                          // of
                                                                          // below??
    }

    public LcdImageLine join(LcdImageLine secondLine, int n) {
        Preconditions.checkArgument(this.size() == secondLine.size());
        BitVector mask = new BitVector(size, true);
        mask = mask.shift(n).not();
        BitVector newLsb = (mask.and(lsb)).or(mask.not().and(secondLine.lsb));
        BitVector newMsb = mask.and(msb).or(mask.not().and(secondLine.msb));
        return new LcdImageLine(newMsb, newLsb, newLsb.or(newMsb));// or
                                                                                // newMsb.or(newLsb)
    }

    @Override
    public boolean equals(Object arg0) {

        return (arg0 instanceof LcdImageLine)
                && this.msb.equals(((LcdImageLine) arg0).msb())
                && this.lsb.equals(((LcdImageLine) arg0).lsb())
                && this.opacity.equals(((LcdImageLine) arg0).opacity());
    }

    @Override
    public int hashCode() {
        return Objects.hash(msb, lsb, opacity);
    }

    public static final class Builder {
        private BitVector.Builder lsb;
        private BitVector.Builder msb;

        public Builder(int size) {
            lsb = new BitVector.Builder(size);
            msb = new BitVector.Builder(size);
        }

        public Builder setByte(int index, int valueMsbByte, int valueLsbByte) {
            lsb.setByte(index, valueLsbByte);
            msb.setByte(index, valueMsbByte);
            return this;
        }

        public LcdImageLine build() {
            BitVector newMsb = msb.build();
            BitVector newLsb = lsb.build();
            return new LcdImageLine(newMsb, newLsb, newMsb.or(newLsb));

        }
    }
}
