package me.radu.network.request;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.radu.core.ServerInstance;
import me.radu.data.Location;
import me.radu.network.IRequestTemplate;
import me.radu.network.Packet;

public class GetLocationInfo implements IRequestTemplate {

    private ServerInstance instance;

    public GetLocationInfo(ServerInstance instance) {
        this.instance = instance;
    }

    @Override
    public void onIncomingRequest(Packet packet) {
        if (!packet.getClient().isAuthenticated()) {
            packet.sendError(Packet.ErrorCode.NOT_AUTHENTICATED);
            return;
        }

        JsonObject payload = packet.getPayload().getAsJsonObject();
        if (!payload.has("location")) {
            packet.sendError(Packet.ErrorCode.BAD_REQUEST);
            return;
        }

        String locationName = payload.get("location").getAsString();
        Location location = instance.getDatabaseManager().getLocationService().findByName(locationName);

        if (location == null) {
            packet.sendError(Packet.ErrorCode.NOT_FOUND);
            return;
        }

        packet.setPayload(new Gson().toJsonTree(location));
    }

}
