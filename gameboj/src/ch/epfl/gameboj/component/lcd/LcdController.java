package ch.epfl.gameboj.component.lcd;

import java.util.Arrays;
import java.util.Objects;
import ch.epfl.gameboj.AddressMap;
import ch.epfl.gameboj.Bus;
import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.Register;
import ch.epfl.gameboj.RegisterFile;
import ch.epfl.gameboj.bits.Bit;
import ch.epfl.gameboj.bits.Bits;
import ch.epfl.gameboj.component.Clocked;
import ch.epfl.gameboj.component.Component;
import ch.epfl.gameboj.component.cpu.Cpu;
import ch.epfl.gameboj.component.cpu.Cpu.Interrupt;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.memory.Ram;

/**
 * CS-108-GameBoy
 * LcdController.java
 * Purpose:Simulates the {@link GameBoy} LCD
 * screen attached to the bus and driven by the clock
 * 
 * @author Sara Djambazovska
 * @author Marouane Jaakik
 *
 */

public final class LcdController implements Component, Clocked {

    public static final int LCD_WIDTH = 160;
    public static final int LCD_HEIGHT = 144;
    private static final int DRAW_IMAGE_CYCLES = 17556;
    private static final int DRAW_LINE_CYCLES = 114;
    private static final int LY_MAX_VALUE = 154;
    private static final int MODE_0_CYCLES = 51;
    private static final int MODE_2_CYCLES = 20;
    private static final int MODE_3_CYCLES = 43;
    private static final int LINES_IN_TILE = 8;
    private static final int TILES_IN_BGLINE = 32;
    private static final int TOTAL_SPRITES = 40;
    private static final int MAX_SPRITES_LINE = 10;
    private static final int BG_LINES = 32;
    private static final int FG_LINES = 20;
    private static final int WX_OFFSET = 7;
    private static final int X_OFFSET = 8;
    private static final int Y_OFFSET = 16;
    private static final int BYTES_IN_SPRITE_ATTRIBUTE = 4;
    private final Ram OAM, videoRam;
    private Bus bus;
    private long nextNonIdleCycle, lcdOnCycle = 0;
    private final Cpu cpu;
    private int winY;
    private int adressSource, adressDestination;
    private LcdImage image;
    private final RegisterFile<Reg> reg = new RegisterFile<>(Reg.values());
    private LcdImage.Builder nextImageBuilder;

    /**
     * Enumeration of the modes that represents the 4 states of the LCD
     * Controller
     */
    private enum Modes implements Bit {
        MODE_0, MODE_1, MODE_2, MODE_3
    }

    /**
     * Enumeration of various registers, that are exposed via the bus and are
     * used to configure the LCD controller
     */
    private enum Reg implements Register {
        LCDC, STAT, SCY, SCX, LY, LYC, DMA, BGP, OBP0, OBP1, WY, WX
    }

    /**
     * Enumeration of 8 Bits of the LCDC register
     */
    private enum BitsLCDC implements Bit {
        BG, OBJ, OBJ_SIZE, BG_AREA, TILE_SOURCE, WIN, WIN_AREA, LCD_STATUS
    }

    /**
     * Enumeration of 8 Bits of the STAT register
     */
    private enum BitsSTAT implements Bit {
        MODE0, MODE1, LYC_EQ_LY, INT_MODE0, INT_MODE1, INT_MODE2, INT_LYC
    }

    /**
     * Public constructor that sets up the {@link LcdController} by creating the
     * processor,videoRan, OAMram, and a blank image.
     * 
     * @param cpu
     *            the processor {@link Cpu}
     * @throws NullPointerException
     *             if the {@link Cpu} is null
     */
    public LcdController(Cpu cpu) {
        Objects.requireNonNull(cpu);
        this.cpu = cpu;
        this.image = new LcdImage.Builder(LCD_HEIGHT, LCD_WIDTH).build();
        this.nextImageBuilder = new LcdImage.Builder(LCD_HEIGHT, LCD_WIDTH);
        nextNonIdleCycle = Long.MAX_VALUE;
        videoRam = new Ram(AddressMap.VIDEO_RAM_SIZE);
        OAM = new Ram(AddressMap.OAM_RAM_SIZE);
        adressSource = Integer.MAX_VALUE;
    }

