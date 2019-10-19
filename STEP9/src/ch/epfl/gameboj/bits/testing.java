package ch.epfl.gameboj.bits;

import java.util.List;

public abstract class testing {

    public static void main(String[] args) {
        
        BitVector[] masks = new BitVector[4];
        masks[0]= new BitVector(32);
        masks[1]=masks[0];
        masks[0]=masks[1].and(masks[0]);
        
         System.err.println(masks[0].size());
    }

}
