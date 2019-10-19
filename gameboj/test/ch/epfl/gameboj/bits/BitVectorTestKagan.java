package ch.epfl.gameboj.bits;

import static ch.epfl.gameboj.bits.Bits.test;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.Random;

import org.junit.Test;
import org.junit.jupiter.api.Disabled;

import ch.epfl.gameboj.bits.BitVector.Builder;

public class BitVectorTestKagan {
    private Random r = new Random();
    private static final int UPPER_BOUND = 100;
    
    @Test
    public void constructorsDoesntWorkForNegatifValues() {
        assertThrows(IllegalArgumentException.class, () -> {
            new BitVector(-1);
        });
    }
    
    @Test
    public void constructorsDoesntWorkForInvalidValues() {
        assertThrows(IllegalArgumentException.class, () -> {
            int temp = r.nextInt(UPPER_BOUND) * Integer.SIZE + 1;
            new BitVector(temp);
        });
    }
    
    @Test
    public void andDoesntWorkForDifferentArrayLengths() {
        assertThrows(IllegalArgumentException.class, () -> {
            int[] a = new int[3];
            int[] b = new int[7];
            arrayToVector(a).and(arrayToVector(b));
        });
    }
    
    
    @Test
    public void orDoesntWorkForDifferentArrayLengths() {
        assertThrows(IllegalArgumentException.class, () -> {
            int[] a = new int[3];
            int[] b = new int[7];
            arrayToVector(a).or(arrayToVector(b));
        });
    }
    
    @Test
    public void notWorksForRandomValues() {
        int[] randomArray = new int[r.nextInt(UPPER_BOUND)];
        int[] complementArray = new int[randomArray.length];
        int temp;
        for(int i = 0 ; i <  randomArray.length; ++i) {
            temp = r.nextInt(UPPER_BOUND);
            randomArray[i] = temp;
            complementArray[i] = ~temp;
        }
        BitVector bv = arrayToVector(randomArray);
        assertEquals(bv.not() , arrayToVector(complementArray));
    }
    
    @Test
    public void orWorksForRandomValues() {
        int[] randomArray = new int[r.nextInt(UPPER_BOUND)];
        int[] otherArray = new int[randomArray.length];
        int[] disjuncArr =  new int[randomArray.length];
        int temp1;
        int temp2;
        for(int i = 0 ; i <  randomArray.length; ++i) {
            temp1 = r.nextInt(UPPER_BOUND);
            temp2 = r.nextInt(UPPER_BOUND);
            randomArray[i] = temp1;
            otherArray[i] = temp2;
            disjuncArr[i] = temp1 | temp2;
        }
        BitVector bv1 = arrayToVector(randomArray);
        BitVector bv2 = arrayToVector(otherArray);
        assertEquals(bv1.or(bv2) , arrayToVector(disjuncArr));
    }
    
    @Test
    public void andWorksForRandomValues() {
        int[] randomArray = new int[r.nextInt(UPPER_BOUND)];
        int[] otherArray = new int[randomArray.length];
        int[] conjuncArr =  new int[randomArray.length];
        int temp1;
        int temp2;
        for(int i = 0 ; i <  randomArray.length; ++i) {
            temp1 = r.nextInt(UPPER_BOUND);
            temp2 = r.nextInt(UPPER_BOUND);
            randomArray[i] = temp1;
            otherArray[i] = temp2;
            conjuncArr[i] = temp1 & temp2;
        }
        BitVector bv1 = arrayToVector(randomArray);
        BitVector bv2 = arrayToVector(otherArray);
        assertEquals(bv1.and(bv2) , arrayToVector(conjuncArr));
    }
    
    @Test
    public void hashCodeWorksForRandomValues() {
        int[] randomArray = new int[r.nextInt(UPPER_BOUND)];
        int temp;
        for(int i = 0 ; i <  randomArray.length; ++i) {
            temp = r.nextInt(UPPER_BOUND);
            randomArray[i] = temp;
        }
        BitVector bv1 = arrayToVector(randomArray);
        BitVector bv2 = arrayToVector(Arrays.copyOf(randomArray, randomArray.length));
        assertEquals(bv1.hashCode() , bv2.hashCode());
    }
  
   
    @Test
    public void buildCantCalledTwoTimes() {
        assertThrows(IllegalStateException.class, () -> {
            Builder b = new Builder(32);
            b.build();
            b.build();
        });
    }
 
    @Test
    public void setByteCantCalledAfterBuild() {
        assertThrows(IllegalStateException.class, () -> {
            Builder b = new Builder(32);
            b.build();
            b.setByte(1,1);
        });
    }   
    
    @Test
    public void testBitWorksForRandomValues() {
        int[] randomArray = new int[r.nextInt(UPPER_BOUND)];
        int temp;
        for(int i = 0 ; i <  randomArray.length; ++i) {
            temp = r.nextInt(UPPER_BOUND);
            randomArray[i] = temp;
        }
        
        
        int randomIndex = r.nextInt(randomArray.length * Integer.SIZE);
        int arrayIndex = randomIndex / Integer.SIZE;
        int index = randomIndex % Integer.SIZE;
        
        BitVector bv = arrayToVector(randomArray);
        assertEquals(test(randomArray[arrayIndex] , index) , bv.testBit(randomIndex));
    }
    
    @Test
    public void testBitDoesntWorkForNegatifValues() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            BitVector bv = new BitVector(32);
            bv.testBit(-1);
        });
    }
    
    @Test
    public void testBitDoesntWorkForTooLargeValues() {
        assertThrows(IndexOutOfBoundsException.class, () -> {
            int random = r.nextInt(UPPER_BOUND);
            BitVector bv = new BitVector(random * Integer.SIZE);
            bv.testBit(random + r.nextInt());
        });
    }
    

    @Test
    public void toStringWorksForTrivialValue() {
        BitVector bv = new BitVector(Integer.SIZE);
        StringBuilder sb = new StringBuilder();
        for(int i = 0 ; i < Integer.SIZE ; ++i)
            sb.append('0');
        assertEquals(sb.toString() , bv.toString());
    }
    
    
    private static BitVector arrayToVector(int[] array) {
        Builder b = new Builder(array.length * Integer.SIZE);       
        int i = 0;
        for(int x : array ) {
            for(int j = 0; j < 4 ;  ++j) {
                int temp = x >> j * Byte.SIZE;
                temp = temp & 0b1111_1111;
                b.setByte(i, temp);
                ++i;
            }
        }   
        return b.build();
    }
}