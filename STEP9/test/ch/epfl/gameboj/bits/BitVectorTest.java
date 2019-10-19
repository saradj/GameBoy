package ch.epfl.gameboj.bits;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.List;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.RandomGenerator;

public class BitVectorTest {

  
	@Test
	public void operationsWorkOn32BitVector() {
		for(int i = 0; i < RandomGenerator.RANDOM_ITERATIONS; i++) {
			int value = RandomGenerator.randomBit(32);
			int distance = RandomGenerator.randomBit(5);
			int or = RandomGenerator.randomBit(32);
			int and = RandomGenerator.randomBit(32);
			int extract = RandomGenerator.randomBit(5);
			BitVector.Builder builderValue = new BitVector.Builder(32);
			BitVector.Builder builderOr = new BitVector.Builder(32);
			BitVector.Builder builderAnd = new BitVector.Builder(32);
			builderValue.setByte(0, Bits.clip(8, value)).setByte(1, Bits.extract(value, 8, 8)).setByte(2, Bits.extract(value, 16, 8)).setByte(3, Bits.extract(value, 24, 8));
			builderOr.setByte(0, Bits.clip(8, or)).setByte(1, Bits.extract(or, 8, 8)).setByte(2, Bits.extract(or, 16, 8)).setByte(3, Bits.extract(or, 24, 8));
			builderAnd.setByte(0, Bits.clip(8, and)).setByte(1, Bits.extract(and, 8, 8)).setByte(2, Bits.extract(and, 16, 8)).setByte(3, Bits.extract(and, 24, 8));
			BitVector bitVectorValue = builderValue.build();
			BitVector bitVectorOr = builderOr.build();
			BitVector bitVectorAnd = builderAnd.build();
			assertEquals(value << distance, bitVectorValue.shift(distance).get(0));
			assertEquals(value >>> distance, bitVectorValue.shift(-distance).get(0));
			assertEquals(value | or, bitVectorValue.or(bitVectorOr).get(0));
			assertEquals(value & and, bitVectorValue.and(bitVectorAnd).get(0));
			assertEquals(Bits.extract(value, extract, 32 - extract), bitVectorValue.extractZeroExtended(extract, Integer.SIZE).get(0));
			assertEquals(Bits.extract(value, extract, 32 - extract) | Bits.clip(extract, value) << 32 - extract, bitVectorValue.extractWrapped(extract, Integer.SIZE).get(0));
		}
	}

	@Test
	public void builderWithoutOverrideWorks() {
		for(int i = 0; i < RandomGenerator.RANDOM_ITERATIONS; i++) {
			BitVector.Builder builder = new BitVector.Builder(64);
			int[] bytes = new int[8];
			for(int j = 0; j < 8; j++) {
				bytes[j] = RandomGenerator.randomBit(8);
				builder.setByte(j, bytes[j]);
			}
			BitVector bitVector = builder.build();
			assertThrows(IllegalStateException.class, () -> builder.setByte(RandomGenerator.randomBit(3),RandomGenerator.randomBit(8)));
			assertThrows(IllegalStateException.class, () -> builder.build());
			for(int j = 0; j < 4; j++) {
				assertEquals(Bits.make16(bytes[j*2 + 1], bytes[j*2]), Bits.extract(bitVector.get(j/2), (j % 2) * 16, 16));
			}

		}

	}
	

	@Test
	public void builderWithOverrideWorks() {
		for(int i = 0; i < RandomGenerator.RANDOM_ITERATIONS; i++) {
			BitVector.Builder builder = new BitVector.Builder(64);
			int[] bytes = new int[8];
			for(int j = 0; j < 8; j++) {
				bytes[j] = RandomGenerator.randomBit(8);
				builder.setByte(j, bytes[j]);
			}
			int[] override = new int[8];
			for(int j = 0; j < 8; j++) {
				override[j] = RandomGenerator.randomBit(8);
				builder.setByte(j, override[j]);
			}
			BitVector bitVector = builder.build();
			assertThrows(IllegalStateException.class, () -> builder.setByte(RandomGenerator.randomBit(3),RandomGenerator.randomBit(8)));
			assertThrows(IllegalStateException.class, () -> builder.build());
			for(int j = 0; j < 4; j++) {
				assertEquals(Bits.make16(override[j*2 + 1], override[j*2]), Bits.extract(bitVector.get(j/2), (j % 2) * 16, 16));
			}

		}

	}
	
