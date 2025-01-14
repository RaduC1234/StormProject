package me.radu.gui.controller;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import lombok.extern.log4j.Log4j2;
import me.radu.core.ClientInstance;
import me.radu.data.Location;
import me.radu.data.User;
import me.radu.gui.ClientGUIService;
import me.radu.network.Packet;

import java.util.Optional;

@Log4j2
public class LoginViewController {

    @FXML
    public Button login_signin_button;

    @FXML
    public TextField login_username_field;

    @FXML
    public TextField login_password_field;

    @FXML
    public Label login_info_label;

    private final ClientInstance instance;

    public LoginViewController(ClientInstance instance) {
        this.instance = instance;
    }

    @FXML
    private void handleLoginButtonPress() {
        String username = login_username_field.getText();
        String password = login_password_field.getText();

        if (username.isEmpty() || password.isEmpty()) {
            login_info_label.setText("Please enter username and password!");
        } else {
            JsonObject jsonPayload = new JsonObject();
            jsonPayload.addProperty("username", username);
            jsonPayload.addProperty("password", password);

            var promise = instance.getNetworkService().sendRequest("AUTHENTICATE", jsonPayload);

            promise.thenAccept(response -> {

                if (response.isError()) {
                    Packet.ErrorCode errorCode = response.getError();
                    Platform.runLater(() -> {
                        switch (errorCode) {
                            case USER_IN_USE:
                                login_info_label.setText("Error: User already logged in!");
                                break;
                            case BAD_CREDENTIALS:
                                login_info_label.setText("Error: Invalid username or password!");
                                break;
                            default:
                                login_info_label.setText("Error: Unknown error occurred.");

                        }
                    });
                } else {
                    ClientGUIService.getInstance().setScene("weatherScreen");
                    handleGetSelfUser();
                }

            }).exceptionally(ex -> {
                log.error("Error during authentication: {}", ex.getMessage());
                return null;
            });
        }
    }

    private void handleGetSelfUser() {
        var promise = instance.getNetworkService().sendRequest("GET_SELF_USER", null);

        promise.thenAccept(response -> {
            if (response.isError()) {
                log.error("Failed to get self user info.");
                return;
            }

            User user = new Gson().fromJson(response.getPayload(), User.class);
            instance.setSelfClient(user);

            Optional.ofNullable(user.savedLocationString())
                    .filter(locationName -> !locationName.isEmpty())
                    .ifPresent(this::requestFullLocation);
        }).exceptionally(ex -> {
            log.error("Exception while getting self user info: {}", ex.getMessage());
            return null;
        });
    }

    private void requestFullLocation(String locationName) {
        JsonObject payload = new JsonObject();
        payload.addProperty("location", locationName);

        var locationPromise = instance.getNetworkService().sendRequest("GET_LOCATION_INFO", payload);

        locationPromise.thenAccept(locationResponse -> {
            if (locationResponse.isError()) {
                log.error("Failed to get location details for {}", locationName);
                return;
            }

            Location location = new Gson().fromJson(locationResponse.getPayload(), Location.class);
            instance.setSavedLocation(location);
        }).exceptionally(ex -> {
            log.error("Error fetching location details: {}", ex.getMessage());
            return null;
        });
    }
}
