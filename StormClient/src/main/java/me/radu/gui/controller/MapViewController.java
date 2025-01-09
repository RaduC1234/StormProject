package me.radu.gui.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonParseException;
import javafx.animation.FadeTransition;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import me.radu.core.ClientInstance;
import me.radu.data.Location;
import me.radu.gui.ClientGUIService;

import java.util.List;

public class MapViewController {

    @FXML
    private VBox sidebar;
    @FXML
    private VBox menuItems;

    @FXML
    private Button toggleButton;

    @FXML
    private Label forecastLabel;
    @FXML
    private Label mapsLabel;
    @FXML
    private Label adminLabel;

    @FXML
    private HBox forecastBox;
    @FXML
    private HBox mapBox;
    @FXML
    private HBox adminBox;

    @FXML
    private TextField locationInput;
    @FXML
    private Text messageLabel;

    private boolean isExpanded = false;
    ClientInstance instance;

    public MapViewController(ClientInstance instance) {
        this.instance = instance;
    }

    @FXML
    private void initialize() {
        forecastBox.setOnMouseClicked(mouseEvent -> ClientGUIService.getInstance().setScene("weatherScreen"));
        mapBox.setOnMouseClicked(mouseEvent -> ClientGUIService.getInstance().setScene("mapScreen"));
        adminBox.setOnMouseClicked(mouseEvent -> ClientGUIService.getInstance().setScene("adminScreen"));
    }

    @FXML
    private void addLocation() {
        if (locationInput.getText().isEmpty()) {
            return;
        }

        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("location", locationInput.getText().toLowerCase());

        var promise = instance.getNetworkService().sendRequest("IS_LOCATION", jsonPayload);

        promise.thenAccept(packet -> {
            Platform.runLater(() -> {
                if (packet.isError()) {
                    // If location is not found, search for the nearest location
                    findNearestLocation();
                    return;
                }

                Location location = new Gson().fromJson(packet.getPayload(), Location.class);
                instance.setSavedLocation(location);

                messageLabel.setText("Location " + location.name() + " saved successfully.");
                messageLabel.setStyle("-fx-fill: green");

                // Save location persistently
                saveLocationToServer(location.name());
            });
        });
    }

    /**
     * Finds the nearest location if the entered one is not found.
     */
    private void findNearestLocation() {
        double defaultRadiusKm = 50.0;

        JsonObject payload = new JsonObject();
        payload.addProperty("latitude", instance.getSavedLocation().latitude());
        payload.addProperty("longitude", instance.getSavedLocation().longitude());
        payload.addProperty("radiusKm", defaultRadiusKm);

        var promise = instance.getNetworkService().sendRequest("FIND_NEAREST_LOCATION", payload);

        promise.thenAccept(response -> {
            Platform.runLater(() -> {
                if (response.isError()) {
                    messageLabel.setText("No nearby locations found.");
                    messageLabel.setStyle("-fx-fill: red");
                    return;
                }

                Location nearestLocation = new Gson().fromJson(response.getPayload(), Location.class);
                messageLabel.setText("Location not found. Nearest location is " + nearestLocation.name() + ".");
                messageLabel.setStyle("-fx-fill: orange");
            });
        }).exceptionally(ex -> {
            //.error("Error fetching nearest location: {}", ex.getMessage());
            return null;
        });
    }


    /**
     * Sends a request to update `savedLocationString` on the server.
     */
    private void saveLocationToServer(String locationName) {
        JsonObject payload = new JsonObject();
        payload.addProperty("savedLocationString", locationName);

        instance.getNetworkService().sendRequest("UPDATE_SAVED_LOCATION", payload);
    }



    @FXML
    private void toggleMenu() {
        this.isExpanded = !isExpanded;

        double expandedWidth = 200;
        double collapsedWidth = 74;

        // Animate the sidebar width
        Timeline widthAnimation = new Timeline(
                new KeyFrame(Duration.millis(300),
                        new KeyValue(sidebar.prefWidthProperty(), isExpanded ? expandedWidth : collapsedWidth)
                )
        );

        widthAnimation.play();

        // List of labels to animate
        List<Label> labels = List.of(forecastLabel, mapsLabel, adminLabel);

        // Fade animation for text labels (smooth transition)
        FadeTransition fadeTransition = new FadeTransition(Duration.millis(300));
        fadeTransition.setFromValue(isExpanded ? 0 : 1);
        fadeTransition.setToValue(isExpanded ? 1 : 0);
        fadeTransition.setOnFinished(event -> {
            labels.forEach(label -> {
                label.setVisible(isExpanded);
                label.setManaged(isExpanded);
                label.getStyleClass().add("menu-item"); // Ensure they have the right CSS class
            });
        });

        labels.forEach(fadeTransition::setNode);
        fadeTransition.play();

        // Update toggle button icon
        toggleButton.setText(isExpanded ? "←" : "☰");

        // Add or remove sidebar-expanded class dynamically
        sidebar.getStyleClass().remove("sidebar-expanded");
        if (isExpanded) {
            sidebar.getStyleClass().add("sidebar-expanded");
        }
    }
}