    /**
     * The current Image is the latest image that was drawn by the
     * LcdController.
     * 
     * @return {@link LcdImage}: the current image
     */
    public LcdImage currentImage() {
        return image;
    }

    @Override
    public void attachTo(Bus bus) {
        this.bus = bus;
        bus.attach(this);
    }

    @Override
    public int read(int address) {
        Preconditions.checkBits16(address);
        if (address >= AddressMap.VIDEO_RAM_START
                && address < AddressMap.VIDEO_RAM_END)
            return videoRam.read(address - AddressMap.VIDEO_RAM_START);
        if (address >= AddressMap.REGS_LCDC_START
                && address < AddressMap.REGS_LCDC_END)
            return reg.get(Reg.values()[address - AddressMap.REGS_LCDC_START]);
        if (address >= AddressMap.OAM_START && address < AddressMap.OAM_END) {
            return OAM.read(address - AddressMap.OAM_START);
        }
        return NO_DATA;
    }

    @Override
    public void write(int address, int data) {
        Preconditions.checkBits8(data);

        if (address >= AddressMap.REGS_LCDC_START
                && address < AddressMap.REGS_LCDC_END) {
            Reg r = getReg(address);
            switch (r) {
            case LY:
                break;
            case STAT:
                data = data & 0xF8;
                reg.set(Reg.STAT, Bits.clip(3, reg.get(Reg.STAT)) | data);
                break;
            case LCDC:
                reg.set(Reg.LCDC, data);
                if (!screenOn()) {
                    setMode(Modes.MODE_0);
                    writeLY_LYC(Reg.LY, 0);
                    nextNonIdleCycle = Long.MAX_VALUE;
                }
                break;
            case LYC:
                writeLY_LYC(Reg.LYC, data);
                break;
            case DMA:
                reg.set(Reg.DMA, data);
                adressSource = Bits.make16(data, 0);
                adressDestination = 0;
                break;
            default:
                reg.set(r, data);
            }

        } else if (address >= AddressMap.VIDEO_RAM_START
                && address < AddressMap.VIDEO_RAM_END) {
            videoRam.write(address - AddressMap.VIDEO_RAM_START, data);
        } else if (address >= AddressMap.OAM_START
                && address < AddressMap.OAM_END) {
            OAM.write(address - AddressMap.OAM_START, data);
        }
    }

    @Override
    public void cycle(long cycle) {

        if (adressSource != Integer.MAX_VALUE
                && adressDestination < AddressMap.OAM_RAM_SIZE) {
            OAM.write(adressDestination, bus.read(adressSource));
            adressDestination++;
            adressSource++;
        }

        if (nextNonIdleCycle == Long.MAX_VALUE && screenOn()) {
            setMode(Modes.MODE_2);
            nextNonIdleCycle = cycle;
            lcdOnCycle = cycle;
        }

        if (nextNonIdleCycle == cycle && screenOn())
            reallyCycle();
    }

