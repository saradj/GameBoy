package ch.epfl.gameboj.gui;

import java.io.File;
import java.util.List;
import java.util.Map;
import ch.epfl.gameboj.GameBoy;
import ch.epfl.gameboj.component.Joypad.Key;
import ch.epfl.gameboj.component.cartridge.Cartridge;
import ch.epfl.gameboj.component.lcd.LcdImage;
import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.ImageView;
import javafx.scene.layout.BorderPane;

import javafx.stage.Stage;

/**
 * CS-108 Main.java 
 * Purpose: Representing the main class containing the main
 * program of the simulation, extends {@link Application}
 * 
 * @author Sara Djambazovska
 * @author Marouane Jaakik
  * 
 */
public final class Main extends Application {

    private final Map<String, Key> keyMap = Map.of("LEFT", Key.LEFT, "RIGHT",
            Key.RIGHT, "UP", Key.UP, "DOWN", Key.DOWN);
    private final Map<String, Key> textMap = Map.of("A", Key.A, "B", Key.B, "S",
            Key.SELECT, " ", Key.START);

    /**
     * Represents the main program that receives the name of the ROM file to be
     * read
     * 
     * @param args
     *            String[]: containing the name of the ROM file to be put in the
     *            {@link Cartridge}
     */
    public static void main(String[] args) {
        Application.launch(args);
    }

    @Override
    public void start(Stage stage) throws Exception {
        long start = System.nanoTime();
        List<String> Parameters = getParameters().getRaw();
        if (Parameters.size() != 1)
            System.exit(1);
        File romFile = new File(Parameters.get(0));
        GameBoy gb = new GameBoy(Cartridge.ofFile(romFile));
        ImageView imageView = new ImageView();
        LcdImage image = gb.lcdController().currentImage();
        imageView.setImage(ImageConverter.convert(image));
        imageView.setFitWidth(image.width() * 2);
        imageView.setFitHeight(image.height() * 2);

        BorderPane borderPane = new BorderPane(imageView);
        Scene scene = new Scene(borderPane);
        stage.setScene(scene);
        stage.sizeToScene();
        stage.setTitle("Gameboj");
        imageView.setOnKeyPressed(e -> {
            if (keyMap.containsKey(e.getCode().name()))
                gb.joypad().keyPressed(keyMap.get(e.getCode().name()));
            else if (textMap.containsKey(e.getText().toUpperCase()))
                gb.joypad().keyPressed(textMap.get(e.getText().toUpperCase()));
        });
        imageView.setOnKeyReleased(e -> {
            if (keyMap.containsKey(e.getCode().name()))
                gb.joypad().keyReleased(keyMap.get(e.getCode().name()));
            else if (textMap.containsKey(e.getText().toUpperCase()))
                gb.joypad().keyReleased(textMap.get(e.getText().toUpperCase()));
        });

        AnimationTimer timer = new AnimationTimer() {
            public void handle(long currentNanoTime) {
                imageView.setImage(ImageConverter
                        .convert(gb.lcdController().currentImage()));
                long elapsedTime = currentNanoTime - start;
                gb.runUntil(
                        (long) (elapsedTime * GameBoy.CYCLES_PER_NANOSECOND));
            }
        };
        stage.show();
        imageView.requestFocus();
        timer.start();
    }
}
