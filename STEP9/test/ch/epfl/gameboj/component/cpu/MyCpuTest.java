package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

public class MyCpuTest {
    private static final Opcode[] DIRECT_OPCODE_TABLE =
            buildOpcodeTable(Opcode.Kind.DIRECT);

    private static final Opcode[] PREFIXED_OPCODE_TABLE =
            buildOpcodeTable(Opcode.Kind.PREFIXED);
    private static int PREFIX = 0xCB;

    private boolean valueOverXBits(int v, int limit){
        return false;
    }

    private int testAssembler(Cpu c, boolean mixedCodes, int... codes){
        Ram r = new Ram(65535);
        Bus b = connect(c, r);
        int PC=0;

//        for (int code: codes){
//            System.out.println(String.format("%8s", Integer.toBinaryString(code)).replace(' ', '0'));
//        }

        for (int i=0; i<codes.length; i++){
            b.write(i, codes[i]);
            boolean nextPrefixed = false;
            if(i==PC) {
                if(mixedCodes && nextPrefixed) {
                    PC += DIRECT_OPCODE_TABLE[codes[i]].totalBytes;
                    nextPrefixed=false;
                }
                else {
                    PC += PREFIXED_OPCODE_TABLE[codes[i]].totalBytes;
                    if(codes[i]==PREFIX)
                        nextPrefixed=true;
                }
            }
            //System.out.println("Current cycle: " + i + " next non idle cycle : " + PC);
        }

        cycleCpu(c, PC);

        return PC;
    }

    private Bus connect(Cpu cpu, Ram ram) {
        RamController rc = new RamController(ram, 0);
        Bus b = new Bus();
        cpu.attachTo(b);
        rc.attachTo(b);
        return b;
    }

    private void cycleCpu(Cpu cpu, long cycles) {
        for (long c = 0; c < cycles; ++c) {
            cpu.cycle(c);
            //
            // System.out.println(c);
        }

    }

