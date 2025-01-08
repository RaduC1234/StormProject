package me.radu.network.request;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import me.radu.data.Location;
import me.radu.data.LocationService;
import me.radu.network.IRequestTemplate;
import me.radu.network.Packet;

public class IsLocationRequest implements IRequestTemplate {

    LocationService locationService;

    public IsLocationRequest(LocationService locationService) {
        this.locationService = locationService;
    }

    @Override
    public void onIncomingRequest(Packet packet) {

        if(!packet.getClient().isAuthenticated()) {
            packet.sendError(Packet.ErrorCode.NOT_AUTHENTICATED);
            return;
        }

        // Extract the payload as a JSON object
        JsonObject payload = packet.getPayload().getAsJsonObject();

        // Validate the payload and location name
        if (payload.isJsonNull() || !payload.has("location")) {
            packet.sendError(Packet.ErrorCode.BAD_CREDENTIALS);
            return;
        }

        // Find the location by name
        String locationName = payload.get("location").getAsString();
        Location location = locationService.findByName(locationName);

        // Check if the location exists
        if (location == null) {
            packet.sendError(Packet.ErrorCode.BAD_CREDENTIALS);
            return;
        }

        // Convert the Location object to JSON using Gson
        Gson gson = new Gson();
        JsonObject locationJson = gson.toJsonTree(location).getAsJsonObject();

        // Set the location as the response payload
        packet.setPayload(locationJson);
    }
}
