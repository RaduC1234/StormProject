package me.radu.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.layout.StackPane;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Synchronized;
import me.radu.ClientMain;
import me.radu.core.ClientInstance;

import java.io.IOException;
import java.util.Objects;

import static java.lang.System.exit;

public class ClientGUIService extends Application {

    public final int BASE_SCREEN_WIDTH = 1600;
    public final int BASE_SCREEN_HEIGHT = 800;

    private static ClientGUIService instance;
    private static ClientInstance clientInstance;
    private static SceneRegistry registry;

    @Getter
    private static LoginViewController loginViewController;
    @Getter
    private static WeatherViewController weatherViewController;

    public static void launchGUI(ClientInstance instance) {
        clientInstance = instance;
        loginViewController = new LoginViewController(instance);
        weatherViewController = new WeatherViewController();
        launch();
    }


    public synchronized static ClientGUIService getInstance() {
        while (instance == null) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        }
        return instance;
    }

    @Override
    public void start(Stage stage) throws IOException {
        instance = this;

        stage.setTitle("Storm Client");
        stage.getIcons().add(new Image(Objects.requireNonNull(ClientMain.class.getResourceAsStream("/images/duke.png"))));
        stage.setOnCloseRequest(event -> exit(0));

        double screenWidth = Screen.getPrimary().getBounds().getWidth();
        double screenHeight = Screen.getPrimary().getBounds().getHeight();

        stage.setWidth(screenWidth * 4 / 5);
        stage.setHeight(screenHeight * 3 / 4);
        stage.centerOnScreen();

        FXMLLoader loginScreen = new FXMLLoader(ClientMain.class.getResource("/scenes/login-view.fxml")); loginScreen.setController(loginViewController);
        FXMLLoader weatherScreen = new FXMLLoader(ClientMain.class.getResource("/scenes/weather-view.fxml")); weatherScreen.setController(weatherViewController);

        registry = new SceneRegistry(stage);
        registry.addScreen("loginScreen", new Scene(loginScreen.load(), BASE_SCREEN_WIDTH, BASE_SCREEN_HEIGHT));
        registry.addScreen("weatherScreen", new Scene(weatherScreen.load(), BASE_SCREEN_WIDTH, BASE_SCREEN_HEIGHT));
    }

    public void setScene(String sceneName) {
        Platform.runLater(()-> {
            registry.activate(sceneName);
        });
    }
}
