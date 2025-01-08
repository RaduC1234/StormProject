package me.radu.gui.controller;

import com.google.gson.JsonObject;
import javafx.animation.*;
import javafx.application.Platform;
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
import me.radu.data.Location;
import me.radu.data.User;
import me.radu.gui.ClientGUIService;
import me.radu.gui.SceneAware;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class WeatherViewController implements SceneAware {
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
    private Text locationTitle;
    @FXML
    private Label currentWeatherDate;
    @FXML
    private ImageView currentWeatherIcon;
    @FXML
    private Label currentWeatherTemp;
    @FXML
    private Label currentWeatherStatus;

    @FXML
    private Label dayOneLabel;
    @FXML
    private ImageView dayOneIcon;
    @FXML
    private Label dayOneTemp;

    @FXML
    private Label dayTwoLabel;
    @FXML
    private ImageView dayTwoIcon;
    @FXML
    private Label dayTwoTemp;

    @FXML
    private Label dayThreeLabel;
    @FXML
    private ImageView dayThreeIcon;
    @FXML
    private Label dayThreeTemp;

    @FXML
    private Label dayFourLabel;
    @FXML
    private ImageView dayFourIcon;
    @FXML
    private Label dayFourTemp;

    @FXML
    private Label dayFiveLabel;
    @FXML
    private ImageView dayFiveIcon;
    @FXML
    private Label dayFiveTemp;

    private boolean isExpanded = false;
    private final ClientInstance instance;

    public WeatherViewController(ClientInstance instance) {
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

        if (user.type() != User.UserType.ADMIN) {
            adminBox.setVisible(false);
            adminBox.setManaged(false);
        }


        LocalDate today = LocalDate.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd-MM-yyyy");
        currentWeatherDate.setText(today.format(formatter));

        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("EEEE");
        dayOneLabel.setText(today.format(dayFormatter));
        dayTwoLabel.setText(today.plusDays(1).format(dayFormatter));
        dayThreeLabel.setText(today.plusDays(2).format(dayFormatter));
        dayFourLabel.setText(today.plusDays(3).format(dayFormatter));
        dayFiveLabel.setText(today.plusDays(4).format(dayFormatter));

        if (instance.getSavedLocation() == null) {
            locationTitle.setText("❌ No location selected.");

            Image noDataImage = new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/delete.png")));
            currentWeatherIcon.setImage(noDataImage);
            dayOneIcon.setImage(noDataImage);
            dayTwoIcon.setImage(noDataImage);
            dayThreeIcon.setImage(noDataImage);
            dayFourIcon.setImage(noDataImage);
            dayFiveIcon.setImage(noDataImage);

            currentWeatherTemp.setText("No data");
            currentWeatherStatus.setText("No data");
            dayOneTemp.setText("❌ No Data");
            dayTwoTemp.setText("❌ No Data");
            dayThreeTemp.setText("❌ No Data");
            dayFourTemp.setText("❌ No Data");
            dayFiveTemp.setText("❌ No Data");
            return;
        }

        Location location = instance.getSavedLocation();

        locationTitle.setText(location.name());

        JsonObject jsonPayload = new JsonObject();
        jsonPayload.addProperty("location", location.name());
        var promise = instance.getNetworkService().sendRequest("GET_FORECAST", jsonPayload);

        promise.thenAccept(response -> {
            if (response.isError()) {
                locationTitle.setText("❌ Failed to load weather data.");
                return;
            }

            JsonObject responseData = response.getPayload().getAsJsonObject();
            if (!responseData.has("days")) {
                locationTitle.setText("❌ Invalid weather data received.");
                return;
            }

            JsonObject days = responseData.getAsJsonObject("days");

            Platform.runLater(() -> {
                updateWeatherInfo(dayOneLabel, dayOneIcon, dayOneTemp, days, "day1");
                updateWeatherInfo(dayTwoLabel, dayTwoIcon, dayTwoTemp, days, "day2");
                updateWeatherInfo(dayThreeLabel, dayThreeIcon, dayThreeTemp, days, "day3");
                updateWeatherInfo(dayFourLabel, dayFourIcon, dayFourTemp, days, "day4");
                updateWeatherInfo(dayFiveLabel, dayFiveIcon, dayFiveTemp, days, "day5");
            });
        });
    }

    private void updateWeatherInfo(Label dayLabel, ImageView dayIcon, Label dayTemp, JsonObject days, String dayKey) {

        if (!days.has(dayKey)) {
            dayTemp.setText("❌ No Data");
            return;
        }

        JsonObject dayData = days.getAsJsonObject(dayKey);

        int maxTemp = dayData.get("maxTemperature").getAsInt();
        int minTemp = dayData.get("minTemperature").getAsInt();
        String condition = dayData.get("condition").getAsString();

        dayTemp.setText(minTemp + "°C / " + maxTemp + "°C");

        String iconPath = "/images/" + condition.toLowerCase() + ".png";
        try {
            dayIcon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconPath))));
        } catch (Exception e) {
            dayIcon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/unknown.png"))));
        }

        if(dayKey.equals("day1")) {
            try {
                currentWeatherIcon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream(iconPath))));
            } catch (Exception e) {
                currentWeatherIcon.setImage(new Image(Objects.requireNonNull(getClass().getResourceAsStream("/images/unknown.png"))));
            }

            currentWeatherStatus.setText(
                    Arrays.stream(condition.split(""))
                            .map(s -> s.equals(condition.substring(0, 1)) ? s.toUpperCase() : s.toLowerCase())
                            .collect(Collectors.joining())
            );

            currentWeatherTemp.setText(maxTemp + "°C");
        }
    }
}
