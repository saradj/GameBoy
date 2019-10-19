package ch.epfl.gameboj.component;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

/**
 * CS-108 Joypad.java 
 * Purpose: Representing the Joypad of the {@link GameBoy}
 * 
 * @author Sara Djambazovska
 * @author Marouane Jaakik
 *
 */
public final class Joypad implements Component {

    private final Cpu cpu;
    private final boolean[] select = new boolean[2];
    private final int[] line = new int[2];
    private final static int NUMBER_OF_STATES = 4;
    private final static int BIT_SELECT0 = 4;
    private final static int BIT_SELECT1 = 5;

    /**
     * Enumeration representing the Keys on the {@link Joypad} of the
     * {@link GameBoy}
     *
     */
    public static enum Key {
        RIGHT, LEFT, UP, DOWN, A, B, SELECT, START
    }

    public Joypad(Cpu cpu) {
        this.cpu = cpu;
    }

    @Override
    public int read(int address) {
        return address == AddressMap.REG_P1 ? Bits.complement8(calculateP1())
                : NO_DATA;
    }

    @Override
    public void write(int adress, int data) {
        if (adress == AddressMap.REG_P1) {
            int p1Before = calculateP1();
            select[0] = Bits.test(Bits.complement8(data), 4);
            select[1] = Bits.test(Bits.complement8(data), 5);
            requestJoypadInterruption(p1Before, calculateP1());
        }
    }

    /**
     * Simulates the pressing of the Key on the {@link Joypad}
     * 
     * @param key
     *            {@link Key}: the Key pressed
     */
    public void keyPressed(Key key) {
        int state = key.ordinal() % (Key.values().length / line.length);
        int lineIndex = key.ordinal() / (Key.values().length / line.length);
        int p1Before = calculateP1();
        line[lineIndex] = Bits.set(line[lineIndex], state, true);
        requestJoypadInterruption(p1Before, calculateP1());
    }

    /**
     * Simulates the releasing of the Key on the {@link Joypad}
     * 
     * @param key
     *            {@link Key}: the Key released
     */
    public void keyReleased(Key key) {
        int state = key.ordinal() % (Key.values().length / line.length);
        int lineIndex = key.ordinal() / (Key.values().length / line.length);
        line[lineIndex] = Bits.set(line[lineIndex], state, false);
    }

    private int calculateP1() {
        int newP1 = 0;
        newP1 = select[0] ? Bits.set(newP1, BIT_SELECT0, true) | line[0] : newP1;
        newP1 = select[1] ? Bits.set(newP1, BIT_SELECT1, true) | line[1] : newP1;
        return newP1;
    }

    private void requestJoypadInterruption(int p1Before, int p1After) {
        int oldActiveStates = Bits.clip(NUMBER_OF_STATES, p1Before);
        int newActiveStates = Bits.clip(NUMBER_OF_STATES, p1After);
        if (((oldActiveStates ^ newActiveStates)
                & (Bits.complement8(oldActiveStates) & 0xF)) != 0)
            cpu.requestInterrupt(Interrupt.JOYPAD);
    }
}
