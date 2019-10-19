package ch.epfl.gameboj.component.cpu;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Alu.Flag;
import ch.epfl.gameboj.component.cpu.Alu.RotDir;
import ch.epfl.gameboj.component.memory.Ram;

/**
 * CS-108-GameBoy 
 * Cpu.java 
 * Purpose:Simulates the {@link GameBoy} processor
 * attached to the bus and driven by the clock
 * 
 * @author Sara Djambazovska
 * @author Marouane Jaakik
 *
 */
public final class Cpu implements Component, Clocked {

    private final Ram highRam = new Ram(AddressMap.HIGH_RAM_SIZE);
    private static final Opcode[] DIRECT_OPCODE_TABLE = buildOpcodeTable(
            Opcode.Kind.DIRECT);
    private static final Opcode[] PREFIXED_OPCODE_TABLE = buildOpcodeTable(
            Opcode.Kind.PREFIXED);
    private long nextNonIdleCycle = 0;
    private boolean ime = false;
    private int SP = 0;
    private int PC = 0, IF = 0, IE = 0;
    private static final int OPCODE_PREFIX = 0xCB;
    private Bus bus;
    private RegisterFile<Reg> registerFile = new RegisterFile<>(Reg.values());

    /**
     * Enumeration of {@link Register} of 8 bits
     *
     */
    private enum Reg implements Register {
        A, F, B, C, D, E, H, L
    }

    /**
     * Enumeration of pairs of {@link Register} containing 16 bits
     *
     */
    private enum Reg16 implements Register {
        AF, BC, DE, HL
    }

    /**
     * Enumeration of Interrupts
     */
    public enum Interrupt implements Bit {
        VBLANK, LCD_STAT, TIMER, SERIAL, JOYPAD
    }

    /**
     * Enumeration of the 4 possible sources of a {@link Flag}
     */
    public enum FlagSrc {
        V0, V1, ALU, CPU
    }

    /**
     * Determines whether or not the processor should do anything during this
     * cycle, and if so, it calls ReallyCycle below
     * 
     * @param cycle
     *            long value, represents the time to execute an elementary
     *            machine instruction, some processor instruction might require
     *            multiple cycles
     */
    @Override
    public void cycle(long cycle) {
        if (nextNonIdleCycle == Long.MAX_VALUE && testIE_IF())
            nextNonIdleCycle = cycle;

        if (nextNonIdleCycle == cycle)
            reallyCycle();
        return;
    }

    /**
     * Checks whether {@link Interrupt} are enabled (i.e if IME is true) and if
     * an interruption is pending, it handles it accordingly, otherwise it asks
     * the component to evolve by executing all the operations it has to perform
     */

    public void reallyCycle() {

        if (ime && testIE_IF())

        {
            int i = indexInterrupt();
            this.ime = false;
            IF = Bits.set(IF, i, false);
            push16(PC);
            PC = AddressMap.INTERRUPTS[i];
            nextNonIdleCycle += 5;

        } else if (read8(PC) == OPCODE_PREFIX) {
            dispatch(PREFIXED_OPCODE_TABLE[read8AfterOpcode()]);

        } else
            dispatch(DIRECT_OPCODE_TABLE[read8(PC)]);
    }

    private void writeRegF(int num) {
        registerFile.set(Reg.F, num);
    }

    /**
     * Given a byte containing an opcode, execute the corresponding instruction,
     * reading or writing, if necessary, values from the bus or registers.
     * 
     * @param opcode
     *            an {@link Opcode}, is the first byte of the instruction, that
     *            tells the processor what operations need to be performed.
     */

