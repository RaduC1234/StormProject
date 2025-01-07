package me.radu.gui;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.util.Duration;
import java.util.List;

public class WeatherViewController {
    @FXML private VBox sidebar;
    @FXML private VBox menuItems;
    @FXML private Button toggleButton;
    @FXML private Label forecastLabel;
    @FXML private Label mapsLabel;
    @FXML private Label adminLabel;

    private boolean isExpanded = false;

    @FXML
    private void toggleMenu() {
        isExpanded = !isExpanded;

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