    /**
     * Switches the LcdController Modes and draws every image Line by Line, it
     * raises VBLANK and LCDSTAT interruptions when necessary.
     * 
     */
    public void reallyCycle() {

        long numberOfCyclesInImage = (nextNonIdleCycle - lcdOnCycle)
                % DRAW_IMAGE_CYCLES;
        int currentLine = (int) (numberOfCyclesInImage / DRAW_LINE_CYCLES);
        assert 0 <= currentLine && currentLine < LY_MAX_VALUE;
        int currentCycleLine = (int) (numberOfCyclesInImage) % DRAW_LINE_CYCLES;
        //transition trough modes
        if (currentLine >= 0 && currentLine < LCD_HEIGHT) {
            //mode 2 
            if (0 <= currentCycleLine && currentCycleLine < MODE_2_CYCLES) {
                setMode(Modes.MODE_2);
                lcdStatInterrupt(Modes.MODE_2);
                nextNonIdleCycle += MODE_2_CYCLES;
                writeLY_LYC(Reg.LY, currentLine);

                if (currentLine == 0) {
                    nextImageBuilder = new LcdImage.Builder(LCD_HEIGHT,
                            LCD_WIDTH);
                    winY = 0;
                }
            }
            //mode 3
            else if (MODE_2_CYCLES <= currentCycleLine
                    && currentCycleLine < MODE_2_CYCLES + MODE_3_CYCLES) {
                setMode(Modes.MODE_3);
                nextNonIdleCycle += MODE_3_CYCLES;
                nextImageBuilder.setLine(computeLine(currentLine), currentLine);

            }
            //mode 0
            else {
                setMode(Modes.MODE_0);
                lcdStatInterrupt(Modes.MODE_0);
                nextNonIdleCycle += MODE_0_CYCLES;
            }
        } 
        //mode 1
        else {
            setMode(Modes.MODE_1);
            lcdStatInterrupt(Modes.MODE_1);
            nextNonIdleCycle += DRAW_LINE_CYCLES;
            writeLY_LYC(Reg.LY, currentLine);
            if (currentLine == LCD_HEIGHT) {
                cpu.requestInterrupt(Interrupt.VBLANK);
                image = nextImageBuilder.build();
            }
        }
    }

    /**
     * Writes the given value in LY or LYC
     * 
     * @param r
     *            the given register that can be either LY or LYC
     * @param v
     *            8 bit value
     */
    private void writeLY_LYC(Reg r, int v) {
        Preconditions.checkBits8(v);
        Preconditions.checkArgument(r == Reg.LY || r == Reg.LYC);
        reg.set(r, v);
        boolean equals = (reg.get(Reg.LY) == reg.get(Reg.LYC));
        reg.setBit(Reg.STAT, BitsSTAT.LYC_EQ_LY, equals);
        if (equals && reg.testBit(Reg.STAT, BitsSTAT.INT_LYC))
            cpu.requestInterrupt(Interrupt.LCD_STAT);
    }

    /**
     * Setting to 2 lower bits of the register STAT to match the given mode
     * 
     * @param mode
     *            the {@link Modes} we want to set
     */
    private void setMode(Modes mode) {
        reg.setBit(Reg.STAT, BitsSTAT.MODE0, Bits.test(mode.ordinal(), 0));
        reg.setBit(Reg.STAT, BitsSTAT.MODE1, Bits.test(mode.ordinal(), 1));
    }

    /**
     * Computes the {@link LcdImageLine} of the given index which contains one sprite of the
     * given index
     * 
     * @param spriteIndex
     *            index of the sprite
     * @param lineIndex
     *            index of the line
     * @return {@link LcdImageLine}: the computed sprite individual line
     */
    private LcdImageLine spriteIndividualLine(int spriteIndex, int lineIndex) {
        Objects.checkIndex(lineIndex, LCD_HEIGHT);
        Objects.checkIndex(spriteIndex, TOTAL_SPRITES);
        LcdImageLine.Builder builder = new LcdImageLine.Builder(LCD_WIDTH);
        boolean hFlip = byte3_SpriteTest(spriteIndex, 5),
                vFlip = byte3_SpriteTest(spriteIndex, 6),
                palette = byte3_SpriteTest(spriteIndex, 4);
        int tileStartByteSprites = vFlip
                ? (spriteSize() - 1 - lineIndex + spriteY(spriteIndex))
                : lineIndex - spriteY(spriteIndex);
        int tileIndex = spriteByte(spriteIndex, 2);
        int MSB = msbAddress(1, tileIndex, tileStartByteSprites * 2);
        int LSB = lsbAddress(1, tileIndex, tileStartByteSprites * 2);
        builder = hFlip ? builder.setByte(0, MSB, LSB)
                : builder.setByte(0, Bits.reverse8(MSB), Bits.reverse8(LSB));
        return palette
                ? builder.build().shift(spriteX(spriteIndex))
                        .mapColors(reg.get(Reg.OBP1))
                : builder.build().shift(spriteX(spriteIndex))
                        .mapColors(reg.get(Reg.OBP0));
    }