	@Test
	public void operationsWorkOn64BitVector() {
		for(int i = 0; i < RandomGenerator.RANDOM_ITERATIONS; i++) {
		BitVector.Builder builderA = new BitVector.Builder(64);
		BitVector.Builder builderB = new BitVector.Builder(64);
		int[] contentA = new int[8];
		int[] contentB = new int[8];
		for(int j = 0; j < 8; j++) {
			contentA[j] = RandomGenerator.randomBit(8);
			contentB[j] = RandomGenerator.randomBit(8);
			builderA.setByte(j, contentA[j]);
			builderB.setByte(j, contentB[j]);
		}
		BitVector bitVectorA = builderA.build();
		BitVector bitVectorB = builderB.build();
		BitVector and = bitVectorA.and(bitVectorB);
		BitVector or = bitVectorA.or(bitVectorB);
		
		//and, or test
		for(int j = 0; j < 2; j++) {
			System.out.println(j);
			assertEquals((Bits.make16(contentA[4*j + 3], contentA[4*j + 2]) << 16 | Bits.make16(contentA[4*j + 1], contentA[4*j])) 
					| (Bits.make16(contentB[4*j + 3], contentB[4*j + 2]) << 16 | Bits.make16(contentB[4*j + 1], contentB[4*j])), or.get(j));
			assertEquals((Bits.make16(contentA[4*j + 3], contentA[4*j + 2]) << 16 | Bits.make16(contentA[4*j + 1], contentA[4*j]) )
					& (Bits.make16(contentB[4*j + 3], contentB[4*j + 2]) << 16 | Bits.make16(contentB[4*j + 1], contentB[4*j])), and.get(j));
		}
		}
	}
    @Test
    public void toStringAndBuilderWork() {
        BitVector.Builder builder = new BitVector.Builder(32)
                .setByte(0, 0b1100_1100)
                .setByte(1, 0b1010_1010)
                .setByte(2, 0b0101_0101)
                .setByte(3, 0b1001_0110);
        BitVector a = builder.build();
        assertTrue("10010110010101011010101011001100".equals(a.toString()));
        assertThrows(IllegalStateException.class, () -> {
            builder.setByte(0, 0);
        });
    }
    
    @Test
    public void constructorsWork() {
        BitVector a = new BitVector(32);
        BitVector b = new BitVector(64, true);
        BitVector c = new BitVector(128, false);
        assertTrue("00000000000000000000000000000000".equals(a.toString()));
        assertTrue("1111111111111111111111111111111111111111111111111111111111111111".equals(b.toString()));
        assertTrue(("0000000000000000000000000000000000000000000000000000000000000000"
                + "0000000000000000000000000000000000000000000000000000000000000000").equals(c.toString()));
    }
    
    @Test
    public void testBitWorks() {
        BitVector.Builder builder = new BitVector.Builder(32)
                .setByte(0, 0b1100_1100)
                .setByte(1, 0b1010_1010)
                .setByte(2, 0b0101_0101)
                .setByte(3, 0b1001_0110);
        BitVector a = builder.build();
        BitVector b = new BitVector(64, true);
        assertTrue(a.testBit(31));
        assertTrue(!a.testBit(0));
        assertTrue(b.testBit(52));

    }
    