    private void dispatch(Opcode opcode) {

        int nextPc = PC + opcode.totalBytes;
        switch (opcode.family) {
        // load
        case NOP: {
        }
            break;

        case LD_R8_HLR: {
            Reg r = extractReg(opcode, 3);
            registerFile.set(r, read8AtHl());
        }
            break;

        case LD_A_HLRU: {
            registerFile.set(Reg.A, read8AtHl());
            setReg16(Reg16.HL, Bits.clip(16,
                    reg16(Reg16.HL) + extractHlIncrement(opcode)));
        }
            break;

        case LD_A_N8R: {
            registerFile.set(Reg.A,
                    read8(AddressMap.REGS_START + read8AfterOpcode()));
        }
            break;

        case LD_A_CR: {
            registerFile.set(Reg.A,
                    read8(AddressMap.REGS_START + registerFile.get(Reg.C)));
        }
            break;

        case LD_A_N16R: {
            registerFile.set(Reg.A, read8(read16AfterOpcode()));
        }
            break;

        case LD_A_BCR: {
            registerFile.set(Reg.A, read8(reg16(Reg16.BC)));
        }
            break;

        case LD_A_DER: {
            registerFile.set(Reg.A, read8(reg16(Reg16.DE)));
        }
            break;

        case LD_R8_N8: {
            Reg r = extractReg(opcode, 3);
            registerFile.set(r, read8AfterOpcode());
        }
            break;

        case LD_R16SP_N16: {
            Reg16 r = extractReg16(opcode);
            setReg16SP(r, read16AfterOpcode());
        }
            break;

        case POP_R16: {
            Reg16 r = extractReg16(opcode);
            setReg16(r, pop16());
        }
            break;

        // store
        case LD_HLR_R8: {
            Reg r = extractReg(opcode, 0);
            write8AtHl(registerFile.get(r));
        }
            break;

        case LD_HLRU_A: {
            write8AtHl(registerFile.get(Reg.A));
            setReg16(Reg16.HL, Bits.clip(16,
                    reg16(Reg16.HL) + extractHlIncrement(opcode)));
        }
            break;

        case LD_N8R_A: {
            write8(AddressMap.REGS_START + read8AfterOpcode(),
                    registerFile.get(Reg.A));
        }
            break;

        case LD_CR_A: {
            write8(AddressMap.REGS_START + registerFile.get(Reg.C),
                    registerFile.get(Reg.A));
        }
            break;

        case LD_N16R_A: {
            write8(read16AfterOpcode(), registerFile.get(Reg.A));
        }
            break;

        case LD_BCR_A: {
            write8(reg16(Reg16.BC), registerFile.get(Reg.A));
        }
            break;

        case LD_DER_A: {
            write8(reg16(Reg16.DE), registerFile.get(Reg.A));
        }
            break;

        case LD_HLR_N8: {
            write8AtHl(read8AfterOpcode());
        }
            break;

        case LD_N16R_SP: {
            write16(read16AfterOpcode(), SP);
        }
            break;

        case PUSH_R16: {
            Reg16 r = extractReg16(opcode);
            push16(reg16(r));
        }
            break;

        // move
        case LD_R8_R8: {
            Reg r = extractReg(opcode, 3);
            Reg s = extractReg(opcode, 0);
            if (!r.equals(s))
                registerFile.set(r, registerFile.get(s));
        }
            break;
        case LD_SP_HL: {
            SP = reg16(Reg16.HL);
        }
            break;

        // Add
        case ADD_A_R8: {
            Reg r = extractReg(opcode, 0);
            setRegFlags(Reg.A, Alu.add(registerFile.get(Reg.A),
                    registerFile.get(r), getCarryADD(opcode)));
        }
            break;

        case ADD_A_N8: {
            setRegFlags(Reg.A, Alu.add(registerFile.get(Reg.A),
                    read8AfterOpcode(), getCarryADD(opcode)));
        }
            break;

        case ADD_A_HLR: {
            setRegFlags(Reg.A, Alu.add(registerFile.get(Reg.A), read8AtHl(),
                    getCarryADD(opcode)));
        }
            break;

        case INC_R8: {
            Reg r = extractReg(opcode, 3);
            int vf = Alu.add(registerFile.get(r), 1);
            registerFile.set(r, Alu.unpackValue(vf));
            combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU,
                    FlagSrc.CPU);
        }
            break;

        case INC_HLR: {
            int vf = Alu.add(read8AtHl(), 1);
            write8AtHl(Alu.unpackValue(vf));
            combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V0, FlagSrc.ALU,
                    FlagSrc.CPU);
        }
            break;

        case INC_R16SP: {
            Reg16 r = extractReg16(opcode);
            int vf = Alu.add16H(reg16SP(r), 1);
            setReg16SP(r, Alu.unpackValue(vf));
        }
            break;

