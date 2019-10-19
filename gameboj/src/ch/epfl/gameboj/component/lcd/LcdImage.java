package ch.epfl.gameboj.component.lcd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;

/**
 * CS-108 LcdImage.java
 *  Purpose: Representing an image of {@link GameBoy}
 * 
 * @author Sara Djambazovska
 * @author Marouane Jaakik
 *
 */
public final class LcdImage {

    private final int height, width;
    private final List<LcdImageLine> lineList;

    /**
     * Constructs a {@link LcdImage} with the the given list of
     * {@link LcdImageLine}
     * 
     * @param height
     * @param width
     * @param lineList
     * @throws IllegalArgumentException
     *             if height or with are not strictly positive integers or the
     *             line list is empty or not compatible with the given
     *             parameters
     * @throws NullPointerException
     *             if the list of lines is null
     */
    public LcdImage(int height, int width, List<LcdImageLine> lineList) {
        Objects.requireNonNull(lineList);
        Preconditions
                .checkArgument(height > 0 && width > 0 && !lineList.isEmpty());
        Preconditions.checkArgument(lineList.size() == height);
        for (LcdImageLine line : lineList)
            Preconditions.checkArgument(line.size() == width);
        this.height = height;
        this.width = width;
        this.lineList = Collections.unmodifiableList(new ArrayList<>(lineList));
    }

    /**
     * Returns the width of the image in pixels
     * 
     * @return integer: width of the image in pixels
     */
    public int width() {
        return width;
    }

    /**
     * Returns the height of the image in pixels
     * 
     * @return integer: height of the image in pixels
     */
    public int height() {
        return height;
    }

    /**
     * Returns the colour of the pixel (as an integer between 1 and 3) in the
     * image specified by the coordinates passed as an argument
     * 
     * @param x
     *            integer: the x coordinate of the pixel in the image
     * @param y
     *            integer: the y coordinate of the pixel in the image
     * @return integer: the colour of the pixel as an integer between 1 and 3
     */
    public int get(int x, int y) {
        Objects.checkIndex(x, width);
        Objects.checkIndex(y, height);
        int color = lineList.get(y).lsb().testBit(x) ? 1 : 0;
        color |= lineList.get(y).msb().testBit(x) ? 1 << 1 : 0;
        return color;
    }

    @Override
    public boolean equals(Object arg0) {
        return (arg0 instanceof LcdImage)
                && (this.height == ((LcdImage) arg0).height)
                && (this.width == ((LcdImage) arg0).width)
                && this.lineList.equals(((LcdImage) arg0).lineList);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lineList);
    }

    /**
     * Represents a Builder of the {@link LcdImage} allowing the construction of
     * the {@link LcdImage} line by line
     */
    public static final class Builder {
        private final int height, width;
        private final List<LcdImageLine> lineList;

        /**Constructs the {@link LcdImage} Builder
         * @param height
         * @param width
         */
        public Builder(int height, int width) {
            lineList = new ArrayList<>(height);
            this.height = height;
            this.width = width;
            lineList.addAll(Collections.nCopies(height,
                    new LcdImageLine(new BitVector(width), new BitVector(width),
                            new BitVector(width))));

        }

        /**
         * Sets the line at the given index to the line passed as an argument
         * and returns the {@link Builder} itself
         * 
         * @param line
         *            {@link LcdImageLine}: the line to be assigned
         * @param index
         *            integer: the index of the line to be set
         * @throws NullPointerException
         *             line is null.
         * 
         * @throws IndexOutOfBoundsException
         *             if index is not between 0 (included) and height
         *             (excluded).
         * 
         * @throws IllegalArgumentException
         *             if the {@link LcdImageLine}'s size is not equal to the
         *             width of the image being built.
         * 
         * @return {@link Builder}
         */
        public Builder setLine(LcdImageLine line, int index) {
            Objects.requireNonNull(line);
            Objects.checkIndex(index, height);
            Preconditions.checkArgument(line.size() == width);
            lineList.set(index, line);
            return this;
        }

        /**
         * Builds the corresponding {@link LcdImage}
         * 
         * @return {@link LcdImage}: the build image
         */
        public LcdImage build() {
            return new LcdImage(height, width, lineList);
        }
    }
}
