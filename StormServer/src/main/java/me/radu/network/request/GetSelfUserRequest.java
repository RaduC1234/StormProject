package me.radu.network.request;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import me.radu.data.User;
import me.radu.network.IRequestTemplate;
import me.radu.network.Packet;

public class GetSelfUserRequest implements IRequestTemplate {

    private static final Gson GSON = new Gson();

    @Override
    public void onIncomingRequest(Packet packet) {
        if (!packet.getClient().isAuthenticated()) {
            packet.sendError(Packet.ErrorCode.NOT_AUTHENTICATED);
            return;
        }

        User user = packet.getClient().getUser();
        JsonElement userJson = GSON.toJsonTree(user);

        packet.setPayload(userJson);
    }
}
