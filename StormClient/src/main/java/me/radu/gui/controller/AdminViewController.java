package me.radu.gui.controller;

import com.google.gson.JsonObject;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import me.radu.core.ClientInstance;
import me.radu.gui.ClientGUIService;
import me.radu.gui.NativeFileDialogs;
import me.radu.gui.SceneAware;

import java.io.File;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.List;

public class AdminViewController implements SceneAware {

    @FXML private VBox sidebar;
    @FXML private VBox menuItems;
    @FXML private Button toggleButton;
    @FXML private Label forecastLabel;
    @FXML private Label mapsLabel;
    @FXML private Label adminLabel;
    @FXML private HBox forecastBox;
    @FXML private HBox mapBox;
    @FXML private HBox adminBox;

    @FXML private Text messageLabel;
    @FXML private Button confirmButton;

    private boolean isExpanded = false;
    private ClientInstance instance;
    private String selectedFilePath;

    public AdminViewController(ClientInstance instance) {
        this.instance = instance;
    }

    @FXML
    private void initialize() {
        forecastBox.setOnMouseClicked(mouseEvent -> ClientGUIService.getInstance().setScene("weatherScreen"));
        mapBox.setOnMouseClicked(mouseEvent -> ClientGUIService.getInstance().setScene("mapScreen"));
        adminBox.setOnMouseClicked(mouseEvent -> ClientGUIService.getInstance().setScene("adminScreen"));
    }

    @FXML
    private void toggleMenu() {
        this.isExpanded = !isExpanded;

        double expandedWidth = 200;
        double collapsedWidth = 74;


        Timeline widthAnimation = new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(sidebar.prefWidthProperty(), isExpanded ? expandedWidth : collapsedWidth)
                )
        );

        widthAnimation.play();


        List<Label> labels = List.of(forecastLabel, mapsLabel, adminLabel);


        FadeTransition fadeTransition = new FadeTransition(Duration.millis(300));
        fadeTransition.setFromValue(isExpanded ? 0 : 1);
        fadeTransition.setToValue(isExpanded ? 1 : 0);
        fadeTransition.setOnFinished(event -> {
            labels.forEach(label -> {
                label.setVisible(isExpanded);
                label.setManaged(isExpanded);
                label.getStyleClass().add("menu-item");
            });
        });

        labels.forEach(fadeTransition::setNode);
        fadeTransition.play();

        toggleButton.setText(isExpanded ? "←" : "☰");

        sidebar.getStyleClass().remove("sidebar-expanded");
        if (isExpanded) {
            sidebar.getStyleClass().add("sidebar-expanded");
        }
    }

    @FXML
    private void sendRequest() {
        if (selectedFilePath == null || selectedFilePath.isEmpty()) {
            this.messageLabel.setText("No file selected.");
            this.messageLabel.setStyle("-fx-fill: red");
            return;
        }

        File file = new File(selectedFilePath);
        if (!file.exists() || !file.isFile()) {
            this.messageLabel.setText("Selected file does not exist.");
            this.messageLabel.setStyle("-fx-fill: red");
            return;
        }

        try {
            String fileContent = Files.readString(file.toPath(), StandardCharsets.UTF_8);

            String minimizedContent = fileContent
                    .replaceAll("[\\r\\n]+", " ")
                    .replaceAll("\\s{2,}", " ")
                    .trim();

            JsonObject object = new JsonObject();
            object.addProperty("content", minimizedContent);

            var promise = instance.getNetworkService().sendRequest("ADMIN_ADD_INFO", object);

            promise.thenAccept(response -> {
                if (response.isError()) {
                    this.messageLabel.setText("Failed to update database: " + response.getError().toString());
                    this.messageLabel.setStyle("-fx-fill: red");
                } else {
                    this.messageLabel.setText("Data added successfully!");
                    this.messageLabel.setStyle("-fx-fill: green");
                }
            }).exceptionally(ex -> {
                this.messageLabel.setText("Error: " + ex.getMessage());
                this.messageLabel.setStyle("-fx-fill: red");
                return null;
            });

        } catch (IOException e) {
            this.messageLabel.setText("Error reading file: " + e.getMessage());
            this.messageLabel.setStyle("-fx-fill: red");
        }
    }

    @FXML
    private void selectFile() {
        String filePath = NativeFileDialogs.selectFile("");

        if (filePath == null || filePath.isEmpty()) {
            this.confirmButton.setManaged(false);
            this.confirmButton.setVisible(false);
            return;
        }

        if (!filePath.endsWith(".json")) {
            this.messageLabel.setText("The selected file is not a valid JSON file.");
            this.messageLabel.setStyle("-fx-fill: red");
            this.confirmButton.setManaged(false);
            this.confirmButton.setVisible(false);
            return;
        }

        this.selectedFilePath = filePath;

        this.messageLabel.setText("Selected file: " + filePath);
        this.messageLabel.setStyle("-fx-fill: white");
        this.confirmButton.setManaged(true);
        this.confirmButton.setVisible(true);
    }



    @Override
    public void onSceneShown(Scene scene) {
        this.messageLabel.setText("");
        this.confirmButton.setManaged(false);
        this.confirmButton.setVisible(false);
    }

}
