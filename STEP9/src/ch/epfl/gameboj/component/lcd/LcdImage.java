package ch.epfl.gameboj.component.lcd;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.BitVector;

public final class LcdImage {

    private final int height, width;
    private final List<LcdImageLine> lineList;

    public LcdImage(int height, int width, List<LcdImageLine> lineList) {
    	    Objects.requireNonNull(lineList);
        Preconditions .checkArgument(height > 0 && width > 0);
    	    Preconditions.checkArgument(lineList.size()==height);;
        this.height = height;
        this.width = width;
        this.lineList = lineList;
    }

    public int get(int x, int y) {

        int color = lineList.get(y).lsb().testBit(x) ? 1 : 0;
        color |= lineList.get(y).msb().testBit(x) ? 0b10 : 0;
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

    public static final class Builder {
        private final int height, width;
        private final List<LcdImageLine> lineList;

        public Builder(int height, int width) {
            lineList = new ArrayList<>(height);
            this.height = height;
            this.width = width;
            for (int i = 0; i < height; i++) {
                lineList.add(new LcdImageLine(new BitVector(width),
                        new BitVector(width), new BitVector(width)));
            }
        }

        public Builder setLine(LcdImageLine line, int index) {

            lineList.set(index, line);

            return this;
        }

        public LcdImage build() {
            return new LcdImage(height, width, lineList);
        }
    }

	public int width() {
		// TODO Auto-generated method stub
		return width;
	}
	public int height() {
		return height;
		
	}
}
