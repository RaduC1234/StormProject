package me.radu.data;

import java.util.ArrayList;

public record Location(
        Long id,
        String name,
        Long latitude,
        Long longitude,
        ArrayList<Weather> weather
) {}
