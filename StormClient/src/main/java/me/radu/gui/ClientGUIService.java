package me.radu.gui;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Screen;
import javafx.stage.Stage;
import lombok.Getter;
import me.radu.ClientMain;
import me.radu.core.ClientInstance;
import me.radu.gui.controller.AdminViewController;
import me.radu.gui.controller.LoginViewController;
import me.radu.gui.controller.MapViewController;
import me.radu.gui.controller.WeatherViewController;

import java.io.IOException;
import java.util.Objects;

import static java.lang.System.exit;
import static java.lang.System.in;

public class ClientGUIService extends Application {

    public final int BASE_SCREEN_WIDTH = 1600;
    public final int BASE_SCREEN_HEIGHT = 800;

    private static ClientGUIService instance;
    private static ClientInstance clientInstance;
    private static SceneRegistry registry;

    @Getter private static LoginViewController loginViewController;
    @Getter private static WeatherViewController weatherViewController;
    @Getter private static MapViewController mapViewController;
    @Getter private static AdminViewController adminViewController;

    public static void launchGUI(ClientInstance instance) {
        clientInstance = instance;
        loginViewController = new LoginViewController(instance);
        weatherViewController = new WeatherViewController(instance);
        mapViewController = new MapViewController(instance);
        adminViewController = new AdminViewController(instance);
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
        FXMLLoader mapScreen = new FXMLLoader(ClientMain.class.getResource("/scenes/map-view.fxml")); mapScreen.setController(mapViewController);
        FXMLLoader adminScreen = new FXMLLoader(ClientMain.class.getResource("/scenes/admin-view.fxml")); adminScreen.setController(adminViewController);

        registry = new SceneRegistry(stage);
        registry.addScreen("loginScreen", new Scene(loginScreen.load(), BASE_SCREEN_WIDTH, BASE_SCREEN_HEIGHT), loginViewController);
        registry.addScreen("weatherScreen", new Scene(weatherScreen.load(), BASE_SCREEN_WIDTH, BASE_SCREEN_HEIGHT), weatherViewController);
        registry.addScreen("mapScreen", new Scene(mapScreen.load(), BASE_SCREEN_WIDTH, BASE_SCREEN_HEIGHT),  mapViewController);
        registry.addScreen("adminScreen", new Scene(adminScreen.load(), BASE_SCREEN_WIDTH, BASE_SCREEN_HEIGHT), adminViewController);
    }

    public void setScene(String sceneName) {
        Platform.runLater(()-> {
            registry.activate(sceneName);
        });
    }
}