        case ADD_HL_R16SP: {
            Reg16 r = extractReg16(opcode);
            int vf = Alu.add16H(reg16(Reg16.HL), reg16SP(r));
            setReg16(Reg16.HL, Alu.unpackValue(vf));
            combineAluFlags(vf, FlagSrc.CPU, FlagSrc.V0, FlagSrc.ALU,
                    FlagSrc.ALU);
        }
            break;

        case LD_HLSP_S8: {
            int e = Bits.clip(16, Bits.signExtend8(read8AfterOpcode()));
            int vf = Alu.add16L(SP, e);
            combineAluFlags(vf, FlagSrc.V0, FlagSrc.V0, FlagSrc.ALU,
                    FlagSrc.ALU);
            if (Bits.test(opcode.encoding, 4)) {
                setReg16(Reg16.HL, Alu.unpackValue(vf));
            } else
                SP = Alu.unpackValue(vf);
        }
            break;

        // Subtract
        case SUB_A_R8: {
            Reg r = extractReg(opcode, 0);
            int vf = Alu.sub(registerFile.get(Reg.A), registerFile.get(r),
                    getCarryADD(opcode));
            setRegFlags(Reg.A, vf);
        }
            break;

        case SUB_A_N8: {
            int vf = Alu.sub(registerFile.get(Reg.A), read8AfterOpcode(),
                    getCarryADD(opcode));
            setRegFlags(Reg.A, vf);
        }
            break;

        case SUB_A_HLR: {
            setRegFlags(Reg.A, Alu.sub(registerFile.get(Reg.A), read8AtHl(),
                    getCarryADD(opcode)));
        }
            break;