    @Test
    public void complementWorks() {
        BitVector.Builder builder = new BitVector.Builder(32)
                .setByte(0, 0b1100_1100)
                .setByte(1, 0b1010_1010)
                .setByte(2, 0b0101_0101)
                .setByte(3, 0b1001_0110);
        BitVector a = builder.build();
        BitVector b = new BitVector(64, true);
        BitVector c = a.not();
        BitVector d = b.not();
        assertTrue("01101001101010100101010100110011".equals(c.toString()));
        assertTrue("0000000000000000000000000000000000000000000000000000000000000000".equals(d.toString()));
    }
    
    @Test
    public void andOrWork() {
        BitVector.Builder builder = new BitVector.Builder(32)
                .setByte(0, 0b1100_1100)
                .setByte(1, 0b1010_1010)
                .setByte(2, 0b0101_0101)
                .setByte(3, 0b1001_0110);
        BitVector a = builder.build();
        builder = new BitVector.Builder(32)
                .setByte(0, 0b1110_1110)
                .setByte(1, 0b0001_0001)
                .setByte(2, 0b1101_1101)
                .setByte(3, 0b0101_0101);
        BitVector b = builder.build();
        BitVector c = a.or(b);
        BitVector d = b.and(a);
        assertTrue("11010111110111011011101111101110".equals(c.toString()));
        assertTrue("00010100010101010000000011001100".equals(d.toString()));
    }
    
    @Test
    public void tempTest() {
    	 BitVector.Builder builder = new BitVector.Builder(64)
                 .setByte(0, 0b11001100)
                 .setByte(1, 0b10101010)
                 .setByte(2, 0b01010101)
                 .setByte(3, 0b10010110)
                 .setByte(4, 0b11001100)
                 .setByte(5, 0b10101010)
                 .setByte(6, 0b01010101)
                 .setByte(7, 0b10010110);
        
         BitVector a = builder.build();
         BitVector c = new BitVector(64, true);
         BitVector c2 = c.extractZeroExtended(1, 64);
         System.out.println(a);
         System.out.println(c2);
         assertEquals("0000000000000001001011001010101101010101100110010010110010101011", c2.toString());
    }
    @Test
    public void extractsWork() {
        BitVector.Builder builder = new BitVector.Builder(64)
                .setByte(0, 0b11001100)
                .setByte(1, 0b10101010)
                .setByte(2, 0b01010101)
                .setByte(3, 0b10010110)
                .setByte(4, 0b11001100)
                .setByte(5, 0b10101010)
                .setByte(6, 0b01010101)
                .setByte(7, 0b10010110);
        BitVector a = builder.build();
        builder = new BitVector.Builder(64)
                .setByte(0, 0b11101110)
                .setByte(1, 0b00010001)
                .setByte(2, 0b11011101)
                .setByte(3, 0b01010101)
                .setByte(4, 0b11101110)
                .setByte(5, 0b00010001)
                .setByte(6, 0b11011101)
                .setByte(7, 0b01010101);
        // a : 1001011001010101101010101100110010010110010101011010101011001100
        // b : 0101010111011101000100011110111001010101110111010001000111101110
        BitVector b = builder.build();
        BitVector c1 = a.extractZeroExtended(-7, 32);
        BitVector c2 = a.extractZeroExtended(15, 64);
        BitVector c3 = a.extractZeroExtended(15, 32);
        BitVector c4 = a.extractZeroExtended(-32, 32);
        System.out.println("0000000000000001001011001010101101010101100110010010110010101011");
        System.out.println(c2.toString());
        assertEquals("00101010110101010110011000000000", c1.toString());
        assertEquals("0000000000000001001011001010101101010101100110010010110010101011", c2.toString());
        assertEquals("01010101100110010010110010101011", c3.toString());
        assertEquals("00000000000000000000000000000000", c4.toString());
        BitVector d1 = b.extractWrapped(0, 32);
        BitVector d2 = b.extractWrapped(0, 64);
        BitVector d3 = b.extractWrapped(1, 64);
        BitVector d4 = b.extractWrapped(130, 64);
        assertTrue("01010101110111010001000111101110".equals(d1.toString()));
        assertTrue("0101010111011101000100011110111001010101110111010001000111101110".equals(d2.toString()));
        assertTrue("0010101011101110100010001111011100101010111011101000100011110111".equals(d3.toString()));
        assertTrue("1001010101110111010001000111101110010101011101110100010001111011".equals(d4.toString()));
    }
    
