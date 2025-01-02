package me.radu.network;

import lombok.Getter;
import lombok.Setter;
import me.radu.data.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.channels.SocketChannel;

public class Client {

    private static final Logger LOGGER = LogManager.getLogger(Client.class);

    @Getter
    private final SocketChannel channel;

    @Getter
    private final String guestName;

    private boolean isAuthenticated;

    @Setter
    @Getter
    private User user;

    public Client(SocketChannel channel) throws IOException {
        this.isAuthenticated = false;
        this.channel = channel;
        this.guestName = channel.getRemoteAddress().toString();
    }

    public boolean isAuthenticated() {
        return isAuthenticated;
    }

    public void setAuthenticated(boolean authenticated) {
        isAuthenticated = authenticated;
    }

    public void disconnect(String reason) {
        LOGGER.info("Client {} has disconnected. ({})", this.getGuestName(), reason);
        try {
            this.getChannel().close();
        } catch (IOException e) {
            LOGGER.error("Client {} is already disconnected", this.getGuestName());
        }
    }
}
