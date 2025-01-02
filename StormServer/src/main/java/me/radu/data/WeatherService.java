package me.radu.data;

import jakarta.persistence.EntityManager;

import java.util.List;

public class WeatherService {

    private final EntityManager entityManager;

    public WeatherService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Weather> findAll() {
        return entityManager.createQuery("SELECT w FROM Weather w", Weather.class).getResultList();
    }

    public Weather findById(Long id) {
        return entityManager.find(Weather.class, id);
    }

    public void save(Weather weather) {
        entityManager.getTransaction().begin();
        if (weather.getId() == null) {
            entityManager.persist(weather);
        } else {
            entityManager.merge(weather);
        }
        entityManager.getTransaction().commit();
    }

    public void deleteById(Long id) {
        entityManager.getTransaction().begin();
        Weather weather = entityManager.find(Weather.class, id);
        if (weather != null) {
            entityManager.remove(weather);
        }
        entityManager.getTransaction().commit();
    }
}
