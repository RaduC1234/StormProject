package me.radu.data;

import jakarta.persistence.EntityManager;

import java.util.List;

public class LocationService {

    private final EntityManager entityManager;

    public LocationService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<Location> findAll() {
        return entityManager.createQuery("SELECT l FROM Location l", Location.class).getResultList();
    }

    public Location findById(Long id) {
        return entityManager.find(Location.class, id);
    }

    public void save(Location location) {
        entityManager.getTransaction().begin();
        if (location.getId() == null) {
            entityManager.persist(location);
        } else {
            entityManager.merge(location);
        }
        entityManager.getTransaction().commit();
    }

    public void deleteById(Long id) {
        entityManager.getTransaction().begin();
        Location location = entityManager.find(Location.class, id);
        if (location != null) {
            entityManager.remove(location);
        }
        entityManager.getTransaction().commit();
    }
}