    @Test
    public void builderWorksOnRewrite() {
        BitVector.Builder builder = new BitVector.Builder(64)
                .setByte(0, 0b11001100)
                .setByte(1, 0b10101010)
                .setByte(2, 0b01010101)
                .setByte(3, 0b10010110)
                .setByte(4, 0b11001100)
                .setByte(5, 0b10101010)
                .setByte(6, 0b01010101)
                .setByte(7, 0b10010110)
                .setByte(0, 0b11101110)
                .setByte(1, 0b00010001)
                .setByte(2, 0b11011101)
                .setByte(3, 0b01010101)
                .setByte(4, 0b11101110)
                .setByte(5, 0b00010001)
                .setByte(6, 0b11011101)
                .setByte(7, 0b01010101);
        BitVector a = builder.build();
        assertEquals("0101010111011101000100011110111001010101110111010001000111101110", a.toString());
    }
    
    @Test
    public void printExample() {
//        Supposed to write : 
//        11111111111111111111111111111111
//        00000000000000011111111111111111
//        1111111111100000000000000011111111111111111000000000000000111111
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = v1.extractZeroExtended(-17, 32).not();
        BitVector v3 = v2.extractWrapped(11, 64);
        for (BitVector v: List.of(v1, v2, v3))
          System.out.println(v);
    }


// TESTS CONSTRUCTEUR 1
    // Cas normal
    @Test
    public void contstructeur1TestNormal() {
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = new BitVector(32, false);
        
        assertEquals("11111111111111111111111111111111", v1.toString());
        assertEquals("00000000000000000000000000000000", v2.toString());
    }
    
    // Cas d'erreur
    @Test
    public void constructeur1TestError() {
        assertThrows(IllegalArgumentException.class,() -> {BitVector v1 = new BitVector(-1, true);});
        assertThrows(IllegalArgumentException.class,() -> {BitVector v2 = new BitVector(-546, false);});
        assertThrows(IllegalArgumentException.class,() -> {BitVector v3 = new BitVector(4, true);});
        assertThrows(IllegalArgumentException.class,() -> {BitVector v4 = new BitVector(31, false);});
    }
    
    
// TESTS CONSTRUCTEUR2
    // Cas normal
    @Test
    public void constructeur2TestNormal() {
        BitVector v2 = new BitVector(32);
        assertEquals("00000000000000000000000000000000", v2.toString());
    }
    
    // Cas d'erreur
    @Test
    public void constructeur2TestError() {
        assertThrows(IllegalArgumentException.class,() -> {BitVector v1 = new BitVector(-1);});
        assertThrows(IllegalArgumentException.class,() -> {BitVector v2 = new BitVector(-546);});
        assertThrows(IllegalArgumentException.class,() -> {BitVector v3 = new BitVector(4);});
        assertThrows(IllegalArgumentException.class,() -> {BitVector v4 = new BitVector(31);});
    }
    
    
// TESTS TESTBIT()
    // Cas Normaux
    @Test
    public void testBitNormal() {
        // 11001100000000001010101011110000
        BitVector v1 = new BitVector.Builder(32).setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
        for(int i = 0 ; i < 4 ; ++i) {
            assertEquals(false, v1.testBit(i));
        }
        for(int i = 4 ; i < 8 ; ++i) {
            assertEquals(true, v1.testBit(i));
        }
        for(int i = 16 ; i < 26 ; ++i) {
            assertEquals(false, v1.testBit(i));
        }
        for(int i = 28 ; i < 30 ; ++i) {
            assertEquals(false, v1.testBit(i));
        }
        for(int i = 30 ; i < 32 ; ++i) {
            assertEquals(true, v1.testBit(i));
        }
    }
    
