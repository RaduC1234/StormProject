package me.radu.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.NoResultException;
import jakarta.transaction.Transactional;

import java.util.List;

public class UserService {

    private final EntityManager entityManager;

    public UserService(EntityManager entityManager) {
        this.entityManager = entityManager;
    }

    public List<User> findAll() {
        return entityManager.createQuery("SELECT u FROM User u", User.class).getResultList();
    }

    public User findById(Long id) {
        return entityManager.find(User.class, id);
    }

    @Transactional
    public void save(User user) {
        entityManager.getTransaction().begin();
        if (user.getId() == null) {
            entityManager.persist(user);
        } else {
            entityManager.merge(user);
        }
        entityManager.getTransaction().commit();
    }

    @Transactional
    public void deleteById(Long id) {
        entityManager.getTransaction().begin();
        User user = entityManager.find(User.class, id);
        if (user != null) {
            entityManager.remove(user);
        }
        entityManager.getTransaction().commit();
    }


    public User findByUsername(String username) {
        try {
            return entityManager.createNamedQuery("User.findByUsername", User.class)
                    .setParameter("username", username)
                    .getSingleResult();
        } catch (NoResultException e) {
            return null;
        }

    }

    public boolean existsByUsername(String username) throws Exception {
        User byUsername;
        try {
            byUsername = findByUsername(username);
        } catch (NoResultException e) {
            return false;
        }

        return byUsername != null;
    }

    public boolean deleteByUsername(String username) throws Exception {
        User byUsername = findByUsername(username);

        if (byUsername == null)
            return false;

        entityManager.getTransaction().begin();
        entityManager.remove(byUsername);
        entityManager.getTransaction().commit();
        return true;

    }
}

