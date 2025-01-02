package me.radu.data;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.ToString;

import java.util.Date;
import java.util.Objects;

@Entity
@Getter
@Setter
@ToString
@RequiredArgsConstructor
public class Weather {

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE)
    private Long id;

    @ToString.Exclude
    @ManyToOne(cascade = CascadeType.DETACH, optional = false)
    @JoinColumn(name = "location_id", nullable = false)
    private Location location;

    @Temporal(TemporalType.DATE)
    private Date date;

    private Integer maxTemperature;

    private Integer minTemperature;

    private Condition condition;

    enum Condition {
        SUNNY,
        CLOUDY,
        RAINY,
        SNOWY,

        UNKNOWN
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Weather weather = (Weather) o;
        return Objects.equals(id, weather.id) && Objects.equals(location, weather.location) && Objects.equals(date, weather.date) && Objects.equals(maxTemperature, weather.maxTemperature) && Objects.equals(minTemperature, weather.minTemperature) && condition == weather.condition;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, location, date, maxTemperature, minTemperature, condition);
    }
}
