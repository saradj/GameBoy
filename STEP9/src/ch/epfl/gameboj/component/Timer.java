package ch.epfl.gameboj.component;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;

/**
 * CS-108 
 * Timer.java
 * Purpose: Representing the timer of the {@link GameBoy}, a
 * {@link Component} connected to the {@link Bus} and driven by the Clock
 * 
 * @author Sara Djambazovska
 * @author Marouane Jaakik
 *
 */
public final class Timer implements Component, Clocked {

    private Cpu cpu;
    private int TIMA, timer, TAC, TMA;

    /**
     * Public constructor that constructs the {@link Timer} associated to the
     * processor {@link Cpu} given as an argument
     * 
     * @param cpu
     *            {@link Cpu} the processor of the {@link GameBoy} that is
     *            associated to the {@link Timer}
     * @throws NullPointerException
     *             if the given argument {@link Cpu} processor is null
     */
    public Timer(Cpu cpu) {
        Objects.requireNonNull(cpu);
        this.cpu = cpu;
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Clocked#cycle(long)
     */
    @Override
    public void cycle(long cycle) {
        boolean s0 = state();
        timer = Bits.clip(16, timer + 4);
        incIfChange(s0);

    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#read(int)
     */
    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        switch (address) {
        case AddressMap.REG_TAC:
            return TAC;
        case AddressMap.REG_TMA:
            return TMA;
        case AddressMap.REG_TIMA:
            return TIMA;
        case AddressMap.REG_DIV:
            return Bits.extract(timer, 8, 8);
        default:
            return NO_DATA;
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see ch.epfl.gameboj.component.Component#write(int, int)
     */
    @Override
    public void write(int address, int data) {
        Preconditions.checkBits16(address);
        Preconditions.checkBits8(data);
        boolean s0 = state();
        switch (address) {
        case AddressMap.REG_TAC:
            TAC = data;
            incIfChange(s0);
            break;
        case AddressMap.REG_TMA:
            TMA = data;
            break;
        case AddressMap.REG_TIMA:
            TIMA = data;
            break;
        case AddressMap.REG_DIV:
            timer = 0;
            incIfChange(s0);
            break;
        }
    }

    private boolean state() {
        int i = Bits.extract(TAC, 0, 2);
        switch (i) {
        case 0:
            return Bits.test(TAC, 2) && Bits.test(timer, 9);
        case 1:
            return Bits.test(TAC, 2) && Bits.test(timer, 3);
        case 2:
            return Bits.test(TAC, 2) && Bits.test(timer, 5);
        default:
            return Bits.test(TAC, 2) && Bits.test(timer, 7);
        }
    }

    private void incIfChange(boolean before) {
        if (before && !state()) {
            if (TIMA == 0xff) {
                cpu.requestInterrupt(Interrupt.TIMER);
                TIMA = TMA;
            } else
                TIMA++;
        }
    }
}
