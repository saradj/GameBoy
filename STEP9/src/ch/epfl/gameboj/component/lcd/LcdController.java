package ch.epfl.gameboj.component.lcd;

import java.util.ArrayList;

import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.component.lcd.LcdImage.Builder;
import ch.epfl.gameboj.component.memory.Ram;
import ch.epfl.gameboj.component.memory.RamController;

public final class LcdController implements Component, Clocked {

	private RamController videoRam = new RamController(new Ram(AddressMap.VIDEO_RAM_SIZE), 0x8000, 0xA000);
	private static final int LCD_WIDTH = 160, LCD_HEIGHT = 144,
			LCDCAdress = AddressMap.REGS_LCDC_START + Reg.LCDC.index();
	private long nextNonIdleCycle = 0, lcdOnCycle = 0;
	private Cpu cpu;
	private int currentLineCycle, currentLine;
	private LcdImage image;
	private Bus bus;

	private enum Modes implements Bit {
		MODE_0, MODE_1, MODE_2, MODE_3
	}

	private enum Reg implements Register {
		LCDC, STAT, SCY, SCX, LY, LYC, DMA, BGP, OBP0, OBP1, WY, WX
	}

	private enum BitsLCDC implements Bit {
		BG, OBJ, OBJ_SIZE, BG_AREA, TILE_SOURCE, WIN, WIN_AREA, LCD_STATUS
	}

	private enum BitsSTAT implements Bit {
		MODE0, MODE1, LYC_EQ_LY, INT_MODE0, INT_MODE1, INT_MODE2, INT_LYC
	}

	private RegisterFile<Reg> registerFile = new RegisterFile<>(Reg.values());

	public LcdController(Cpu cpu) {
		this.cpu = cpu;
		image = new LcdImage.Builder(LCD_HEIGHT, LCD_WIDTH).build();

	}

	public LcdImage currentImage() {
		return image;
	}

	@Override
	public void attachTo(Bus bus) {
		this.bus = bus;
		bus.attach(this);
	}

	@Override
	public void cycle(long cycle) {
		if (nextNonIdleCycle == Long.MAX_VALUE && Bits.test(registerFile.get(Reg.LCDC), BitsLCDC.LCD_STATUS)) {
			registerFile.setBit(Reg.STAT, BitsSTAT.INT_MODE0, false);
			registerFile.setBit(Reg.STAT, BitsSTAT.INT_MODE1, true);
			registerFile.setBit(Reg.LCDC, BitsLCDC.LCD_STATUS, true);
			nextNonIdleCycle = cycle;
			lcdOnCycle = cycle;
			reallyCycle(cycle-lcdOnCycle);
		}
		if (nextNonIdleCycle == cycle)
			reallyCycle(cycle);
		else
			return;
	}

	public void reallyCycle(long cycle) {
		currentLine= (int) ((cycle) / 114) % 154;
		//currentCycleLine = (int) (cycle % 154*114);
		Modes mode = Mode(cycle);
		switch (mode) {
		case MODE_0: {
			nextNonIdleCycle += 51;
			setMode(Modes.MODE_0);
			if (Bits.test(registerFile.get(Reg.STAT), BitsSTAT.INT_MODE0))
				cpu.requestInterrupt(Interrupt.LCD_STAT);

		}

		case MODE_1: {
			registerFile.set(Reg.LY, currentLine);
			nextNonIdleCycle += 114;
			setMode(Modes.MODE_1);
			cpu.requestInterrupt(Interrupt.VBLANK);
			if (Bits.test(registerFile.get(Reg.STAT), BitsSTAT.INT_MODE1))
				cpu.requestInterrupt(Interrupt.LCD_STAT);
		}

		case MODE_2: {
			nextNonIdleCycle += 20;
			registerFile.set(Reg.LY, currentLine);
			setMode(Modes.MODE_2);
			if (Bits.test(registerFile.get(Reg.STAT), BitsSTAT.INT_MODE2))
				cpu.requestInterrupt(Interrupt.LCD_STAT);

		}

		case MODE_3: {
			nextNonIdleCycle += 43;
			setMode(Modes.MODE_3);

		}
		}
	}

