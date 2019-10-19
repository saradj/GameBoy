package ch.epfl.gameboj.bits;

import ch.epfl.gameboj.bits.BitVector.Builder;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class Step7_BitVectorTest_0859 {

    @Test
    void test() {
 //       fail("Not yet implemented");
    }
    
    @Test
    void constructor0And1Fails() {
   //     BitVector bvec = new BitVector(0, false);
        assertThrows(IllegalArgumentException.class,
                () -> new BitVector(2, false));
       
    }
    @Disabled
    @Test
    void testing() {
        BitVector v = new BitVector.Builder(32)
                .setByte(0, 0b0000_1111)
                .setByte(1, 0b1010_1010)
                .setByte(2, 0xFF)
                .setByte(3, 0b1111_1111)
                .build();
        int n = 8;
        BitVector j = v.shift(n).shift(-n);
        BitVector k = v.shift(-v.size()+n).shift(v.size()-n);
        System.out.println("j:"+j);
        System.out.println("k:"+k);
        
        
    }
    @Test
    void hashCode_Equals_Test() {
        int []i = new int[1];
        i[0] = 0;
        BitVector b0 = new BitVector(32, false);
        BitVector b1 = new BitVector(32, false);
        assertTrue(b0.equals(b1));
        assertEquals(b1.hashCode(), b0.hashCode());
        assertEquals(Arrays.hashCode(i), new BitVector(32, false).hashCode());
        Arrays.fill(i, -1);
        assertEquals(Arrays.hashCode(i), new BitVector(32, true).hashCode());
        
        Builder b5 = new BitVector.Builder(64);
        b5.setByte(0, 0b1111_1111).setByte(1, 0b1111_1111).setByte(2, 0b00010001).setByte(3, 0b1111_1111)
          .setByte(4, 0b1111_0000).setByte(5, 0b1010_1010).setByte(7, 0b1100_1100);
        BitVector v5 = b5.build();
        Builder b6 = new BitVector.Builder(64);
        b6.setByte(0, 0b1111_1111).setByte(1, 0b1111_1111).setByte(2, 0b00010001).setByte(3, 0b1111_1111)
        .setByte(4, 0b1111_0000).setByte(5, 0b1010_1010).setByte(7, 0b1100_1100);
        BitVector v6 = b6.build();
        assertEquals("11001100000000001010101011110000"+"11111111"+"00010001"+"11111111"+"11111111", v5.toString());
        int [] j = {0b11111111_00010001_11111111_11111111, 0b11001100_00000000_10101010_11110000};
        assertEquals(Arrays.hashCode(j), v5.hashCode());
        assertTrue(v5.equals(v6));
    }
    
    @Test
    void not_and_Test() {
        Builder b5 = new BitVector.Builder(64);
        b5.setByte(0, 0b1111_1111).setByte(1, 0b1111_1111).setByte(2, 0b00010001).setByte(3, 0b1111_1111)
          .setByte(4, 0b1111_0000).setByte(5, 0b1010_1010).setByte(7, 0b1100_1100);
        BitVector v5 = b5.build();
        BitVector v7 = v5.not().not();
        assertTrue(v5.equals(v7));
        assertTrue(new BitVector(32).not().equals(new BitVector(32, true)));
        BitVector v8 = v5.and(new BitVector(64, true));
        assertTrue(v5.equals(v8));
    }
    @Disabled
    @Test
    void testinganithing() {
        System.out.println("testinganithing debut");
        BitVector bVec = new BitVector(32, true);
        System.out.println(bVec.toString());
        System.out.println("size = "+ 32*50+ " isEqueal to : "+ bVec.size());
        System.out.println(bVec.testBit(17));
        BitVector vfr = bVec.extractZeroExtended(0, 64);
        
        System.out.println(-31% 32 == 0);
        System.out.println("extract : "+vfr.toString());
     
//        BitVector vf = bVec.extractZeroExtended(-8, 32);
        BitVector vf = bVec.extractWrapped(-8, 32);
        System.out.println("                 "+bVec.toString());
        System.out.println("extract vrot -8: " + vf.toString());
        System.out.println("testinganithing fin");
        
        
//        assertTrue(false);
    }
    
    @Test
    void profTest() {
        BitVector v1 = new BitVector(32, true);
        BitVector v2 = v1.extractZeroExtended(-17, 32).not();
        BitVector v3 = v2.extractWrapped(11, 64);
        String s = new String();
        for (BitVector v: List.of(v1, v2, v3)) {
//          System.out.print(v);
          s += v.toString();
        }
        assertEquals("11111111111111111111111111111111000000000000000"
                +"111111111111111111111111111100000000000000011111111111111111000000000000000111111", s);
    }
    @Test
    void profTest1() {
        BitVector v = new BitVector.Builder(64)
                .setByte(0, 0b1111_0000)
                .setByte(1, 0b1010_1010)
                .setByte(3, 0b1100_1100)
                .build();
     //         System.out.println(v);
        assertEquals("0000000000000000000000000000000011001100000000001010101011110000", v.toString());
    }
    
    //bon test
    @Test
    void builderTest() {
//      cas à erreure
        Builder b = new BitVector.Builder(64);
        b.setByte(0, 0b1111_0000).setByte(1, 0b1010_1010);
        
        assertThrows(IndexOutOfBoundsException.class, ()->b.setByte(8, 0));
        assertThrows(IndexOutOfBoundsException.class, ()->b.setByte(-1, 0));
        assertThrows(IndexOutOfBoundsException.class, ()->b.setByte(Integer.MAX_VALUE, 0));
        assertThrows(IndexOutOfBoundsException.class, ()->b.setByte(Integer.MIN_VALUE, 0));

        assertThrows(IllegalArgumentException.class, ()->b.setByte(0, 0b1_0000_0000));
        assertThrows(IllegalArgumentException.class, ()-> new BitVector.Builder(0));
        assertThrows(IllegalArgumentException.class, ()-> new BitVector.Builder(-1));
        assertThrows(IllegalArgumentException.class, ()-> new BitVector.Builder(2));
        assertThrows(IllegalArgumentException.class, ()-> new BitVector.Builder(-32));

        b.build();
        assertThrows(IllegalStateException.class,() -> b.setByte(0, 0));
        assertThrows(IllegalStateException.class,() -> b.build());
        
//      fonctionement correct
        Builder b1 = new BitVector.Builder(32);
        b1.setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100);
        BitVector v1 = b1.build();
        assertEquals("11001100000000001010101011110000", v1.toString());
//      nume octet    33333333222222221111111100000000
        
//      reecriture propre
        Builder b2 = new BitVector.Builder(32);
        b2.setByte(0, 0b1111_0000).setByte(1, 0b1010_1010).setByte(3, 0b1100_1100).setByte(0, 0b1111_1111);
        BitVector v2 = b2.build();
        assertEquals("11001100000000001010101011111111", v2.toString());
//      nume octet    33333333222222221111111100000000
        Builder b3 = new BitVector.Builder(32);
        b3.setByte(0, 0b1111_1111).setByte(1, 0b1111_1111).setByte(2, 0b1111_1111).setByte(3, 0b1111_1111);
        BitVector v3 = b3.build();
        assertEquals("11111111"+"11111111"+"11111111"+"11111111", v3.toString());
//      nume octet    33333333   22222222   11111111   00000000
        Builder b4 = new BitVector.Builder(32);
        b4.setByte(0, 0b1111_1111).setByte(1, 0b1111_1111).setByte(2, 0b1111_1111).setByte(3, 0b1111_1111)
          .setByte(2, 0x11);
        BitVector v4 = b4.build();
        assertEquals("11111111"+"00010001"+"11111111"+"11111111", v4.toString());
//      nume octet    33333333   22222222   11111111   00000000
        Builder b5 = new BitVector.Builder(64);
        b5.setByte(0, 0b1111_1111).setByte(1, 0b1111_1111).setByte(2, 0b00010001).setByte(3, 0b1111_1111)
          .setByte(4, 0b1111_0000).setByte(5, 0b1010_1010).setByte(7, 0b1100_1100);
        BitVector v5 = b5.build();
        assertEquals("11001100000000001010101011110000"+"11111111"+"00010001"+"11111111"+"11111111", v5.toString());
//      nume octet    73333333622222225111111140000000   33333333   22222222   11111111   00000000

        Builder b6 = new BitVector.Builder(64);
        b6.setByte(0, 0b1111_1111).setByte(1, 0b1111_1111).setByte(2, 0b00010001).setByte(3, 0b1111_1111)
        .setByte(4, 0b1111_1001).setByte(4, 0b1000_0001);
        BitVector v6 = b6.build();
        assertEquals("00000000000000000000000010000001"+"11111111"+"00010001"+"11111111"+"11111111", v6.toString());
//      nume octet    73333333622222225111111140000000   33333333   22222222   11111111   00000000
    }
}