        case DEC_R8: {
            Reg r = extractReg(opcode, 3);
            int vf = Alu.sub(registerFile.get(r), 1);
            setRegFromAlu(r, vf);
            combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU,
                    FlagSrc.CPU);
        }
            break;

        case DEC_HLR: {
            int vf = Alu.sub(read8AtHl(), 1);
            write8AtHl(Alu.unpackValue(vf));
            combineAluFlags(vf, FlagSrc.ALU, FlagSrc.V1, FlagSrc.ALU,
                    FlagSrc.CPU);
        }
            break;

        case CP_A_R8: {
            Reg r = extractReg(opcode, 0);
            setFlags(Alu.sub(registerFile.get(Reg.A), registerFile.get(r)));
        }
            break;

        case CP_A_N8: {
            setFlags(Alu.sub(registerFile.get(Reg.A), read8AfterOpcode()));
        }
            break;

        case CP_A_HLR: {
            setFlags(Alu.sub(registerFile.get(Reg.A), read8AtHl()));
        }
            break;

        case DEC_R16SP: {
            Reg16 r = extractReg16(opcode);
            setReg16SP(r, Bits.clip(16, reg16SP(r) - 1));
        }
            break;

        // And, or, xor, complement
        case AND_A_N8: {
            setRegFlags(Reg.A,
                    Alu.and(registerFile.get(Reg.A), read8AfterOpcode()));
        }
            break;

        case AND_A_R8: {
            Reg r = extractReg(opcode, 0);
            setRegFlags(Reg.A,
                    Alu.and(registerFile.get(Reg.A), registerFile.get(r)));
        }
            break;

        case AND_A_HLR: {
            setRegFlags(Reg.A, Alu.and(registerFile.get(Reg.A), read8AtHl()));
        }
            break;

        case OR_A_R8: {
            Reg r = extractReg(opcode, 0);
            setRegFlags(Reg.A,
                    Alu.or(registerFile.get(Reg.A), registerFile.get(r)));
        }
            break;

        case OR_A_N8: {
            setRegFlags(Reg.A,
                    Alu.or(registerFile.get(Reg.A), read8AfterOpcode()));
        }
            break;

        case OR_A_HLR: {
            setRegFlags(Reg.A, Alu.or(registerFile.get(Reg.A), read8AtHl()));
        }
            break;

        case XOR_A_R8: {
            Reg r = extractReg(opcode, 0);
            setRegFlags(Reg.A,
                    Alu.xor(registerFile.get(Reg.A), registerFile.get(r)));
        }
            break;

        case XOR_A_N8: {
            setRegFlags(Reg.A,
                    Alu.xor(registerFile.get(Reg.A), read8AfterOpcode()));
        }
            break;

        case XOR_A_HLR: {
            setRegFlags(Reg.A, Alu.xor(registerFile.get(Reg.A), read8AtHl()));
        }
            break;

        case CPL: {
            registerFile.set(Reg.A, Bits.complement8(registerFile.get(Reg.A)));
            combineAluFlags(0, FlagSrc.CPU, FlagSrc.V1, FlagSrc.V1,
                    FlagSrc.CPU);
        }
            break;

        // Rotate, shift
        case ROTCA: {
            int vf = Alu.rotate(rotDir(opcode), registerFile.get(Reg.A));
            setRegFromAlu(Reg.A, vf);
            combineAluFlags(vf, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0,
                    FlagSrc.ALU);
        }
            break;

        case ROTA: {
            int vf = Alu.rotate(rotDir(opcode), registerFile.get(Reg.A),
                    Bits.test(registerFile.get(Reg.F), 4));
            setRegFromAlu(Reg.A, vf);
            combineAluFlags(vf, FlagSrc.V0, FlagSrc.V0, FlagSrc.V0,
                    FlagSrc.ALU);
        }
            break;

        case ROTC_R8: {
            Reg r = extractReg(opcode, 0);
            setRegFlags(r, Alu.rotate(rotDir(opcode), registerFile.get(r)));
        }
            break;

        case ROT_R8: {
            Reg r = extractReg(opcode, 0);
            setRegFlags(r, Alu.rotate(rotDir(opcode), registerFile.get(r),
                    Bits.test(registerFile.get(Reg.F), 4)));
        }
            break;

        case ROTC_HLR: {
            write8AtHlAndSetFlags(Alu.rotate(rotDir(opcode), read8AtHl()));
        }
            break;

        case ROT_HLR: {
            write8AtHlAndSetFlags(Alu.rotate(rotDir(opcode), read8AtHl(),
                    Bits.test(registerFile.get(Reg.F), 4)));
        }
            break;

        case SWAP_R8: {
            Reg r = extractReg(opcode, 0);
            setRegFlags(r, Alu.swap(registerFile.get(r)));
        }
            break;

        case SWAP_HLR: {
            write8AtHlAndSetFlags(Alu.swap(read8AtHl()));
        }
            break;

        case SLA_R8: {
            Reg r = extractReg(opcode, 0);
            setRegFlags(r, Alu.shiftLeft(registerFile.get(r)));
        }
            break;

        case SRA_R8: {
            Reg r = extractReg(opcode, 0);
            setRegFlags(r, Alu.shiftRightA(registerFile.get(r)));
        }
            break;

        case SRL_R8: {
            Reg r = extractReg(opcode, 0);
            setRegFlags(r, Alu.shiftRightL(registerFile.get(r)));
        }
            break;

        case SLA_HLR: {
            write8AtHlAndSetFlags(Alu.shiftLeft(read8AtHl()));
        }
            break;

        case SRA_HLR: {
            write8AtHlAndSetFlags(Alu.shiftRightA(read8AtHl()));
        }
            break;

        case SRL_HLR: {
            write8AtHlAndSetFlags(Alu.shiftRightL(read8AtHl()));
        }
            break;

        // Bit test and set
        case BIT_U3_R8: {
            Reg r = extractReg(opcode, 0);
            if (Bits.test(registerFile.get(r), extractN3(opcode))) {
                combineAluFlags(0, FlagSrc.V0, FlagSrc.V0, FlagSrc.V1,
                        FlagSrc.CPU);
            } else
                combineAluFlags(0, FlagSrc.V1, FlagSrc.V0, FlagSrc.V1,
                        FlagSrc.CPU);
        }
            break;

        case BIT_U3_HLR: {
            if (Bits.test(read8AtHl(), extractN3(opcode)))
                combineAluFlags(0, FlagSrc.V0, FlagSrc.V0, FlagSrc.V1,
                        FlagSrc.CPU);
            else
                combineAluFlags(0, FlagSrc.V1, FlagSrc.V0, FlagSrc.V1,
                        FlagSrc.CPU);
        }
            break;

        case CHG_U3_R8: {
            Reg r = extractReg(opcode, 0);
            registerFile.set(r, Bits.set(registerFile.get(r), extractN3(opcode),
                    test6_Opcode(opcode)));
        }
            break;

        case CHG_U3_HLR: {
            write8AtHl(Bits.set(read8AtHl(), extractN3(opcode),
                    test6_Opcode(opcode)));
        }
            break;

        // Misc. ALU
        case DAA: {
            int vf = Alu.bcdAdjust(registerFile.get(Reg.A),
                    Bits.test(registerFile.get(Reg.F), 6),
                    Bits.test(registerFile.get(Reg.F), 5),
                    Bits.test(registerFile.get(Reg.F), 4));
            setRegFromAlu(Reg.A, vf);
            combineAluFlags(vf, FlagSrc.ALU, FlagSrc.CPU, FlagSrc.V0,
                    FlagSrc.ALU);
        }
            break;

        case SCCF: {
            if (!getCarryADD(opcode))
                combineAluFlags(0, FlagSrc.CPU, FlagSrc.V0, FlagSrc.V0,
                        FlagSrc.V1);
            else
                combineAluFlags(0, FlagSrc.CPU, FlagSrc.V0, FlagSrc.V0,
                        FlagSrc.V0);
        }
            break;

        // Jumps
        case JP_HL: {
            nextPc = reg16(Reg16.HL);
        }
            break;

        case JP_N16: {
            nextPc = read16AfterOpcode();
        }
            break;

        case JP_CC_N16: {
            if (condition(opcode)) {
                nextPc = read16AfterOpcode();
                nextNonIdleCycle += opcode.additionalCycles;
            }
        }
            break;

        case JR_E8: {
            int e = Bits.signExtend8(read8AfterOpcode());
            nextPc = Bits.clip(16, nextPc + e);
        }
            break;
        case JR_CC_E8: {
            if (condition(opcode)) {
                int e = Bits.signExtend8(read8AfterOpcode());
                nextPc = Bits.clip(16, nextPc + e);
                nextNonIdleCycle += opcode.additionalCycles;
            }
        }
            break;

        // Calls and returns
        case CALL_N16: {
            push16(nextPc);
            nextPc = read16AfterOpcode();
        }
            break;

        case CALL_CC_N16: {
            if (condition(opcode)) {
                push16(nextPc);
                nextPc = read16AfterOpcode();
                nextNonIdleCycle += opcode.additionalCycles;
            }
        }
            break;

        case RST_U3: {
            push16(nextPc);
            nextPc = AddressMap.RESETS[extractN3(opcode)];
        }
            break;

        case RET: {
            nextPc = pop16();
        }
            break;

        case RET_CC: {
            if (condition(opcode)) {
                nextPc = pop16();
                nextNonIdleCycle += opcode.additionalCycles;
            }
        }
            break;

        // Interrupts
        case EDI: {
           
                ime = Bits.test(opcode.encoding, 3);
        }
            break;

        case RETI: {
            ime = true;
            nextPc = pop16();
        }
            break;

        // Misc control
        case HALT: {
            nextNonIdleCycle = Long.MAX_VALUE;
        }
            break;

        case STOP:
            throw new Error("STOP is not implemented");
        }

        PC = nextPc;
        nextNonIdleCycle += opcode.cycles;
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address == AddressMap.REG_IE)
            return IE;

        if (address == AddressMap.REG_IF)
            return IF;
        if (address >= AddressMap.HIGH_RAM_START
                && address < AddressMap.HIGH_RAM_END)
            return highRam.read(address - AddressMap.HIGH_RAM_START);

        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);

        if (address == AddressMap.REG_IE)
            IE = data;
        if (address == AddressMap.REG_IF)
            IF = data;
        if (address >= AddressMap.HIGH_RAM_START
                && address < AddressMap.HIGH_RAM_END)
            highRam.write(address - AddressMap.HIGH_RAM_START, data);
    }

    /**
     * @return an int array of size 10 containing the value of the following
     *         registers in this order: SP,PC,A,F,B,C,D,E,H,L
     */
    public int[] _testGetPcSpAFBCDEHL() {
        int[] t = new int[10];
        t[0] = PC;
        t[1] = SP;
        for (int i = 0; i < 8; i++)
            t[i + 2] = registerFile.get(Reg.values()[i]);
        return t;
    }

    @Override
    public void attachTo(Bus bus) {
        bus.attach(this);
        this.bus = bus;
    }

    /**
     * Raises the given interrupt, i.e sets the corresponding bit in the IF
     * register
     * 
     * @param i
     *            Interrupt, interruption to be raised
     */
    public void requestInterrupt(Interrupt i) {
        IF = Bits.set(IF, i.index(), true);
    }

    /**
     * Constructs an Opcode Table containing all the opcodes of the given kind
     * 
     * @param kind
     *            a kind of Opcode
     * @return an Opcode array of the given kind
     */
    private static Opcode[] buildOpcodeTable(Opcode.Kind kind) {
        Opcode[] opcode = new Opcode[256];
        for (Opcode o : Opcode.values())
            if (o.kind.equals(kind))
                opcode[o.encoding] = o;

        return opcode;
    }

    /**
     * Reads from the bus the value 8 bits at the given address
     *
     * @param address
     *            integer value of 16 bits, the location at which we want to
     *            read the stored value
     * 
     *
     * @return an 8 bits Data stored in the given address
     * 
     */
    private int read8(int address) {
        return bus.read(address);
    }

    /**
     * Reads from the bus the value 8 bits to the address contained in the pair
     * of registers HL
     * 
     * @return 8 bits Data stored in HL
     */
    private int read8AtHl() {
        return read8(reg16(Reg16.HL));
    }

    /**
     * Reads from the bus the 8 bits value at the address following the one
     * contained in the program counter, i.e at address PC + 1
     * 
     * @return 8 bits Data stored in PC + 1
     */
    private int read8AfterOpcode() {
        return read8(PC + 1);
    }

    /**
     * Reads from the bus the value 16 bits at the given address,
     * 
     * @param address
     * 
     * @return 16 bits value, of which the 8 MSB bits is the data stored in
     *         address, and the 8 LSB is the data stored in address + 1
     */
    private int read16(int address) {
        return Bits.make16(read8(address + 1), read8(address));
    }

    /**
     * Reads from the bus the value 16 bits at PC + 1
     * 
     * @return 16 bits value starting at PC+1
     */
    private int read16AfterOpcode() {
        return read16(PC + 1);
    }

    /**
     * Writes on the bus, at the given address, the given 8-bit value,
     * 
     * @param address
     *            integer value of 16 bits, the location at which we want to
     *            read the stored value
     * @param v
     *            an 8-bit value we want to store
     */
    private void write8(int address, int v) {
        bus.write(address, v);
    }

    /**
     * Writes on the bus, at the given address, the given 16-bit value
     * 
     * @param address
     *            integer value of 16 bits, the location at which we want to
     *            read the stored value
     * @param v
     *            a 16-bit value, data to be stored
     * 
     * @throws IllegalArgumentException
     *             if the data is not a 16-bit value
     * 
     */
    private void write16(int address, int v) {
        Preconditions.checkBits16(v);
        int LSB = Bits.clip(8, v);
        int MSB = Bits.extract(v, 8, 8);
        write8(address, LSB);
        write8(address + 1, MSB);
    }

    /**
     * Writes on the bus, at the address contained in the pair of HL registers
     * 
     * @param v
     *            an 8-value to be stored in the pair HL
     */
    private void write8AtHl(int v) {
        bus.write(reg16(Reg16.HL), v);
    }

    /**
     * Decrements the address contained in the stack pointer (SP register) by 2
     * units, then writes the given 16-bit value to this new address
     * 
     * @param v
     *            a 16-bit value
     *
     */
    private void push16(int v) {
        SP = Bits.clip(16, SP - 2);
        write16(SP, v);
    }

    /**
     * Reads from the bus and returns the 16-bit value at the address contained
     * in the stack pointer (SP register), then increments it by 2 units.
     * 
     * @return 16-bit value stored at SP
     */
    private int pop16() {
        int res = read16(SP);
        SP = Bits.clip(16, SP + 2);
        return res;
    }

    /**
     * Which retrieves the identity of an 8-bit register from the encoding of
     * the given opcode, starting at the given index bit,
     * 
     * @param opcode
     *            an {@link Opcode}, is the first byte of the instruction, that
     *            tells the processor what operations need to be performed.
     * @param startBit
     *            Integer, starting bit index
     * @return a Reg corresponding to the 3 extracted bits from the opcode
     */
    private Reg extractReg(Opcode opcode, int startBit) {
        int e = opcode.encoding;
        int r = Bits.extract(e, startBit, 3);
        if (r == 0b111)
            return Reg.A;
        return Reg.values()[r + 2];
    }

    /**
     * Which retrieves the identity of an 16-bit register from the encoding of
     * the given opcode
     * 
     * @param opcode
     *            an {@link Opcode}, is the first byte of the instruction, that
     *            tells the processor what operations need to be performed.
     * @return a Reg16 corresponding to the 2 extracted bits from the opcode
     *         starting at the 4th bit
     */
    private Reg16 extractReg16(Opcode opcode) {
        int e = opcode.encoding;
        int r = Bits.extract(e, 4, 2);
        return Reg16.values()[Bits.clip(2, r + 1)];
    }

    /**
     * Used to encode the incrementing or decrementing of the HL pair in
     * different instructions
     * 
     * @param opcode
     *            an {@link Opcode}, is the first byte of the instruction, that
     *            tells the processor what operations need to be performed.
     * @return -1 or +1 based on the index bit 4,
     */
    private int extractHlIncrement(Opcode opcode) {
        int e = opcode.encoding;
        return Bits.test(e, 4) ? -1 : 1;
    }

    /**
     * @param r
     *            a Reg16 representing the register pair
     * @return the value contained in the given register pair,
     */
    private int reg16(Reg16 r) {
        return Bits.make16(registerFile.get(Reg.values()[(r.index() * 2)]),
                registerFile.get(Reg.values()[(r.index() * 2) + 1]));
    }

    /**
     * @param r
     *            a Reg16 representing the register pair
     * @return the value contained in the given register pair, except when the
     *         pair is AF,in which case it returns the value in SP
     */
    private int reg16SP(Reg16 r) {
        return (r.index() == 0) ? SP : reg16(r);
    }

    /**
     * Modifies the value contained in the given register pair, bearing in mind
     * to set the low-order bits to 0 if the pair in question is AF
     * 
     * @param r
     *            a Reg16 representing the register pair
     * @param newV
     *            the new value that will be stored on the given pair
     */
    private void setReg16(Reg16 r, int newV) {
        {
            int LSB = Bits.clip(8, newV);
            int MSB = Bits.extract(newV, 8, 8);
            if (r.index() == 0) {
                LSB = Bits.extract(LSB, 4, 4);
                LSB = LSB << 4;
            }
            registerFile.set(Reg.values()[r.index() * 2], MSB);
            registerFile.set(Reg.values()[r.index() * 2 + 1], LSB);
        }
    }

    /**
     * Does the same thing as setReg16 except when the passed pair is AF, in
     * which case the SP register is modified instead of the AF pair
     * 
     * @param r
     *            a Reg16 representing the register pair
     * @param newV
     *            the new value to be stored in the given register pair
     */
    private void setReg16SP(Reg16 r, int newV) {
        Preconditions.checkBits16(newV);
        if (r.index() == 0) {
            SP = newV;
        } else
            setReg16(r, newV);
    }

    /**
     * which retrieves the value stored in the given pair and places it in the
     * given register
     * 
     * @param r
     *            a Reg16 representing the register pair
     * @param vf
     *            an int containing a pair of values/fanions returned by one of
     *            the {@link Alu} methods
     */
    private void setRegFromAlu(Reg r, int vf) {
        registerFile.set(r, Alu.unpackValue(vf));
    }

    /**
     * Retrieves the flags stored in the given pair and places them in the F
     * register
     * 
     * @param valueFlags
     *            flags stored in the given pair
     */
    private void setFlags(int valueFlags) {
        registerFile.set(Reg.F, Alu.unpackFlags(valueFlags));
    }

    /**
     * Does what setRegFromAlu & setFlags do
     * 
     * @param r
     *            a Reg16 representing the register pair
     * @param vf
     *            an int containing a pair of values/fanions returned by one of
     *            the Alu methods
     */
    private void setRegFlags(Reg r, int vf) {
        registerFile.set(r, Alu.unpackValue(vf));
        registerFile.set(Reg.F, Alu.unpackFlags(vf));
    }

    /**
     * Extracts the value stored in the given pair and writes it on the bus to
     * the address contained in the pair of HL registers, then extracts the
     * flags stored in the pair and places them in the register F
     * 
     * @param vf
     *            an int containing a pair of values/fanions returned by one of
     *            the Alu methods
     */
    private void write8AtHlAndSetFlags(int vf) {
        write8AtHl(Alu.unpackValue(vf));
        registerFile.set(Reg.F, Alu.unpackFlags(vf));
    }

    /**
     * @param o
     *            an {@link Opcode}, is the first byte of the instruction, that
     *            tells the processor what operations need to be performed.
     * @return an int representing a 3-bit values extracted from bit index
     *         3(included) to bit index 6(excluded)
     */
    private int extractN3(Opcode o) {
        return Bits.extract(o.encoding, 3, 3);
    }

    /**
     * @param o
     *            an {@link Opcode}, is the first byte of the instruction, that
     *            tells the processor what operations need to be performed.
     * @return {@link RotDir}, the rotation direction
     * 
     */
    private RotDir rotDir(Opcode o) {
        if (Bits.test(o.encoding, 3))
            return RotDir.RIGHT;
        else
            return RotDir.LEFT;
    }

    /**
     * @param o
     *            an {@link Opcode}, is the first byte of the instruction, that
     *            tells the processor what operations need to be performed.
     * @return a boolean representing the truth value of bit index 6
     */
    private boolean test6_Opcode(Opcode o) {
        return Bits.test(o.encoding, 6);
    }

    //Auxiliary method used to write the method combineAluFlags 
    private boolean Fatima(int i, int vf, int index) {
        switch (i) {
        case 0: {
            return false;
        }
        case 1: {
            return true;
        }

        case 2: {
            return Bits.test(vf, index);
        }

        default: {
            return Bits.test(registerFile.get(Reg.F), index);
        }

               }
    }

    /**
     * Combines the flags stored in the register F with those contained in the
     * pair value-flags, according to the last four parameters, which each
     * correspond to a flag, and stores the result in the register F.
     * 
     * @param vf
     *            an integer containing a pair of values/fanions returned by one
     *            of the {@link Alu} methods
     * @param z
     *            {@link FlagSrc}
     * @param n
     *            {@link FlagSrc}
     * @param h
     *            {@link FlagSrc}
     * @param c
     *            {@link FlagSrc}
     */
    private void combineAluFlags(int vf, FlagSrc z, FlagSrc n, FlagSrc h,
            FlagSrc c) {
        int r = Alu.maskZNHC(Fatima(z.ordinal(), vf, 7),
                Fatima(n.ordinal(), vf, 6), Fatima(h.ordinal(), vf, 5),
                Fatima(c.ordinal(), vf, 4));
        registerFile.set(Reg.F, r);
    }

    /**
     * @param o
     *            an {@link Opcode}, is the first byte of the instruction, that
     *            tells the processor what operations need to be performed.
     * @return boolean true if and only if 3th bit of opcode and fanion C are
     *         both true
     */
    private boolean getCarryADD(Opcode o) {
        if (Bits.test(o.encoding, 3) && Bits.test(registerFile.get(Reg.F), 4))
            return true;
        else
            return false;
    }

    /**
     * Extracts the condition from an opcode according to the corresponding
     * flags either Z or C
     * 
     * @param o
     *            an {@link Opcode}, is the first byte of the instruction, that
     *            tells the processor what operations need to be performed.
     * @return boolean, the condition
     */
    private boolean condition(Opcode o) {
        int cc = Bits.extract(o.encoding, 3, 2);
        switch (cc) {
        case 0:
            return !Bits.test(registerFile.get(Reg.F), 7);

        case 1:
            return Bits.test(registerFile.get(Reg.F), 7);

        case 2:
            return !Bits.test(registerFile.get(Reg.F), 4);

        default:
            return Bits.test(registerFile.get(Reg.F), 4);
        }
    }

   
    /**
     * @return boolean true if and only if both IE and IF are true
     */
    private boolean testIE_IF() {
        int x = IE & IF;
        return x != 0;
    }

    /**
     * @return boolean true if and only if both IE and IF are true
     */
    private int indexInterrupt() {
        int x = IE & IF;
        int y = Integer.lowestOneBit(x);
        return (32 - Integer.numberOfLeadingZeros(y) - 1);
    }

}
