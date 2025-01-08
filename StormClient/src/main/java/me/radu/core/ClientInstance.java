package me.radu.core;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.log4j.Log4j2;
import me.radu.data.Location;
import me.radu.data.User;
import me.radu.gui.ClientGUIService;
import me.radu.network.ClientNetworkService;

import java.util.Optional;

@Log4j2
public class ClientInstance {

    @Getter
    private ClientNetworkService networkService;
    private Thread guiThread;

    @Getter
    @Setter
    private User selfClient;
    @Getter
    @Setter
    private Location savedLocation;

    public void start() {
        // Start GUI on a new thread
        guiThread = new Thread(() -> {
            ClientGUIService.launchGUI(this);
        });
        guiThread.setDaemon(true);
        guiThread.start();

        // Start network client on this thread
        networkService = new ClientNetworkService();
        try {
            networkService.connect("127.0.0.1", 8080); // Change host/port as needed
        } catch (Exception e) {
            System.err.println("Failed to connect to server: " + e.getMessage());
        }

        ClientGUIService.getInstance().setScene("loginScreen");
    }

    public void stop() {
        if (networkService != null) {
            networkService.stop();
        }
        log.info("Client stopped.");
    }
}
