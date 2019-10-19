package ch.epfl.gameboj.component.lcd;

import java.util.Objects;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;
import ch.epfl.gameboj.bits.Bits;

/**
 * CS-108 LcdImageLine.java
 *  Purpose: Representing an image line of the
 * {@link LcdImage} of {@link GameBoy}
 * 
 * @author Sara Djambazovska
 * @author Marouane Jaakik
 *
 */
public final class LcdImageLine {

    private static final int SAME_PALETTE = 0b11100100;
    private static final int NUMBER_OF_COLORS = 4;
    private final BitVector msb, lsb, opacity;
    private final int size;

    /**
     * Constructs a {@link LcdImageLine} with the given parameters
     * 
     * @param msb
     * @param lsb
     * @param opacity
     */
    public LcdImageLine(BitVector msb, BitVector lsb, BitVector opacity) {
        Preconditions.checkArgument(
                msb.size() == lsb.size() && lsb.size() == opacity.size());
        this.msb = msb;
        this.lsb = lsb;
        this.opacity = opacity;
        size = msb.size();
    }

    /**
     * Returns the size of the line in pixels
     * 
     * @return integer: number of pixels in the line
     */
    public int size() {
        return size;
    }

    /**
     * Returns the most significant bits of the line
     * 
     * @return {@link BitVector}: containing the most significant bits of the
     *         line
     */
    public BitVector msb() {
        return msb;
    }

    /**
     * Returns the least significant bits of the line
     * 
     * @return {@link BitVector}: containing the least significant bits of the
     *         line
     */
    public BitVector lsb() {
        return lsb;
    }

    /**
     * Returns the opacity bits of the line
     * 
     * @return {@link BitVector}: containing the opacity bits of the line
     */
    public BitVector opacity() {
        return opacity;
    }

    /**
     * Shifts the line for number of pixels passed as an argument, shifts to the
     * left if the argument is positive, otherwise to the right
     * 
     * @param i
     *            integer: amount of pixels to be shifted
     * @return {@link LcdImageLine}: the shifted line
     */
    public LcdImageLine shift(int i) {
        return new LcdImageLine(msb.shift(i), lsb.shift(i), opacity.shift(i));
    }

    /**
     * Performs wrapped extraction on the line starting at the pixel indexed by
     * the first argument and for a number of pixels indicated by the second
     * argument
     * 
     * @param pixel
     *            integer: starting pixel for the extraction
     * @param size
     *            integer: number of pixels to be extracted
     * @return {@link LcdImageLine}: the wrapped extraction of the line
     */
    public LcdImageLine extractWrapped(int pixel, int size) {
        return new LcdImageLine(msb.extractWrapped(pixel, size),
                lsb.extractWrapped(pixel, size),
                opacity.extractWrapped(pixel, size));

    }

    /**
     * Performs zero extended extraction on the line starting at the pixel
     * indexed by the first argument and for a number of pixels indicated by the
     * second argument
     * 
     * @param pixel
     *            integer: starting pixel for the extraction
     * @param size
     *            integer: number of pixels to be extracted
     * @return {@link LcdImageLine}: the zero extended extraction of the line
     */
    public LcdImageLine extractZeroExtended(int pixel, int size) {
        return new LcdImageLine(msb.extractZeroExtended(pixel, size),
                lsb.extractZeroExtended(pixel, size),
                opacity.extractZeroExtended(pixel, size));
    }

    /**
     * Transforms the colours of the line according to the palette passed as an
     * argument
     * 
     * @param palette
     *            integer: 8 bits the transformation palette
     * @throws IllegalArgumentException
     *             if the palette passed as an argument is not of 8 bits
     * @return {@link LcdImageLine}: the line with the new colours mapped
     *         according to the palette
     */
    public LcdImageLine mapColors(int palette) {
        Preconditions.checkBits8(palette);
        if (palette == SAME_PALETTE)
            return this;
        else {
            BitVector newLsb = new BitVector(size());
            BitVector newMsb = new BitVector(size());
            BitVector mask;
            for (int i = 0; i < NUMBER_OF_COLORS; i++) {
                mask = Bits.test(i, 0) ? lsb : lsb.not();
                mask = Bits.test(i, 1) ? mask.and(msb) : mask.and(msb.not());
                newLsb = Bits.test(palette, i * 2) ? newLsb.or(mask) : newLsb;
                newMsb = Bits.test(palette, i * 2 + 1) ? newMsb.or(mask)
                        : newMsb;
            }
            return new LcdImageLine(newMsb, newLsb, opacity);
        }
    }

