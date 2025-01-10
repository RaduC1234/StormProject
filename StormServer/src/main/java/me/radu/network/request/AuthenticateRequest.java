package me.radu.network.request;

import com.google.gson.JsonObject;
import me.radu.core.ServerInstance;
import me.radu.data.User;
import me.radu.network.Client;
import me.radu.network.IRequestTemplate;
import me.radu.network.Packet;

public class AuthenticateRequest implements IRequestTemplate {

    ServerInstance serverInstance;

    public AuthenticateRequest(ServerInstance serverInstance) {
        this.serverInstance = serverInstance;
    }

    @Override
    public void onIncomingRequest(Packet packet) {
        var payload = packet.getPayload().getAsJsonObject();
        var username = payload.has("username") ? payload.get("username").getAsString() : null;
        var password = payload.has("password") ? payload.get("password").getAsString() : null;

        var userService = serverInstance.getDatabaseManager().getUserService();
        var clients = serverInstance.getServerNetworkService().getClients();

        for(Client client : clients) {
            if(client.getUser() != null && client.getUser().getUsername().equals(username))  {
                packet.sendError(Packet.ErrorCode.USER_IN_USE);
                return;
            }
        }

        User user = userService.findByUsername(username);

        if(user == null || !user.getPassword().equals(password)) {
            packet.sendError(Packet.ErrorCode.BAD_CREDENTIALS);
            return;
        }

        packet.getClient().setUser(user);
        packet.getClient().setAuthenticated(true);
    }
}
