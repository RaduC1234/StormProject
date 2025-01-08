package me.radu.gui.controller;

import javafx.animation.*;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import javafx.scene.text.Text;
import javafx.util.Duration;
import me.radu.core.ClientInstance;
import me.radu.data.User;
import me.radu.gui.ClientGUIService;
import me.radu.gui.SceneAware;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Objects;

public class WeatherViewController implements SceneAware {
    @FXML private VBox sidebar;
    @FXML private VBox menuItems;
    @FXML private Button toggleButton;
    @FXML private Label forecastLabel;
    @FXML private Label mapsLabel;
    @FXML private Label adminLabel;
    @FXML private HBox forecastBox;
    @FXML private HBox mapBox;
    @FXML private HBox adminBox;

    @FXML private Text locationTitle;
    @FXML private Label currentWeatherDate;
    @FXML private ImageView currentWeatherIcon;
    @FXML private Label currentWeatherTemp;
    @FXML private Label currentWeatherStatus;

    // Updated day labels & images
    @FXML private Label dayOneLabel;
    @FXML private ImageView dayOneIcon;
    @FXML private Label dayOneTemp;

    @FXML private Label dayTwoLabel;
    @FXML private ImageView dayTwoIcon;
    @FXML private Label dayTwoTemp;

    @FXML private Label dayThreeLabel;
    @FXML private ImageView dayThreeIcon;
    @FXML private Label dayThreeTemp;

    @FXML private Label dayFourLabel;
    @FXML private ImageView dayFourIcon;
    @FXML private Label dayFourTemp;

    @FXML private Label dayFiveLabel;
    @FXML private ImageView dayFiveIcon;
    @FXML private Label dayFiveTemp;

    private boolean isExpanded = false;
    private final ClientInstance instance;

    public WeatherViewController(ClientInstance instance) {
        this.instance = instance;
    }

    @FXML
    private void initialize() {
        forecastBox.setOnMouseClicked(mouseEvent -> ClientGUIService.getInstance().setScene("weatherScreen"));
        mapBox.setOnMouseClicked(mouseEvent -> ClientGUIService.getInstance().setScene("mapScreen"));
        adminBox.setOnMouseClicked(mouseEvent -> ClientGUIService.getInstance().setScene("adminScene"));
    }

    @FXML
    private void toggleMenu() {
        isExpanded = !isExpanded;
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
        fadeTransition.setOnFinished(event -> labels.forEach(label -> {
            label.setVisible(isExpanded);
            label.setManaged(isExpanded);
            label.getStyleClass().add("menu-item");
        }));

        labels.forEach(fadeTransition::setNode);
        fadeTransition.play();

        toggleButton.setText(isExpanded ? "←" : "☰");

        sidebar.getStyleClass().remove("sidebar-expanded");
        if (isExpanded) sidebar.getStyleClass().add("sidebar-expanded");
    }

    @Override
    public void onSceneShown(Scene scene) {
        User user = instance.getSelfClient();

        // Hide admin box if the user is not an admin
        if (user.type() != User.UserType.ADMIN) {
            adminBox.setVisible(false);
            adminBox.setManaged(false);
        }

        // Set current date in the format "day-month-year"
        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        currentWeatherDate.setText(today.format(formatter));

        // Set day labels for the next 5 days
        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE");
        dayOneLabel.setText(today.format(dayFormatter));
        dayTwoLabel.setText(today.plusDays(1).format(dayFormatter));
        dayThreeLabel.setText(today.plusDays(2).format(dayFormatter));
        dayFourLabel.setText(today.plusDays(3).format(dayFormatter));
        dayFiveLabel.setText(today.plusDays(4).format(dayFormatter));

        // Check if a location is selected
        if (instance.getSavedLocation() == null) {
            locationTitle.setText("❌ No location selected.");

            // Load "delete.png" image for missing data
            Image noDataImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/delete.png")));
            currentWeatherIcon.setImage(noDataImage);
            dayOneIcon.setImage(noDataImage);
            dayTwoIcon.setImage(noDataImage);
            dayThreeIcon.setImage(noDataImage);
            dayFourIcon.setImage(noDataImage);
            dayFiveIcon.setImage(noDataImage);

            // Set weather status when no data is available
            currentWeatherTemp.setText("No data");
            currentWeatherStatus.setText("No data");
            dayOneTemp.setText("❌ No Data");
            dayTwoTemp.setText("❌ No Data");
            dayThreeTemp.setText("❌ No Data");
            dayFourTemp.setText("❌ No Data");
            dayFiveTemp.setText("❌ No Data");
        }
    }
}