    /**
     * computes the line of the given index containing all the sprites of the
     * array in either the background or foreground
     * 
     * @param sprites
     *            a sprite array of a maximum of 10 sprites
     * @param lineIndex
     *            index of the line
     * @param background
     *            boolean that states whether the sprite is in the background of
     *            foreground.
     * @return
     */
    private LcdImageLine spriteLine(int[] sprites, int lineIndex,
            boolean background) {
        Objects.checkIndex(lineIndex, LCD_HEIGHT);
        LcdImageLine spriteLine = new LcdImageLine.Builder(LCD_WIDTH).build();
        for (int i = sprites.length - 1; i >= 0; i--) {
            if (byte3_SpriteTest(sprites[i], 7) == background) {
                spriteLine = spriteLine
                        .below(spriteIndividualLine(sprites[i], lineIndex));
            }
        }
        return spriteLine;
    }

    /**
     * Computes the line of the given index by combining the background image,
     * the window and the sprites
     * 
     * @param index
     *            the index of the given line
     * @return {@link LcdImageLine} that represents the computed line
     */
    private LcdImageLine computeLine(int index) {
        Objects.checkIndex(index, LCD_HEIGHT);
        int dataWindow = reg.testBit(Reg.LCDC, BitsLCDC.WIN_AREA) ? 1 : 0;
        int data = reg.testBit(Reg.LCDC, BitsLCDC.BG_AREA) ? 1 : 0;
        LcdImageLine bgImageLine = new LcdImageLine.Builder(LCD_WIDTH).build(),
                windowImageLine,
                bgSpriteLine = new LcdImageLine.Builder(LCD_WIDTH).build(),
                fgSpriteLine = new LcdImageLine.Builder(LCD_WIDTH).build();
        int WX_adjusted = reg.get(Reg.WX) - WX_OFFSET;
        int[] sprites = spritesIntersectingLine(index);
        // background
        if (reg.testBit(Reg.LCDC, BitsLCDC.BG))
            bgImageLine = getLcdLine((index + reg.get(Reg.SCY)), data, BG_LINES)
                    .extractWrapped(reg.get(Reg.SCX), LCD_WIDTH)
                    .mapColors(reg.get(Reg.BGP));
        // window and background
        if (!windowDisabled(WX_adjusted) && index >= reg.get(Reg.WY)) {
            windowImageLine = getLcdLine(winY, dataWindow, FG_LINES)
                    .shift(WX_adjusted).mapColors(reg.get(Reg.BGP));
            winY = (winY + 1);
            bgImageLine = bgImageLine.join(windowImageLine, WX_adjusted);
        }
        // sprites
        if (reg.testBit(Reg.LCDC, BitsLCDC.OBJ)) {
            bgSpriteLine = spriteLine(sprites, index, true);
            fgSpriteLine = spriteLine(sprites, index, false);
        }
        bgImageLine = bgImageLine.below(fgSpriteLine);
        return bgSpriteLine.below(bgImageLine,
                bgImageLine.opacity().or(bgSpriteLine.opacity().not()));
    }

    private LcdImageLine getLcdLine(int lineIndex, int data,
            int numberOfTiles) {
        Preconditions.checkArgument(data == 0 || data == 1);
        Preconditions.checkArgument(
                numberOfTiles == FG_LINES || numberOfTiles == BG_LINES);
        LcdImageLine.Builder builder = new LcdImageLine.Builder(
                numberOfTiles * LINES_IN_TILE);
        int startPoint = ((lineIndex / LINES_IN_TILE) * TILES_IN_BGLINE)
                % (TILES_IN_BGLINE * BG_LINES);
        int tileStartByte = (lineIndex % LINES_IN_TILE) * 2;
        int tileSourceIndex = reg.testBit(Reg.LCDC, BitsLCDC.TILE_SOURCE) ? 1
                : 0;
        for (int i = 0; i < numberOfTiles; i++) {
            int tileIndex = this
                    .read(AddressMap.BG_DISPLAY_DATA[data] + startPoint + i);
            tileIndex = reg.testBit(Reg.LCDC, BitsLCDC.TILE_SOURCE) ? tileIndex
                    : Bits.clip(LINES_IN_TILE, tileIndex + 0x80);
            int MSB = msbAddress(tileSourceIndex, tileIndex, tileStartByte);
            int LSB = lsbAddress(tileSourceIndex, tileIndex, tileStartByte);
            builder.setByte(i, Bits.reverse8(MSB), Bits.reverse8(LSB));
        }
        return builder.build();
    }

