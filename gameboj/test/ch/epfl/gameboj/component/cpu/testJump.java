package ch.epfl.gameboj.component.cpu;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;

import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.RandomGenerator;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

class testJump {

	private Bus connect(Cpu cpu, Ram ram) {
		RamController rc = new RamController(ram, 0);
		Bus b = new Bus();
		cpu.attachTo(b);
		rc.attachTo(b);
		return b;
	}

	private void cycleCpu(Cpu cpu, long cycles) {
		for (long c = 0; c < cycles; ++c)
			cpu.cycle(c);
	}

	@Test
	void absolutJumpTest () {
		for (int i = 0; i < RandomGenerator.RANDOM_ITERATIONS; ++i) {
			Cpu cpu = new Cpu();
			Ram ram = new Ram(0xFFFF);
			Bus bus = connect(cpu, ram);
			int value = RandomGenerator.randomBit(8);
			int addressToJumpTo = RandomGenerator.randomBit(16);
			System.err.println(addressToJumpTo);
			bus.write(addressToJumpTo, Opcode.LD_A_N8.encoding);
			bus.write(Bits.clip(16, addressToJumpTo + 1), value);//if addres is 0xffff +1 =0 ?
			bus.write(0, Opcode.JP_N16.encoding);
			bus.write(1, Bits.clip(8, addressToJumpTo));
			bus.write(2, Bits.extract(addressToJumpTo, 8, 8));
			int pc = Bits.clip(16, addressToJumpTo + Opcode.LD_A_N8.totalBytes); 
			int cycles = Opcode.LD_A_N8.cycles + Opcode.JP_N16.cycles;
			cycleCpu(cpu, cycles);
			assertArrayEquals (new int [] {pc, 0, value, 0, 0, 0, 0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
		}
	}
	@Test
    void absJumpTest () {
       
            Cpu cpu = new Cpu();
            Ram ram = new Ram(0xFFFf);
            Bus bus = connect(cpu, ram);
            int value = 50;
            int addressToJumpTo = 0xfffd;
           // System.err.println(addressToJumpTo);
            bus.write(addressToJumpTo, Opcode.LD_A_N8.encoding);
            bus.write(Bits.clip(16, addressToJumpTo + 1), value);//if addres is 0xffff +1 =0 ?
           
            bus.write(0, Opcode.JP_N16.encoding);
            bus.write(1, Bits.clip(8, addressToJumpTo));
            bus.write(2, Bits.extract(addressToJumpTo, 8, 8));
            
            int pc = Bits.clip(16, addressToJumpTo + Opcode.LD_A_N8.totalBytes); 
            int cycles = Opcode.LD_A_N8.cycles + Opcode.JP_N16.cycles;
            cycleCpu(cpu, cycles);
            assertArrayEquals (new int [] {pc, 0, value, 0, 0, 0, 0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
        
    }

	@Test
	void absolutJumpTestHL () {
		for (int i = 0; i < RandomGenerator.RANDOM_ITERATIONS; ++i) {
			Cpu cpu = new Cpu();
			Ram ram = new Ram(0xFFFF);
			Bus bus = connect(cpu, ram);
			int value = RandomGenerator.randomBit(8);
			int addressToJumpTo = RandomGenerator.randomBit(16);
			bus.write(addressToJumpTo, Opcode.LD_A_N8.encoding);
			bus.write(Bits.clip(16, addressToJumpTo + 1), value);
			bus.write(0, Opcode.LD_HL_N16.encoding);
			bus.write(1, Bits.clip(8, addressToJumpTo));
			bus.write(2, Bits.extract(addressToJumpTo, 8, 8));
			bus.write(3, Opcode.JP_HL.encoding);
			int pc = Bits.clip(16, addressToJumpTo + Opcode.LD_A_N8.totalBytes); 
			int cycles = Opcode.LD_A_N8.cycles + Opcode.JP_HL.cycles + Opcode.LD_HL_N16.cycles;
			cycleCpu(cpu, cycles);
			assertArrayEquals (new int [] {pc, 0, value, 0, 0, 0, 0, 0,Bits.extract(addressToJumpTo, 8, 8),  Bits.clip(8, addressToJumpTo) }, cpu._testGetPcSpAFBCDEHL());
		}
	}
	 @Disabled
	@Test
	void relatifJumpTest () {
		for (int i = 0; i < RandomGenerator.RANDOM_ITERATIONS; ++i) {
			Cpu cpu = new Cpu();
			Ram ram = new Ram(0xFFFF);
			Bus bus = connect(cpu, ram);
			int value = RandomGenerator.randomBit(8);
			int jumpIndex = RandomGenerator.randomBit(8);
			bus.write(Bits.clip(16, 2 + Bits.signExtend8(jumpIndex)), Opcode.LD_A_N8.encoding);
			bus.write(Bits.clip(16, 3 + Bits.signExtend8(jumpIndex)), value);
			bus.write(0, Opcode.JR_E8.encoding);
			bus.write(1, jumpIndex);
			int pc = Bits.clip(16, 2 + Bits.signExtend8(jumpIndex) + Opcode.LD_A_N8.totalBytes); 
			int cycles = Opcode.LD_A_N8.cycles + Opcode.JR_E8.cycles;
			cycleCpu(cpu, cycles);
			assertArrayEquals (new int [] {pc, 0, value, 0, 0, 0, 0, 0, 0, 0 }, cpu._testGetPcSpAFBCDEHL());
		}
	}


}