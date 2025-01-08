package me.radu.network.request;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import me.radu.core.ServerInstance;
import me.radu.data.*;
import me.radu.network.IRequestTemplate;
import me.radu.network.Packet;

import java.util.Date;

public class AdminAddInfoRequest implements IRequestTemplate {

    private ServerInstance instance;

    public AdminAddInfoRequest(ServerInstance serverInstance) {
        this.instance = serverInstance;
    }

    @Override
    public void onIncomingRequest(Packet packet) {

        if (!packet.getClient().isAuthenticated()) {
            packet.sendError(Packet.ErrorCode.NOT_AUTHENTICATED);
            return;
        } else if (packet.getClient().getUser().getType() != User.UserType.ADMIN) {
            packet.sendError(Packet.ErrorCode.BAD_REQUEST);
            return;
        }

        try {
            JsonObject payload = packet.getPayload().getAsJsonObject();

            if (!payload.has("content")) {
                packet.sendError(Packet.ErrorCode.BAD_REQUEST);
                return;
            }

            String contentString = payload.get("content").getAsString();
            JsonObject contentJson = JsonParser.parseString(contentString).getAsJsonObject();

            if (!contentJson.has("locations") || !contentJson.has("weather_records")) {
                packet.sendError(Packet.ErrorCode.BAD_REQUEST);
                return;
            }

            JsonArray locationsArray = contentJson.getAsJsonArray("locations");
            JsonArray weatherArray = contentJson.getAsJsonArray("weather_records");

            LocationService locationService = instance.getDatabaseManager().getLocationService();
            WeatherService weatherService = instance.getDatabaseManager().getWeatherService();

            for (JsonElement locationElement : locationsArray) {
                JsonObject locationObj = locationElement.getAsJsonObject();
                String name = locationObj.get("name").getAsString();
                double latitude = locationObj.get("latitude").getAsDouble();
                double longitude = locationObj.get("longitude").getAsDouble();


                Location location = locationService.findByName(name);
                if (location == null) {
                    location = new Location();
                    location.setName(name);
                    location.setLatitude(latitude);
                    location.setLongitude(longitude);
                    locationService.save(location);
                }
            }

            // Process weather records
            for (JsonElement weatherElement : weatherArray) {
                JsonObject weatherObj = weatherElement.getAsJsonObject();
                JsonObject locationObj = weatherObj.getAsJsonObject("location");

                String locationName = locationObj.get("name").getAsString();
                Location location = locationService.findByName(locationName);

                if (location == null) {
                    continue;
                }

                Date date = java.sql.Date.valueOf(weatherObj.get("date").getAsString());
                int maxTemperature = weatherObj.get("maxTemperature").getAsInt();
                int minTemperature = weatherObj.get("minTemperature").getAsInt();
                Weather.Condition condition = Weather.Condition.valueOf(weatherObj.get("condition").getAsString());

                Weather weather = new Weather();
                weather.setLocation(location);
                weather.setDate(date);
                weather.setMaxTemperature(maxTemperature);
                weather.setMinTemperature(minTemperature);
                weather.setCondition(condition);

                weatherService.save(weather);
            }
        } catch (Exception e) {
            packet.sendError(Packet.ErrorCode.INTERNAL_ERROR);
        }
    }

}
