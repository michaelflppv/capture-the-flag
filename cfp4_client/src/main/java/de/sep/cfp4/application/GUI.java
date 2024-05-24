package de.sep.cfp4.application;

import java.util.Locale;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.scene.text.Font;
import javafx.stage.Stage;

/**
 * Main class for the GUI of the game.
 * @author dcebulla
 * @version 0.0.1
 */
public class GUI extends Application {
    private final String TITLE = "Capture the Flag";

    @Override
    public void start(Stage stage) throws Exception {
        Font.loadFont(getClass().getResourceAsStream("fonts/Inter-Medium.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/JotiOne-Regular.ttf"), 14);
        Font.loadFont(getClass().getResourceAsStream("fonts/Inter-SemiBold.ttf"), 14);
        Locale.setDefault(Locale.ENGLISH);


        FXMLLoader loader = new FXMLLoader(Launcher.class.getResource("GUI.fxml"));
        Scene scene = new Scene(loader.load());

        stage.setTitle(this.TITLE);
        stage.getIcons().add(new Image(Launcher.class.getResourceAsStream("images/Flag.png")));
        stage.setScene(scene);
        stage.show();
    }


    public static void main(String[] args) {
        launch(args);
    }
}