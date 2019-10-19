package ch.epfl.gameboj;

import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;

/**
 * CS-108 
 * RegisterFile.java 
 * Purpose: A generic class, representing a file of 8
 * bit {@link Register}s
 * 
 * @author Sara Djambazovska
 * @author Marouane Jaakik
 *
 * 
 * @param <E>
 *            Type of the {@link Register}s stored in the Register file
 */
public final class RegisterFile<E extends Register> {

    private final byte[] registers;

    /**
     * Public constructor that creates a Register file of 8 bit registers, where
     * the number of registers is given by the size of the array passed as an
     * argument
     * 
     * @param allRegs
     *            Array of {@link Register}s, giving the number of registers
     *            stored in the file
     */
    public RegisterFile(E[] allRegs) {
        registers = new byte[allRegs.length];
    }

    /**
     * Returns an integer of 8 bits, representing the value stored in the
     * {@link Register} passed as an argument
     * 
     * @param reg
     *            {@link Register} whose value we want to retain
     * @return integer value of 8 bits stored in the {@link Register} passed as
     *         an argument
     */
    public int get(E reg) {
        return Byte.toUnsignedInt(registers[reg.index()]);
    }

    /**
     * Sets the value in the given {@link Register} as the 8 bit value passed as
     * a second argument
     * 
     * @param reg
     *            {@link Register} where we want to set the data
     * @param newValue
     *            integer of 8 bits, the data to be set in the {@link Register}
     * @throws IllegalArgumentException
     *             if the value to be set is not 8 bits
     */
    public void set(E reg, int newValue) {
        Preconditions.checkBits8(newValue);
        registers[reg.index()] = (byte) newValue;
    }

    /**
     * Returns true iff the {@link Bit} in the given {@link Register} is 1
     * 
     * @param reg
     *            {@link Register} whose bit is tested
     * @param b
     *            {@link Bit}, the bit to be tested
     * @return boolean value, true iff the given bit in the register is 1
     */
    public boolean testBit(E reg, Bit b) {
        return Bits.test(Byte.toUnsignedInt(registers[reg.index()]), b);
    }

    /**
     * Sets the {@link Bit} in the {@link Register} to the new value passed as a
     * second argument as a boolean, meaning the bit is set to 1 iff true
     * 
     * @param reg
     *            {@link Register} whose bit is being set
     * @param bit
     *            {@link Bit} in the {@link Register} that is set to the new
     *            value
     * @param newValue
     *            boolean value representing 1 if true, 0 if false, to be set to
     *            the given {@link Bit}
     */
    public void setBit(E reg, Bit bit, boolean newValue) {
        registers[reg.index()] = (byte) Bits.set(registers[reg.index()],
                bit.index(), newValue);
    }
}