    // Cas limites
    @Test
    public void testBitLimit() {
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = new BitVector(32);
        BitVector v3 = new BitVector(64, true);
        BitVector v4 = new BitVector(64, false);
        BitVector v5 = new BitVector(96, true);
        BitVector v6 = new BitVector(96);
        
        assertEquals(true, v1.testBit(0));
        assertEquals(true, v1.testBit(31));
        assertEquals(true, v1.testBit(24));
        
        assertEquals(false, v2.testBit(0));
        assertEquals(false, v2.testBit(31));
        assertEquals(false, v2.testBit(5));
        
        assertEquals(true, v3.testBit(0));
        assertEquals(true, v3.testBit(63));
        assertEquals(true, v3.testBit(54));
        
        assertEquals(false, v4.testBit(0));
        assertEquals(false, v4.testBit(63));
        assertEquals(false, v4.testBit(17));
        
        assertEquals(true, v5.testBit(0));
        assertEquals(true, v5.testBit(95));
        assertEquals(true, v5.testBit(84));
        
        assertEquals(false, v6.testBit(0));
        assertEquals(false, v6.testBit(95));
        assertEquals(false, v6.testBit(2));  
    }
    
    // Cas d'erreur
    @Test
    public void testBitError() {
        BitVector v1 = new BitVector(32, true);
        
        assertThrows(IndexOutOfBoundsException.class,() -> v1.testBit(-1));
        assertThrows(IndexOutOfBoundsException.class,() -> v1.testBit(32));
        assertThrows(IndexOutOfBoundsException.class,() -> v1.testBit(45));
        assertThrows(IndexOutOfBoundsException.class,() -> v1.testBit(-3));
    }
    

// TESTS NOT`
    
    @Test
    public void testNotNormal() {
     // 11001100000000001010101011110000
        BitVector v1 = new BitVector.Builder(32).setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
        BitVector v2 = v1.not();
        assertEquals("00110011111111110101010100001111", v2.toString());
    }
    
    // Cas Limites
    @Test
    public void testNotLimit() {
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = new BitVector(32);
        BitVector v3 = v1.not();
        BitVector v4 = v2.not();
        System.out.println(v3.toString());
        System.out.println("00000000000000000000000000000000");
        assertEquals("00000000000000000000000000000000", v3.toString());
        assertEquals("11111111111111111111111111111111", v4.toString());
    }
    
    
// TESTS OR
    // Cas Normaux
    @Test
    public void testOrNormal() {
     // 11001100000000001010101011110000
        BitVector v1 = new BitVector.Builder(32).setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
     // 00000000110101011111111000000000
        BitVector v2 = new BitVector.Builder(32).setByte(0, 0b0000_0000).setByte(1, 0b1111_1110).setByte(2, 0b1101_0101).build();
        
        assertEquals("11001100110101011111111011110000", v1.or(v2).toString());
    }
    
    // Cas Limites
    @Test
    public void testOrLimit() {
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = new BitVector(32);
        
        assertEquals("11111111111111111111111111111111", v1.or(v2).toString());
        assertEquals("11111111111111111111111111111111", v1.or(v1).toString());
        assertEquals("00000000000000000000000000000000", v2.or(v2).toString());
    }
    
    // Cas d'erreurs
    @Test
    public void testOrError() {
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = new BitVector(64);
        
        assertThrows(IllegalArgumentException.class,() -> v1.or(v2));
    }
    
    
// TESTS AND
 // Cas Normaux
    @Test
    public void testAndNormal() {
     // 11001100000000001010101011110000
        BitVector v1 = new BitVector.Builder(32).setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
     // 00000000110101011111111000000000
        BitVector v2 = new BitVector.Builder(32).setByte(0, 0b0000_0000).setByte(1, 0b1111_1110).setByte(2, 0b1101_0101).build();
        
        assertEquals("00000000000000001010101000000000", v1.and(v2).toString());
    }
    