    /**
     * Composes the line below with the above line passed as an argument, using
     * the above line's opacity to perform the composition
     * 
     * @param aboveLine
     *            {@link LcdImageLine}: above line to be composed
     * @throws IllegalArgumentException
     *             if the sizes of the two lines are not the same
     * @return {@link LcdImageLine}: the composition of the line with the line
     *         above it
     */
    public LcdImageLine below(LcdImageLine aboveLine) {// ?????
        return below(aboveLine, aboveLine.opacity);
    }

    /**
     * Composes the line below with the above line passed as an argument,
     * according to the opacity passed as a second argument
     * 
     * @param aboveLine
     *            {@link LcdImageLine}: above line to be composed
     * @param opacity
     *            {@link BitVector}: the opacity to be used while performing the
     *            composition
     * @throws IllegalArgumentException
     *             if the sizes of the two lines are not the same
     * @return {@link LcdImageLine}: the composition of the line with the line
     *         above it
     */
    public LcdImageLine below(LcdImageLine aboveLine, BitVector opacity) {
        Preconditions.checkArgument(this.size() == aboveLine.size());
        BitVector newLsb = opacity.and(aboveLine.lsb)
                .or(opacity.not().and(lsb));
        BitVector newMsb = opacity.and(aboveLine.msb)
                .or(opacity.not().and(msb));
        return new LcdImageLine(newMsb, newLsb, opacity.or(this.opacity));
    }

    /**
     * Joins the line with the line passed as an argument from a pixel index
     * passed as an argument
     * 
     * @param secondLine
     *            {@link LcdImageLine}: the second line to be joined
     * @param n
     *            integer: giving the number of pixels to be taken from the
     *            first line
     * @throws IllegalArgumentException
     *             if the number of pixels in the two lines is different
     * @return {@link LcdImageLine}: the joined line containing the first n
     *         pixels from the first line and the rest from the secon one
     */
    public LcdImageLine join(LcdImageLine secondLine, int n) {
        Objects.requireNonNull(secondLine);
        Preconditions.checkArgument(this.size() == secondLine.size());
        BitVector mask = new BitVector(size, true);
        mask = mask.shift(n).not();
        BitVector newLsb = (mask.and(lsb)).or(mask.not().and(secondLine.lsb));
        BitVector newMsb = mask.and(msb).or(mask.not().and(secondLine.msb));
        BitVector newOpacity = mask.and(this.opacity)
                .or(mask.not().and(secondLine.opacity));
        return new LcdImageLine(newMsb, newLsb, newOpacity);
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

    /**
     * Represents a Builder of the {@link LcdImageLine} allowing the
     * construction of the {@link LcdImageLine} byte by byte of pixels
     */
    public static final class Builder {
        private final BitVector.Builder lsb;
        private final BitVector.Builder msb;

        public Builder(int size) {
            lsb = new BitVector.Builder(size);
            msb = new BitVector.Builder(size);
        }

        /**
         * Sets the byte of pixels, in the {@link LcdImageLine} using the
         * corresponding most significant and least significant bits passed as
         * an argument and returns the builder itself
         * 
         * @param index
         *            integer: the position of the byte to be set
         * @param valueMsbByte
         *            integer: 8 bits, the value of most significant bits for
         *            that byte of pixels
         * @param valueLsbByte
         *            integer: 8 bits, the value of least significant bits for
         *            that byte of pixels
         * @return {@link Builder}
         */
        public Builder setByte(int index, int valueMsbByte, int valueLsbByte) {
            lsb.setByte(index, valueLsbByte);
            msb.setByte(index, valueMsbByte);
            return this;
        }

        /**
         * Builds the {@link LcdImageLine}
         * 
         * @return {@link LcdImageLine}: the builded LCD Image Line
         */
        public LcdImageLine build() {
            BitVector newMsb = msb.build();
            BitVector newLsb = lsb.build();
            return new LcdImageLine(newMsb, newLsb, newMsb.or(newLsb));

        }
    }
}
