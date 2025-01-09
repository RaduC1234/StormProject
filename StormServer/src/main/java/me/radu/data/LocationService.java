package me.radu.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;

import java.util.List;
import java.util.Optional;

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

    @Transactional
    public void save(Location location) {
        entityManager.getTransaction().begin();
        if (location.getId() == null) {
            entityManager.persist(location);
        } else {
            entityManager.merge(location);
        }
        entityManager.getTransaction().commit();
    }

    @Transactional
    public void deleteById(Long id) {
        entityManager.getTransaction().begin();
        Location location = entityManager.find(Location.class, id);
        if (location != null) {
            entityManager.remove(location);
        }
        entityManager.getTransaction().commit();
    }

    public Location findByName(String name) {
        try {
            return entityManager.createNamedQuery("Location.findByName", Location.class)
                    .setParameter("name", name)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }
    }

    public Optional<Location> findNearestLocation(double latitude, double longitude, double radiusKm) {
        List<Location> locations = entityManager.createQuery("SELECT l FROM Location l", Location.class).getResultList();

        Location nearestLocation = null;
        double minDistance = Double.MAX_VALUE;

        for (Location location : locations) {
            double distance = calculateHaversineDistance(latitude, longitude, location.getLatitude(), location.getLongitude());

            if (distance <= radiusKm && distance < minDistance) {
                minDistance = distance;
                nearestLocation = location;
            }
        }

        return Optional.ofNullable(nearestLocation);
    }

    private double calculateHaversineDistance(double lat1, double lon1, double lat2, double lon2) {
        final int EARTH_RADIUS = 6371;
        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));
        return EARTH_RADIUS * c;
    }
}