    // Cas Limites
    @Test
    public void testAndLimit() {
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = new BitVector(32);
        
        assertEquals("00000000000000000000000000000000", v1.and(v2).toString());
        assertEquals("11111111111111111111111111111111", v1.and(v1).toString());
        assertEquals("00000000000000000000000000000000", v2.and(v2).toString());
    }
    
    // Cas d'erreurs
    @Test
    public void testAndError() {
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = new BitVector(64);
        
        assertThrows(IllegalArgumentException.class,() -> v1.and(v2));
    }
    
    
// TESTS EXTRACTZEROEXTENDED
    @Test
    public void extractZeroExtendedTest() {
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = new BitVector(32);
        // 11001100000000001010101011110000
        BitVector v3 = new BitVector.Builder(32).setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
        // 00000000110101011111111000000000
        BitVector v4 = new BitVector.Builder(32).setByte(0, 0b0000_0000).setByte(1, 0b1111_1110).setByte(2, 0b1101_0101).build();
        
        BitVector v5 = v1.extractZeroExtended(-4, 64);
        assertEquals("0000000000000000000000000000111111111111111111111111111111110000", v5.toString());
        BitVector v6 = v2.extractZeroExtended(-4, 64);
        assertEquals("0000000000000000000000000000000000000000000000000000000000000000", v6.toString());
        BitVector v7 = v3.extractZeroExtended(-1, 32);
        assertEquals("10011000000000010101010111100000", v7.toString());
        BitVector v8 = v4.extractZeroExtended(-14, 64);
        assertEquals("0000000000000000000000000011010101111111100000000000000000000000", v8.toString());
        BitVector v9 = v3.extractZeroExtended(-32, 32);
        assertEquals("00000000000000000000000000000000", v9.toString());
    }
    
    
// TESTS EXTRACTWRAPPED
    @Test
    public void extractWrappedTest() {
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = new BitVector(32);
        // 11001100000000001010101011110000
        BitVector v3 = new BitVector.Builder(32).setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
        // 00000000110101011111111000000000
        BitVector v4 = new BitVector.Builder(32).setByte(0, 0b0000_0000).setByte(1, 0b1111_1110).setByte(2, 0b1101_0101).build();
        
        BitVector v5 = v1.extractWrapped(-4, 32);
        assertEquals("11111111111111111111111111111111", v5.toString());
        BitVector v6 = v2.extractWrapped(-4, 64);
        assertEquals("0000000000000000000000000000000000000000000000000000000000000000", v6.toString());
        BitVector v7 = v3.extractWrapped(-1, 32);
        assertEquals("10011000000000010101010111100001", v7.toString());
        BitVector v8 = v4.extractWrapped(-14, 64);
        assertEquals("0111111110000000000000000011010101111111100000000000000000110101", v8.toString());
        BitVector v9 = v3.extractWrapped(-32, 32);
        assertEquals("11001100000000001010101011110000", v9.toString());
    }
    
    
// TESTS SHIFT
    @Test
    public void shiftTest() {
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = new BitVector(32);
        // 11001100000000001010101011110000
        BitVector v3 = new BitVector.Builder(32).setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
        // 00000000110101011111111000000000
        BitVector v4 = new BitVector.Builder(32).setByte(0, 0b0000_0000).setByte(1, 0b1111_1110).setByte(2, 0b1101_0101).build();
        
        BitVector v5 = v1.shift(4);
        assertEquals("11111111111111111111111111110000", v5.toString());
        BitVector v6 = v1.shift(-6);
        assertEquals("00000011111111111111111111111111", v6.toString());
        
        BitVector v7 = v2.shift(12);
        assertEquals("00000000000000000000000000000000", v7.toString());
        BitVector v8 = v2.shift(-12);
        assertEquals("00000000000000000000000000000000", v8.toString());
        
        BitVector v9 = v3.shift(11);
        assertEquals("00000101010101111000000000000000", v9.toString());
        BitVector v10 = v3.shift(-5);
        assertEquals("00000110011000000000010101010111", v10.toString());
        
        BitVector v11 = v4.shift(2);
        assertEquals("00000011010101111111100000000000", v11.toString());
        BitVector v12 = v4.shift(-3);
        assertEquals("00000000000110101011111111000000", v12.toString());
    }
    
    
// TESTS EQUALS
    @Test
    public void equalsTest() {
     // 11001100000000001010101011110000
        BitVector v1 = new BitVector.Builder(32).setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
     // 00000000110101011111111000000000
        BitVector v2 = new BitVector.Builder(32).setByte(0, 0b0000_0000).setByte(1, 0b1111_1110).setByte(2, 0b1101_0101).build();
     
     // 11001100000000001010101011110000
        BitVector v3 = new BitVector.Builder(32).setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
     // 00000000110101011111111000000000
        BitVector v4 = new BitVector.Builder(32).setByte(0, 0b0000_0000).setByte(1, 0b1111_1110).setByte(2, 0b1101_0101).build();
        
        BitVector v5 = new BitVector(64, true);
        BitVector v6 = new BitVector(64, true);
        
        BitVector v7 = new BitVector(5*32);
        BitVector v8 = new BitVector(5*32);
        
        assertEquals(true, v1.equals(v3));
        assertEquals(true, v2.equals(v4));
        assertEquals(true, v5.equals(v6));
        assertEquals(true, v7.equals(v8));
        
        assertEquals(false, v1.equals(v8));
        assertEquals(false, v2.equals(v1));
        assertEquals(false, v5.equals(v7));
        assertEquals(false, v3.equals(v8));
    }
    
 
// TESTS HASHCODE
    @Test
    public void hashCodeTest() {
        // 11001100000000001010101011110000
        BitVector v1 = new BitVector.Builder(32).setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
     // 00000000110101011111111000000000
        BitVector v2 = new BitVector.Builder(32).setByte(0, 0b0000_0000).setByte(1, 0b1111_1110).setByte(2, 0b1101_0101).build();
     
     // 11001100000000001010101011110000
        BitVector v3 = new BitVector.Builder(32).setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
     // 00000000110101011111111000000000
        BitVector v4 = new BitVector.Builder(32).setByte(0, 0b0000_0000).setByte(1, 0b1111_1110).setByte(2, 0b1101_0101).build();
        
        BitVector v5 = new BitVector(64, true);
        BitVector v6 = new BitVector(64, true);
        
        BitVector v7 = new BitVector(5*32);
        BitVector v8 = new BitVector(5*32);
        
        assertEquals(v1.hashCode(), v3.hashCode());
        assertEquals(v2.hashCode(), v4.hashCode());
        assertEquals(v5.hashCode(), v6.hashCode());
        assertEquals(v7.hashCode(), v8.hashCode());
        
        assertNotEquals(v1.hashCode(), v8.hashCode());
        assertNotEquals(v2.hashCode(), v1.hashCode());
        assertNotEquals(v7.hashCode(), v5.hashCode());
        assertNotEquals(v3.hashCode(), v8.hashCode());
    }
    
// TESTS TOSTRING
    // Cas normaux
    @Test
    public void toStringNormal() {
     // 11001100000000001010101011110000
        BitVector v1 = new BitVector.Builder(32).setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).build();
     // 00000000110101011111111000000000
        BitVector v2 = new BitVector.Builder(32).setByte(0, 0b0000_0000).setByte(1, 0b1111_1110).setByte(2, 0b1101_0101).build();
   
        BitVector v6 = new BitVector(32, true);
        
        BitVector v8 = new BitVector(32);
        
        assertEquals("11001100000000001010101011110000", v1.toString());
        assertEquals("00000000110101011111111000000000", v2.toString());
        assertEquals("11111111111111111111111111111111", v6.toString());
        assertEquals("00000000000000000000000000000000", v8.toString());
    }
}