    private static Opcode[] buildOpcodeTableByFamily(Opcode.Family family) {
        ArrayList<Opcode> opcodes = new ArrayList<>();

        for (Opcode opcode : Opcode.values()) {
            if (opcode.family == family) {
                opcodes.add(opcode);
            }
        }

        return opcodes.toArray(new Opcode[0]);
    }
/*
    @Test
    void nop_DoesNothing() {
        Cpu c = new Cpu();
        Ram r = new Ram(10);
        Bus b = connect(c, r);
        b.write(0, Opcode.NOP.encoding);
        cycleCpu(c, Opcode.NOP.cycles);
        assertArrayEquals(new int[] {1,0,0,0,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void testLD_N16R_A() {
        Cpu c = new Cpu();
        Ram r = new Ram(65535);
        Bus b = connect(c, r);
        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0x89);
        b.write(3, Opcode.LD_N16R_A.encoding);
        b.write(4, 0xEE);
        b.write(5, 0xEE);
        cycleCpu(c, 7);
        assertArrayEquals(new int[] {6, 0, 0x89, 0, 0, 0, 0, 0, 0, 0}, c._testGetPcSpAFBCDEHL());
        assertEquals(b.read(0xEEEE), 0x89, 0.01);
    }
    /////////////////////////////////////////////STEP 4 TESTS ///////////////////////////////////////
    /*
    testINCR8
    I want to increment through all FF possible values of A
    and then get back to 0 returning only the half carry flag and zero flag
    1010 or 10
     */
    @Test
    void fib(){
        Cpu c = new Cpu();
        Ram r = new Ram(65535);
        Bus b = connect(c, r);
        byte k[]=  new byte[] {
                (byte)0x31, (byte)0xFF, (byte)0xFF, (byte)0x3E,
                (byte)0x0B, (byte)0xCD, (byte)0x0A, (byte)0x00,
                (byte)0x76, (byte)0x00, (byte)0xFE, (byte)0x02,
                (byte)0xD8, (byte)0xC5, (byte)0x3D, (byte)0x47,
                (byte)0xCD, (byte)0x0A, (byte)0x00, (byte)0x4F,
                (byte)0x78, (byte)0x3D, (byte)0xCD, (byte)0x0A,
                (byte)0x00, (byte)0x81, (byte)0xC1, (byte)0xC9,
              };
        for(int i=0; i<k.length; i++){
        
            b.write(i, Byte.toUnsignedInt(k[i]));
        }
     //   while(c._testGetPcSpAFBCDEHL()[0]!=8)
      //  while(c._testGetPcSpAFBCDEHL()[0]!=8) 
   //    c.cycle(i);
    //    assertArrayEquals(new int[] {13, 65513, 6, 64, 0, 0, 0, 0, 0, 0}, c._testGetPcSpAFBCDEHL());
    }/*
    @Test
    void testINC_A_WorksForLastValue(){
        Cpu c = new Cpu();
        Ram r = new Ram(65535);
        Bus b = connect(c, r);

        for(int i=0; i<0x100; i++){
            b.write(i, Opcode.INC_A.encoding);
        }
        cycleCpu(c,0x100);

        //FIXME: Should incrementation from 0 leave carry flag at zero or set it true?
        assertArrayEquals(new int[] {256, 0, 0, 0b1010_0000, 0, 0, 0, 0, 0, 0}, c._testGetPcSpAFBCDEHL());
    }
    @Test
    void switchs(){
        Cpu c = new Cpu();
        Ram r = new Ram(65535);
        Bus b = connect(c, r);
        b.write(0, Opcode.LD_SP_N16.encoding);
        b.write(1, 0xFF);
        b.write(2, 0xff);
        b.write(3, Opcode.LD_BC_N16.encoding);
        b.write(4, 34);
        b.write(5, 12);
b.write(6, Opcode.LD_DE_N16.encoding);
b.write(7, 78);
b.write(8, 56);
b.write(9, Opcode.PUSH_BC.encoding);
b.write(10, Opcode.PUSH_DE.encoding);
b.write(11, Opcode.POP_BC.encoding);
b.write(12, Opcode.POP_DE.encoding);

        cycleCpu(c, 23);

        assertArrayEquals(new int[] {13, 0XFFFF, 0, 0, 56, 78, 12, 34, 0, 0}, c._testGetPcSpAFBCDEHL());
    }
    @Test
    void testINC_WorksOnStandardValues(){
        Cpu c = new Cpu();
        Ram r = new Ram(65535);
        Bus b = connect(c, r);
        b.write(0, Opcode.LD_B_N8.encoding);
        b.write(1, 0xF);
        b.write(2, Opcode.INC_B.encoding);

        cycleCpu(c, 3);

        assertArrayEquals(new int[] {3, 0, 0, 0b0010_0000, 0xF+1, 0, 0, 0, 0, 0}, c._testGetPcSpAFBCDEHL());
    }

//    @Test
//    void testINC_HL_WorksForLastValue(){
//        Cpu c = new Cpu();
//
//        int[] incCodes = new int[257];
//        for (int i=0; i<256; i++)
//            incCodes[i] = Opcode.INC_HLR.encoding;
//
//        incCodes[256] = Opcode.LD_E_HLR.encoding;
//
//        int PC = testAssembler(c, false, incCodes);
//
//        assertArrayEquals(new int[] {PC, 0, 0, 0b0000_0000, 0, 0, 0, 0b0000_0001, 0, 0}, c._testGetPcSpAFBCDEHL());
//    }

    @Test
    void testADD_A_WorksForNormal8BitIntegers(){
        Cpu c = new Cpu();
        Ram r = new Ram(65535);
        Bus b = connect(c, r);

        b.write(0, Opcode.LD_A_N8.encoding);
        b.write(1, 0b1000_0000);
        b.write(2, Opcode.ADD_A_N8.encoding);
        b.write(3, 0b1_0000);
        b.write(4, Opcode.ADD_A_N8.encoding);
        b.write(5, 0b1111);
        b.write(6, Opcode.ADD_A_N8.encoding);
        b.write(7, 1);

        int PC = Opcode.LD_A_N8.totalBytes + 3*Opcode.ADD_A_N8.totalBytes;

        cycleCpu(c, PC);

        assertArrayEquals(new int[] {PC, 0, 0b1010_0000, 0b0010_0000, 0, 0, 0, 0, 0, 0}, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void testADD_A_WorksForAll8BitRegs(){
        Cpu c = new Cpu();

        int PC = testAssembler(c, false,
                Opcode.LD_A_N8.encoding, 0b0100_0000, Opcode.ADD_A_A.encoding,
                Opcode.LD_B_N8.encoding, 0b0010_0000, Opcode.ADD_A_B.encoding,
                Opcode.LD_C_N8.encoding, 0b0001_0000, Opcode.ADD_A_C.encoding,
                Opcode.LD_D_N8.encoding, 0b0000_1000, Opcode.ADD_A_D.encoding,
                Opcode.LD_E_N8.encoding, 0b0000_0100, Opcode.ADD_A_E.encoding,
                Opcode.LD_H_N8.encoding, 0b0000_0010, Opcode.ADD_A_H.encoding,
                Opcode.LD_L_N8.encoding, 0b0000_0001, Opcode.ADD_A_L.encoding);

        assertArrayEquals(new int[] {PC, 0, 0b1011_1111, 0b0000_0000, 0b0010_0000, 0b0001_0000, 0b0000_1000, 0b0000_0100, 0b0000_010, 0b0000_0001}, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void testADD_A_setsExpectedFlags(){
        Cpu c = new Cpu();

        int PC = testAssembler(c, false,
                Opcode.ADD_A_A.encoding);

        assertArrayEquals(new int[] {PC,0,0, 0b1000_0000,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());

        c = new Cpu();
        PC = testAssembler(c, false,
                Opcode.LD_A_N8.encoding,
                0b0000_1000,
                Opcode.ADD_A_A.encoding);
        assertArrayEquals(new int[] {PC,0,0b0001_0000, 0b0010_0000,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());

        c = new Cpu();
        PC = testAssembler(c, false,
                Opcode.LD_A_N8.encoding,
                0b1000_0000,
                Opcode.ADD_A_A.encoding);
        assertArrayEquals(new int[] {PC,0,0b0000_0000, 0b1001_0000,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());

        c = new Cpu();
        PC = testAssembler(c, false,
                Opcode.LD_A_N8.encoding,
                0b1111_1111,
                Opcode.LD_B_N8.encoding,
                0b0000_0001,
                Opcode.ADD_A_B.encoding);
        assertArrayEquals(new int[] {PC,0,0, 0b1011_0000,0b0000_0001,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());

        c = new Cpu();
        PC = testAssembler(c, false,
                Opcode.LD_A_N8.encoding,
                0b1000_1000,
                Opcode.LD_B_N8.encoding,
                0b1000_1000,
                Opcode.ADD_A_B.encoding);
        assertArrayEquals(new int[] {PC,0,0b0001_0000, 0b0011_0000,0b1000_1000,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());

        c = new Cpu();
        PC = testAssembler(c, false,
                Opcode.LD_A_N8.encoding,
                0b1111_0000,
                Opcode.LD_B_N8.encoding,
                0b0010_0000,
                Opcode.ADD_A_B.encoding);
        assertArrayEquals(new int[] {PC,0, 0b0001_0000, 0b0001_0000,0b0010_0000,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void testADD_A_HLWorks(){
        Cpu c = new Cpu();

        int PC = testAssembler(c, false,
                Opcode.LD_HLR_N8.encoding,
                0b0000_0011,
                Opcode.LD_A_N8.encoding,
                0b1000_0000,
                Opcode.ADD_A_HLR.encoding);

        assertArrayEquals(new int[] {PC -1 ,0, 0b1000_0011, 0,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void testADC_WorksForDifferentCarriesRegistersValues(){
        Cpu c = new Cpu();

        int PC = testAssembler(c, false,
                Opcode.LD_A_N8.encoding,
                0b1000_0000,
                Opcode.ADD_A_A.encoding,
                Opcode.ADC_A_L.encoding);

        assertArrayEquals(new int[] {PC,0, 0b0000_0001, 0b0000_0000,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());

        c = new Cpu();
        PC = testAssembler(c, false,
                Opcode.LD_A_N8.encoding,
                0b0000_1000,
                Opcode.ADD_A_A.encoding,
                Opcode.ADC_A_L.encoding);

        assertArrayEquals(new int[] {PC,0, 0b0001_0000, 0b0000_0000,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());

        c = new Cpu();
        PC = testAssembler(c, false,
                Opcode.LD_A_N8.encoding,
                0b1000_1000,
                Opcode.ADD_A_A.encoding,
                Opcode.ADC_A_L.encoding);

        assertArrayEquals(new int[] {PC,0, 0b0001_0001, 0b0000_0000,0,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());

        c = new Cpu();
        PC = testAssembler(c, false,
                Opcode.LD_A_N8.encoding,
                0b1111_1111,
                Opcode.LD_B_N8.encoding,
                0b1,
                Opcode.LD_L_N8.encoding,
                0b0001_0000,
                Opcode.ADD_A_B.encoding,
                Opcode.ADC_A_L.encoding);

        assertArrayEquals(new int[] {PC,0, 0b0001_0001, 0b0000_0000,1,0,0,0,0,0b0001_0000}, c._testGetPcSpAFBCDEHL());

        c = new Cpu();
        PC = testAssembler(c, false,
                Opcode.LD_A_N8.encoding,
                0b1111_1111,
                Opcode.LD_B_N8.encoding,
                0b1,
                Opcode.ADD_A_B.encoding,
                Opcode.ADC_A_N8.encoding,
                0b0001_0000);

        assertArrayEquals(new int[] {PC,0, 0b0001_0001, 0b0000_0000,1,0,0,0,0,0}, c._testGetPcSpAFBCDEHL());
    }

    @Test
    void testADD_HL_R16WorksForGeneralCases(){
        Cpu c = new Cpu();

        int PC = testAssembler(c, false,
                Opcode.LD_BC_N16.encoding,
                0b1000_1000, 0b0001_0001,
                Opcode.ADD_HL_BC.encoding);

        assertArrayEquals(new int[] {PC,0, 0b0000_0000, 0b0000_0000,0b0001_0001,0b1000_1000,0,0,0b0001_0001,0b1000_1000}, c._testGetPcSpAFBCDEHL());

        c = new Cpu();

        PC = testAssembler(c, false,
                Opcode.LD_BC_N16.encoding,
                0b1000_1000, 0b0001_0001,
                Opcode.ADD_HL_BC.encoding);

        assertArrayEquals(new int[] {PC,0, 0b0000_0000, 0b0000_0000,0b0001_0001,0b1000_1000,0,0,0b0001_0001,0b1000_1000}, c._testGetPcSpAFBCDEHL());

    }
    //////////////////////////////////////Family Based codes less extensive but faster to write///////////////////////////////////
    //Family ADD_HL_R16SP, BC / DE / … / SP//////////////////////////////////////////////////////////////////////////////////////
//    @Test
//    void testADD_HL_R16WorksForDifferentRegisters(){
//        Cpu c = new Cpu();
//
//        //////////////////////Checks carry from most significant bits and store order////////////////////////////////////////////
//        int PC = testAssembler(c, false,
//                Opcode.LD_HL_N16.encoding,
//                0b1000_0000,
//                0b0000_0000,
//                Opcode.ADD_HL_HL.encoding);
//
//        assertArrayEquals(new int[] {PC,0, 0b0000_0000, 0b00000000,0,0,0,0,0b0000_0001,0b0000_0000}, c._testGetPcSpAFBCDEHL());
//
//        PC = testAssembler(c, false,
//                Opcode.LD_HL_N16.encoding,
//                0b0000_0000,
//                0b0000_1000,
//                Opcode.ADD_HL_HL.encoding);
//
//        assertArrayEquals(new int[] {PC,0, 0b0000_0000, 0b0010_0000,0,0,0,0,0b0001_0000,0b0000_0000}, c._testGetPcSpAFBCDEHL());
//
//        PC = testAssembler(c, false,
//                Opcode.LD_HL_N16.encoding,
//                0b0000_0000,
//                0b1000_0000,
//                Opcode.ADD_HL_HL.encoding);
//
//        assertArrayEquals(new int[] {PC,0, 0b0000_0000, 0b0001_0000,0,0,0,0,0b0000_0000,0b0000_0000}, c._testGetPcSpAFBCDEHL());
//
//    }

    ///////////////////////////////////LD_HLSP_S8 ADD and LD instructions///////////////////////////////////////////////
//    @Test
//    void testADDorLD_SP_SignedValuesWorkCorrectly(){
//        Cpu c = new Cpu();
//
//        int PC = testAssembler(c, false,
//                Opcode.ADD_SP_N.encoding,
//                0xFF);
//
//        assertArrayEquals(new int[] {PC,0b1111_1111, 0b0000_0000, 0b0000_0000,0,0,0,0,0b0000_0000,0b0000_0000}, c._testGetPcSpAFBCDEHL());
//
//        Cpu cHL = new Cpu();
//
//        int PCHL = testAssembler(cHL, false,
//                Opcode.ADD_SP_N.encoding,
//                0b001_0011,
//                Opcode.LD_HL_SP_N8.encoding,
//                //-3 in two's complement
//                0b1111_1101
//                );
//
//        Cpu cSP = new Cpu();
//        int PCSP = testAssembler(cSP, false,
//                Opcode.ADD_SP_N.encoding,
//                0b000_0011,
//                Opcode.ADD_SP_N.encoding,
//                0b1111_1101
//                );
//
//        //Same values inserted to ADD SP and LD HL SP should return the same array
//        assertEquals(cHL._testGetPcSpAFBCDEHL()[9], cSP._testGetPcSpAFBCDEHL()[1]);
//
//    }

    ////////////////////////////family SUB A all registers borrow half borow

    //Todo: Par contre, les paires de registres BC, DE, HL, de même que le registre SP peuvent très bien
    // être incrémentés/décrémentés lorsqu’ils sont à l’une ou l’autre extrémité de l’espace mémoire, et
    // il faut donc traiter ce cas correctement.
    //Par contre, lorsque vous incrémentez/décrémentez l’une des paires BC, DE, HL ou le registre 16 bits
    // SP, vous devez absolument traiter correctement le cas où vous passez d’une extrémité à l’autre de
    // l’espace d’adressage. Comme cela a été dit ailleurs, cela se fait facilement au moyen d’un appel à Bits.clip

    //Todo: En résumé : lorsque vous faites des lectures/écritures 16 bits (p.ex. dans read16 et write16), vous
    // n’avez pas besoin de gérer « correctement » (sans planter) le cas où l’adresse vaut 0xFFFF. Vous pouvez aussi
    // très bien arrêter l’exécution de votre programme, soit au moyen d’une assertion, soit en passant l’adresse
    // invalide 0x10000 à bus.read et en provoquant une exception. Il en va de même pour les méthodes qui lisent la
    // valeur 8 ou 16 bits suivant le PC (read8AfterOpcode et read16AfterOpcode).

    //TODO: POP et PUSH lancent une exception après OxFFFF
*/
    private static Opcode[] buildOpcodeTable(Opcode.Kind k){
        Opcode[] codeTable = new Opcode[256];

        for (Opcode code : Opcode.values())
            if(code.kind == k)
                codeTable[code.encoding] = code;

        return codeTable;
    }
}
