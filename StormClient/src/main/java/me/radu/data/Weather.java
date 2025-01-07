package me.radu.data;

import java.util.Date;

public record Weather(
        Long id,
        Location location,
        Date date,
        Integer maxTemperature,
        Integer minTemperature,
        Condition condition
) {
    public enum Condition {
        SUNNY,
        CLOUDY,
        RAINY,
        SNOWY,
        UNKNOWN
    }
}
