package ch.epfl.gameboj.component;

/**
 * CS-108 
 * Clocked.java 
 * Purpose: Representing a {@link Component} driven by the clock of the system
 * 
 * @author Sara Djambazovska
 * @author Marouane Jaakik
 *
 */
public interface Clocked {
    /**
     * Makes the {@link Component} evolve by executing all of the operations
     * during the cycle of the index given as an argument
     * 
     * @param cycle
     *            long value giving the index during which the
     *            {@link Component} should execute all of the operations
     */
    public abstract void cycle(long cycle);
}
