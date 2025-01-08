package me.radu.network.request;

import com.google.gson.JsonObject;
import me.radu.core.ServerInstance;
import me.radu.data.Location;
import me.radu.data.LocationService;
import me.radu.data.Weather;
import me.radu.data.WeatherService;
import me.radu.network.IRequestTemplate;
import me.radu.network.Packet;

import java.sql.Date;
import java.time.LocalDate;

public class GetForecastRequest implements IRequestTemplate {

    private ServerInstance instance;

    public GetForecastRequest(ServerInstance instance) {
        this.instance = instance;
    }

    @Override
    public void onIncomingRequest(Packet packet) {
        try {
            JsonObject payload = packet.getPayload().getAsJsonObject();
            if (!payload.has("location")) {
                packet.sendError(Packet.ErrorCode.BAD_REQUEST);
                return;
            }

            String locationName = payload.get("location").getAsString();
            LocationService locationService = instance.getDatabaseManager().getLocationService();
            WeatherService weatherService = instance.getDatabaseManager().getWeatherService();

            Location location = locationService.findByName(locationName);
            if (location == null) {
                packet.sendError(Packet.ErrorCode.NOT_FOUND);
                return;
            }

            LocalDate today = LocalDate.now();
            JsonObject responseJson = new JsonObject();
            JsonObject daysJson = new JsonObject();

            for (int i = 0; i < 5; i++) {
                LocalDate date = today.plusDays(i);
                Weather weather = weatherService.getWeatherByDateAndLocation(Date.valueOf(date), location);
                if (weather != null) {
                    JsonObject weatherJson = new JsonObject();
                    weatherJson.addProperty("date", date.toString());
                    weatherJson.addProperty("maxTemperature", weather.getMaxTemperature());
                    weatherJson.addProperty("minTemperature", weather.getMinTemperature());
                    weatherJson.addProperty("condition", weather.getCondition().name());
                    daysJson.add("day" + (i + 1), weatherJson);
                }
            }

            responseJson.add("days", daysJson);
            packet.setPayload(responseJson);

        } catch (Exception e) {
            packet.sendError(Packet.ErrorCode.INTERNAL_ERROR);
        }
    }

}
