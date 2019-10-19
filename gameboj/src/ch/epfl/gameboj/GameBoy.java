package ch.epfl.gameboj;

import java.util.Objects;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.Joypad;
import ch.epfl.gameboj.component.Timer;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.memory.BootRomController;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

/**
 * CS-108 GameBoy.java Purpose: Representing the GameBoy, instantiating
 * different {@link Component}s and attaching them to a common bus
 * 
 * @author Sara Djambazovska
 * @author Marouane Jaakik
 *
 */
public final class GameBoy {

    /**
     * 
     */
    public static final long CYCLES_PER_SECOND= (long)Math.pow(2, 20);
    public static final double CYCLES_PER_NANOSECOND = CYCLES_PER_SECOND/1000000000.0;
    private final Bus bus;
    private long cycles = 0;
    private final Ram workRam = new Ram(AddressMap.WORK_RAM_SIZE);
    private final Cpu cpu;
    private final RamController workRamController;
    private final RamController echoRamController;
    private final BootRomController bootRomController;
    private final Timer timer;
    private final LcdController lcdController;
    private final Joypad joypad;

    /**
     * Public constructor that constructs the {@link GameBoy} by creating all of
     * the necessary components and attaching them to the bus
     * 
     * @param cartridge
     *            {@link Cartridge}
     * @throws NullPointerException
     *             if the argument cartridge is null
     */
    public GameBoy(Cartridge cartridge) {
        Objects.requireNonNull(cartridge);
        bus = new Bus();
        bootRomController = new BootRomController(cartridge);
        workRamController = new RamController(workRam,
                AddressMap.WORK_RAM_START);
        echoRamController = new RamController(workRam,
                AddressMap.ECHO_RAM_START, AddressMap.ECHO_RAM_END);
        cpu = new Cpu();
        lcdController = new LcdController(cpu);
        joypad= new Joypad(cpu);
        timer = new Timer(cpu);
        bus.attach(workRamController);
        bus.attach(timer);
        bus.attach(echoRamController);
        cpu.attachTo(bus);
        bus.attach(bootRomController);
        lcdController.attachTo(bus);
        joypad.attachTo(bus);
    }

    /**
     * Returns the bus that connects the components of the GameBoy
     * 
     * @return {@link Bus} the bus that connects the components of the GameBoy
     */
    public Bus bus() {
        return bus;
    }

    /**
     * Returns the processor of the {@link GameBoy}
     * 
     * @return {@link Cpu} the processor of the {@link GameBoy}
     */
    public Cpu cpu() {
        return cpu;
    }
    
    /**
     * Returns the joypad of the {@link GameBoy}
     * 
     * @return {@link Joypad} the joypad of the {@link GameBoy}
     */
    public Joypad joypad() {
        return joypad;
    }

    /**
     * Returns the timer of the {@link GameBoy}, gives access to this object
     * 
     * @return {@link Timer} the timer of the {@link GameBoy}
     */
    public Timer timer() {
        return timer;
    }

    /**
     * Returns the LCD controller of the {@link GameBoy}
     * 
     * @return {@link LcdController} the processor of the {@link GameBoy}
     */
    public LcdController lcdController() {
        return lcdController;
    }

    /**
     * Simulates the running of the {@link GameBoy} until the given cycle minus
     * one, by calling the method cycle on all of it's {@link Component}s that
     * are {@link Clocked} or throws {@link IllegalArgumentException} if more
     * cycles have been simulated than the given argument cycle
     * 
     * @param cycle
     *            long value, allowing to move forward the simulation of the
     *            {@link GameBoy}
     * @throws IllegalArgumentException
     *             if more cycles have been simulated than the given argument
     *             cycle
     */
    public void runUntil(long cycle) {
       Preconditions.checkArgument(cycles <= cycle);
        while (cycles < cycle) {
            timer.cycle(cycles);
            lcdController.cycle(cycles);
            cpu.cycle(cycles);
            cycles++;
        }
    }

    /**
     * Returns the number of cycles simulated so far
     * 
     * @return long value, the number of cycles simulated
     */
    public long cycles() {
        return cycles;
    }

}