	@Override
	public int read(int address) {
		Preconditions.checkBits16(address);
		if (address >= AddressMap.VIDEO_RAM_START && address < AddressMap.VIDEO_RAM_END)
			return videoRam.read(address);
		if (address >= AddressMap.REGS_LCDC_START && address < AddressMap.REGS_LCDC_END)
			return registerFile.get(Reg.values()[address - AddressMap.REGS_LCDC_START]);
		return NO_DATA;
	}

	@Override
	public void write(int adress, int data) {
		Preconditions.checkBits16(adress);
		Preconditions.checkBits8(data);
		if (adress >= AddressMap.REGS_LCDC_START && adress < AddressMap.REGS_LCDC_END) {

			if (adress == AddressMap.REGS_LCDC_START + Reg.LCDC.index()) {
				registerFile.set(Reg.LCDC, data);
				// registerFile.setBit(Reg.STAT, BitsSTAT.MODE0, false);
				// registerFile.setBit(Reg.STAT, BitsSTAT.MODE1, false);
				if (Bits.test(registerFile.get(Reg.LCDC), BitsLCDC.LCD_STATUS)) {
					registerFile.set(Reg.LY, 0);
					ly_equ_lyc();
				}
				nextNonIdleCycle = Long.MAX_VALUE;
			} else if (adress == AddressMap.REGS_LCDC_START + Reg.STAT.index()) {
				data = data & 0xF8;
				registerFile.set(Reg.STAT, Bits.clip(3, registerFile.get(Reg.STAT)) | data);
				// ly_equ_lyc();
			} else if (adress == AddressMap.REGS_LCDC_START + Reg.LYC.index()) {
				registerFile.set(Reg.LYC, data);
				ly_equ_lyc();
			}

		} else if (adress >= AddressMap.VIDEO_RAM_START && adress < AddressMap.VIDEO_RAM_END)
			videoRam.write(adress, data);
	}

	private void ly_equ_lyc() {
		boolean value = (registerFile.get(Reg.LY) == registerFile.get(Reg.LYC)) ? true : false;
		registerFile.setBit(Reg.STAT, BitsSTAT.LYC_EQ_LY, value);
		if (value)
			cpu.requestInterrupt(Interrupt.LCD_STAT);
	}

	private Modes Mode(long cycle) {
		int currentLine = (int) (cycle / 114) % 154;
		int currentCycleLine = (int) (cycle % 154*114)%114;
		if (currentLine >= 144)
			return Modes.MODE_1;
		else if (0 <= currentCycleLine && currentCycleLine < 20)
			return Modes.MODE_2;
		else if (19 < currentLineCycle && currentLineCycle < 63)
			return Modes.MODE_3;
		else
			return Modes.MODE_0;

	}

	private void setMode(Modes mode) {
		switch (mode) {
		case MODE_0: {
			registerFile.setBit(Reg.STAT, BitsSTAT.MODE0, false);
			registerFile.setBit(Reg.STAT, BitsSTAT.MODE1, false);

		}
		case MODE_1: {
			registerFile.setBit(Reg.STAT, BitsSTAT.MODE0, true);
			registerFile.setBit(Reg.STAT, BitsSTAT.MODE1, false);
		}
		case MODE_2: {
			registerFile.setBit(Reg.STAT, BitsSTAT.MODE0, false);
			registerFile.setBit(Reg.STAT, BitsSTAT.MODE1, true);
		}
		case MODE_3: {
			registerFile.setBit(Reg.STAT, BitsSTAT.MODE0, true);
			registerFile.setBit(Reg.STAT, BitsSTAT.MODE1, true);

		}
		}

	}
}
