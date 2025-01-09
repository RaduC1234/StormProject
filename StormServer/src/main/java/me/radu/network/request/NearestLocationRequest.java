package me.radu.network.request;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.radu.core.ServerInstance;
import me.radu.data.Location;
import me.radu.network.IRequestTemplate;
import me.radu.network.Packet;

import java.util.Optional;

public class NearestLocationRequest implements IRequestTemplate {

    private ServerInstance instance;

    public NearestLocationRequest(ServerInstance instance) {
        this.instance = instance;
    }

    @Override
    public void onIncomingRequest(Packet packet) {
        if (!packet.getClient().isAuthenticated()) {
            packet.sendError(Packet.ErrorCode.NOT_AUTHENTICATED);
            return;
        }

        JsonObject payload = packet.getPayload().getAsJsonObject();
        if (!payload.has("latitude") || !payload.has("longitude") || !payload.has("radiusKm")) {
            packet.sendError(Packet.ErrorCode.BAD_REQUEST);
            return;
        }

        double latitude = payload.get("latitude").getAsDouble();
        double longitude = payload.get("longitude").getAsDouble();
        double radiusKm = payload.get("radiusKm").getAsDouble();

        Optional<Location> nearestLocation = instance.getDatabaseManager().getLocationService().findNearestLocation(latitude, longitude, radiusKm);

        if (nearestLocation.isEmpty()) {
            packet.sendError(Packet.ErrorCode.NOT_FOUND);
            return;
        }

        packet.setPayload(new Gson().toJsonTree(nearestLocation.get()));
    }
}
