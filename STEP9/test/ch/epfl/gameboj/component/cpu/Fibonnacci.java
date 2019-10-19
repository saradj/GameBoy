package ch.epfl.gameboj.component.cpu;

import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertArrayEquals;

import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

class Fibonnacci {
    
    int Pc=0;
    Bus b = new Bus();
    
    private Bus connect(Cpu cpu, Ram ram) {
        RamController rc = new RamController(ram, 0);
        Bus b = new Bus();
        cpu.attachTo(b);
        rc.attachTo(b);
        return b;
    }
    
    private void cycleCpu(Cpu cpu) {
        long c=0;
      while (cpu._testGetPcSpAFBCDEHL()[0]!=8  ) {
          cpu.cycle(c);
          c++;
          
      }
      
    }
    
    
    

    byte k[]=  new byte[] {
            (byte)0x31, (byte)0xFF, (byte)0xFF, (byte)0x3E,
            (byte)0x0B, (byte)0xCD, (byte)0x0A, (byte)0x00,
            (byte)0x76, (byte)0x00, (byte)0xFE, (byte)0x02,
            (byte)0xD8, (byte)0xC5, (byte)0x3D, (byte)0x47,
            (byte)0xCD, (byte)0x0A, (byte)0x00, (byte)0x4F,
            (byte)0x78, (byte)0x3D, (byte)0xCD, (byte)0x0A,
            (byte)0x00, (byte)0x81, (byte)0xC1, (byte)0xC9,
          };
    
 /*   void cycleTillPc(Cpu cpu, int pc) {
        int cycle=0;
        while(cpu._testGetPcSpAFBCDEHL()[0]!=pc) {
            cpu.cycle(cycle);
            cycle++;
        }
    }*/
    
    
    @Test
    void fib(){
        Cpu c = new Cpu();
        Ram r = new Ram(65535);
        b=connect(c, r);
      
        
        for(int i=0; i<k.length; i++){
        
            b.write(i, Byte.toUnsignedInt(k[i]));
        }
      cycleCpu(c);

      assertArrayEquals(new int[] {8, 0xffff, 89, 0, 0, 0, 0, 0, 0, 0}, c._testGetPcSpAFBCDEHL());
      
        assertEquals(89, c._testGetPcSpAFBCDEHL()[2]);
    }
}