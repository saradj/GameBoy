package ch.epfl.gameboj.bits;

import ch.epfl.test.TestRandomizer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class Step7_BitVectorTest_7303 {

    private static Random random = TestRandomizer.newRandom();

    @Nested
    class ConstructorTests {
        @Test
        void failsForNegativeSize() {
            assertThrows(IllegalArgumentException.class, () -> new BitVector(-1));
        }

        @Test
        void failsForInvalidSize() {
            assertThrows(IllegalArgumentException.class, () -> new BitVector(367));
        }

        @Test
        void passesForValidSize() {
            new BitVector(128);
        }

        @Test
        void correctlyCreatesAllZeroVector() {
            String expected = "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";

            BitVector bv = new BitVector(96);

            String actual = bv.toString();

            assertEquals(expected, actual);
        }

        @Test
        void withBoolean_correctlyCreatesAllZeroVector() {
            String expected = "000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000000";

            BitVector bv = new BitVector(96, false);

            String actual = bv.toString();

            assertEquals(expected, actual);
        }

        @Test
        void withBoolean_correctlyCreatesAllOnesVector() {
            String expected = "111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111111";

            BitVector bv = new BitVector(96, true);

            String actual = bv.toString();

            assertEquals(expected, actual);
        }
    }

    @Disabled
    @Test
    void sizeReturnsCorrectly() {
        int size;
        for (int i = 0; i < TestRandomizer.RANDOM_ITERATIONS; i++) {
            size = random.nextInt(0x10) * 32;
            BitVector b = new BitVector(size);

            assertEquals(size, b.size());
        }
    }

    @RepeatedTest(TestRandomizer.RANDOM_ITERATIONS)
    void repeatedSizeReturnsCorrectly() {
        int size = (random.nextInt(0x10) + 1) * 32;
        BitVector b = new BitVector(size);
        assertEquals(size, b.size());
    }

    @Test
    void notReturnsCorrectly_Hardcoded() {
        BitVector.Builder b = new BitVector.Builder(64);

        b.setByte(0, 0b10101010)
                .setByte(1, 0b11100111)
                .setByte(7, 0b10011101);

        BitVector bv = b.build();

        String expected = "0110001011111111111111111111111111111111111111110001100001010101";

        assertEquals(expected, bv.not().toString());
    }

    @RepeatedTest(TestRandomizer.RANDOM_ITERATIONS)
    void notReturnsCorrectly() {
        int size = (random.nextInt(3) + 1) * 32;

        BitVector.Builder bvb = new BitVector.Builder(size);
        StringBuilder expectedStringBuilder = new StringBuilder();

        for (int j = (size / 8) - 1; j >= 0; j--) {
            int value = random.nextInt(0b100000000);

            bvb.setByte(j, value);

            for (int b = 7; b >= 0; b--) {
                expectedStringBuilder.append(!Bits.test(value, b) ? '1' : '0');
            }
        }

        String expected = expectedStringBuilder.toString();
        String actual = bvb.build().not().toString();

        assertEquals(expected, actual);
    }

    @Test
    void extractZeroExtendedReturnsCorrectly_Hardcoded() {
        BitVector.Builder b = new BitVector.Builder(64);

        b.setByte(0, 0b11011011)
                .setByte(1, 0b10000111)
                .setByte(7, 0b01111101);

        BitVector bv = b.build();

        String expectedBitVector = "0111110100000000000000000000000000000000000000001000011111011011";
        assertEquals(expectedBitVector, bv.toString());

        BitVector extract1 = bv.extractZeroExtended(0, 32);
        String expectedExtract1 = "00000000000000001000011111011011";
        assertEquals(expectedExtract1, extract1.toString());

        BitVector extract2 = bv.extractZeroExtended(32, 32);
        String expectedExtract2 = "01111101000000000000000000000000";
        assertEquals(expectedExtract2, extract2.toString());

        BitVector extract3 = bv.extractZeroExtended(2, 32);
        String expectedExtract3 = "00000000000000000010000111110110";
        assertEquals(expectedExtract3, extract3.toString());

        BitVector extract4 = bv.extractZeroExtended(-32, 32);
        String expectedExtract4 = "00000000000000000000000000000000";
        assertEquals(expectedExtract4, extract4.toString());

        BitVector extract5 = bv.extractZeroExtended(-10, 32);
        String expectedExtract5 = "00000010000111110110110000000000";
        assertEquals(expectedExtract5, extract5.toString());

        BitVector extract6 = bv.extractZeroExtended(20, 64);
        String expectedExtract6 = "0000000000000000000001111101000000000000000000000000000000000000";
        assertEquals(expectedExtract6, extract6.toString());
    }

    @Test
    void extractWrappedReturnsCorrectly_Hardcoded() {
        BitVector.Builder b = new BitVector.Builder(64);

        b.setByte(0, 0b11011011)
                .setByte(1, 0b10000111)
                .setByte(7, 0b01111101);

        BitVector bv = b.build();

        String expectedBitVector = "0111110100000000000000000000000000000000000000001000011111011011";
        assertEquals(expectedBitVector, bv.toString());

        BitVector extract1 = bv.extractZeroExtended(0, 32);
        String expectedExtract1 = "00000000000000001000011111011011";
        assertEquals(expectedExtract1, extract1.toString());

        BitVector extract2 = bv.extractZeroExtended(32, 32);
        String expectedExtract2 = "01111101000000000000000000000000";
        assertEquals(expectedExtract2, extract2.toString());

        BitVector extract3 = bv.extractZeroExtended(2, 32);
        String expectedExtract3 = "00000000000000000010000111110110";
        assertEquals(expectedExtract3, extract3.toString());

        BitVector extract4 = bv.extractZeroExtended(-32, 32);
        String expectedExtract4 = "01111101000000000000000000000000";
        assertEquals(expectedExtract4, extract4.toString());

        BitVector extract5 = bv.extractZeroExtended(-10, 32);
        String expectedExtract5 = "00000010000111110110110111110100";
        assertEquals(expectedExtract5, extract5.toString());

        BitVector extract6 = bv.extractZeroExtended(20, 64);
        String expectedExtract6 = "0000100001111101101101111101000000000000000000000000000000000000";
        assertEquals(expectedExtract6, extract6.toString());
    }

    @Test
    void givenExample1_testExtractionsAndNot() {
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = v1.extractZeroExtended(-17, 32).not();
        BitVector v3 = v2.extractWrapped(11, 64);

        assertEquals("11111111111111111111111111111111", v1.toString());
        assertEquals("00000000000000011111111111111111", v2.toString());
        assertEquals("1111111111100000000000000011111111111111111000000000000000111111", v3.toString());
    }

    @Test
    void givenExample2_testsBitVectorBuilder() {
        BitVector v = new BitVector.Builder(32)
                .setByte(0, 0b1111_0000)
                .setByte(1, 0b1010_1010)
                .setByte(3, 0b1100_1100)
                .build();

        String expected = "11001100000000001010101011110000";
        String actual = v.toString();

        assertEquals(expected, actual);
    }
}