    /**
     * @param wx
     *            Window X-axis value
     * @return true only if when the activation bit WIN in LCDC is 0 or when WX
     *         is outside the range of the image width
     */
    private boolean windowDisabled(int wx) {
        return !(reg.testBit(Reg.LCDC, BitsLCDC.WIN)) || wx < 0
                || wx >= LCD_WIDTH;
    }

    /**
     * Returns an array of the indexes of the sorted sprites
     * 
     * @param index
     *            the index of the line
     * @return a sorted array of the sprites that intersects with the given
     *         line, it is sorted by their x coordinate and index
     *
     */
    private int[] spritesIntersectingLine(int index) {
        Objects.checkIndex(index, LCD_HEIGHT);
        int[] sprites = new int[MAX_SPRITES_LINE];
        int spritesInLine = 0;
        for (int i = 0; i < TOTAL_SPRITES
                && spritesInLine < MAX_SPRITES_LINE; i++) {
            if (index >= spriteY(i) && index < spriteY(i) + spriteSize()) {
                sprites[spritesInLine] = Bits.make16(spriteX(i) + 8, i);
                spritesInLine++;
            }
        }
        Arrays.sort(sprites, 0, spritesInLine);
        int[] spritesSorted = new int[spritesInLine];
        for (int i = 0; i < spritesInLine; i++)
            spritesSorted[i] = Bits.clip(8, sprites[i]);
        return spritesSorted;
    }

    /**
     * Raises an interruption if the bit INT_MODE of {@link BitsSTAT} that
     * corresponds the given mode is true
     * 
     * @param mode
     *            the given {@link Modes}
     */
    private void lcdStatInterrupt(Modes mode) {

        if (Bits.test(reg.get(Reg.STAT), BitsSTAT.values()[3 + mode.index()])
                && mode.index() != 3)
            cpu.requestInterrupt(Interrupt.LCD_STAT);
    }

    /**
     * Check whether the screen of the {@link GameBoy} is on or off
     * 
     * @return true only if LCDstatus  bit of {@link BitsLCDC}is true
     */
    private boolean screenOn() {
        return reg.testBit(Reg.LCDC, BitsLCDC.LCD_STATUS);
    }

    private Reg getReg(int address) {
        Preconditions.checkArgument(address >= AddressMap.REGS_LCDC_START
                && address < AddressMap.REGS_LCDC_END);
        return Reg.values()[address - AddressMap.REGS_LCDC_START];
    }

    /**
     * 
     * @param index
     *            the spriteIndex
     * @param byteIndex
     *            the byteIndex
     * @return the byte of the given index that corresponds the sprite f the
     *         given index
     * 
     * @throws IllegalArgumentException
     *             if the byteIndex is not in the range 0 to 5
     */
    private int spriteByte(int index, int byteIndex) {
        Preconditions.checkArgument(
                byteIndex >= 0 && byteIndex <= Integer.SIZE / Byte.SIZE);
        return OAM.read(index * BYTES_IN_SPRITE_ATTRIBUTE + byteIndex);
    }


    private int lsbAddress(int tileSource, int index, int startByte) {
        return this.read(AddressMap.TILE_SOURCE[tileSource]
                + (TILES_IN_BGLINE / 2) * index + startByte);
    }

    private int msbAddress(int tileSource, int index, int startByte) {
        return lsbAddress(tileSource, index, startByte + 1);
    }

    private int spriteX(int index) {
        return spriteByte(index, 1) - X_OFFSET;
    }

    private int spriteY(int index) {
        return spriteByte(index, 0) - Y_OFFSET;
    }

    private int spriteSize() {
        return reg.testBit(Reg.LCDC, BitsLCDC.OBJ_SIZE) ? 16 : 8;
    }

    private boolean byte3_SpriteTest(int spriteIndex, int bit) {
        Objects.checkIndex(bit, 8);
        return Bits.test(spriteByte(spriteIndex, 3), bit);
    }

}
