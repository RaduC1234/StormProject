package me.radu.gui;

import javafx.scene.Scene;
import javafx.stage.Stage;

import java.util.HashMap;

public class SceneRegistry {

    private Stage main;
    private HashMap<String, Scene> screenMap = new HashMap<>();
    private String activeSceneName = null;

    public SceneRegistry() {
    }

    public SceneRegistry(Stage main) {
        this.main = main;
    }

    public Scene getScene(String name) {
        return screenMap.get(name);
    }

    public SceneRegistry addScreen(String name, Scene pane){
        screenMap.put(name, pane);
        return this;
    }

    public void removeScreen(String name){
        screenMap.remove(name);
    }

    public void activate(String name){
        if(main.isShowing())
            main.hide();
        main.setScene(screenMap.get(name));
        main.show();
        activeSceneName = name;
    }

}
