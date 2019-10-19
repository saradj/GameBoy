package ch.epfl.gameboj.gui;

import java.util.Objects;

import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.Preconditions;
import ch.epfl.gameboj.component.lcd.LcdController;
import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.scene.image.Image;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;

/**
 * CS-108 ImageConverter.java
 * Purpose: Representing the converter of the
 * {@link GameBoy} from an {@link LcdImage} to {@link Image}
 * 
 * @author Sara Djambazovska
 * @author Marouane Jaakik
 *
 */
public class ImageConverter {

    // colour map with different colours
    private static final int[] COLOR_MAP = new int[] { 0xFF_FF_FF_FF,
            0xFF_00_BF_FF, 0xFF_f4_6e_78, 0xFF_00_00_00 };
    //Colour map for gray palate given
//     private static final int[] COLOR_MAP = new int[] { 0xFF_FF_FF_FF,
//     0xFF_D3_D3_D3, 0xFF_A9_A9_A9, 0xFF_00_00_00 };

    /**
     * Converts the given {@link LcdImage} into a {@link Image}
     * 
     * @param image
     *            {@link LcdImage}: the image to be converted
     * @return {@link Image} the converted {@link LcdImage}
     */
    public static Image convert(LcdImage image) {
        Objects.requireNonNull(image);
        Preconditions.checkArgument(image.height() == LcdController.LCD_HEIGHT
                && image.width() == LcdController.LCD_WIDTH);

        WritableImage writableImage = new WritableImage(160, 144);
        PixelWriter wr = writableImage.getPixelWriter();
        for (int y = 0; y < image.height(); ++y) {
            for (int x = 0; x < image.width(); ++x) {
                wr.setArgb(x, y, COLOR_MAP[image.get(x, y)]);
            }
        }
        return writableImage;
    }
}
