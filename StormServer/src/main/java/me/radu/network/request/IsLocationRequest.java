package me.radu.network.request;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.radu.data.Location;
import me.radu.network.IRequestTemplate;
import me.radu.network.Packet;
import me.radu.data.LocationService;

public class IsLocationRequest implements IRequestTemplate {

    private static final Gson GSON = new Gson();
    private final LocationService locationService;

    public IsLocationRequest(LocationService locationService) {
        this.locationService = locationService;
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
        Location location = locationService.findByName(locationName);

        if (location == null) {
            packet.sendError(Packet.ErrorCode.NOT_FOUND);
            return;
        }

        // Convert location to JSON and send response
        packet.setPayload(GSON.toJsonTree(location));
    }
}
