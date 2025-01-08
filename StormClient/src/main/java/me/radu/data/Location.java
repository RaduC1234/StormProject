package me.radu.data;

import java.util.ArrayList;

public record Location(
        Long id,
        String name,
        Double latitude,
        Double longitude,
        ArrayList<Weather> weather
) {}
