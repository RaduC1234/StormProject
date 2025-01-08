package me.radu.gui;

import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.HashMap;

public class SceneRegistry {

    private Stage main;
    private HashMap<String, Scene> screenMap = new HashMap<>();
    private String activeSceneName = null;
    private HashMap<String, Object> controllerMap = new HashMap<>(); // Store controllers

    public SceneRegistry(Stage main) {
        this.main = main;
    }

    public Scene getScene(String name) {
        return screenMap.get(name);
    }

    public Object getController(String name) {
        return controllerMap.get(name);
    }

    public SceneRegistry addScreen(String name, Scene scene, Object controller) {
        screenMap.put(name, scene);
        controllerMap.put(name, controller);
        return this;
    }

    public void removeScreen(String name) {
        screenMap.remove(name);
        controllerMap.remove(name);
    }

    public void activate(String name) {
        if (main.isShowing()) main.hide();

        Scene scene = screenMap.get(name);
        if (scene != null) {
            main.setScene(scene);
            main.show();
            activeSceneName = name;

            Object controller = controllerMap.get(name);
            if (controller instanceof SceneAware) {
                ((SceneAware) controller).onSceneShown(scene);
            }
        }
    }
}
