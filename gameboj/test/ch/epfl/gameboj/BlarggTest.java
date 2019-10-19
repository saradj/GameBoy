package ch.epfl.gameboj;

import java.io.File;
import java.io.IOException;

import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.DebugPrintComponent;
import ch.epfl.gameboj.component.cartridge.Cartridge;

public final class BlarggTest {
	public static void main(String[] args) throws IOException   {
		run("01-special.gb");
		run("02-interrupts.gb");
		run("03-op sp,hl.gb");
		run("04-op r,imm.gb");
		run("05-op rp.gb");
		run("06-ld r,r.gb");
		
		run("07-jr,jp,call,ret,rst.gb");
		run("08-misc instrs.gb");
		run("09-op r,r.gb");
		run("10-bit ops.gb");
		run("11-op a,(hl).gb");
		run("instr_timing.gb");
		
    }
	 
  public static void run(String s) throws IOException{
	  
	  	File romFile = new File(s);
	    final long cycles = 30000000;

	    GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
	    Component printer = new DebugPrintComponent();
	    printer.attachTo(gb.bus());
	    while (gb.cycles() < cycles) {
	      long nextCycles = Math.min(gb.cycles() + 17556, cycles);
	      gb.runUntil(nextCycles);
	      gb.cpu().requestInterrupt(Cpu.Interrupt.VBLANK);
	    }  
  }
}