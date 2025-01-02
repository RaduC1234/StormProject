package me.radu.data;

import jakarta.persistence.EntityManager;
import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

import java.util.Properties;

public class DatabaseManager {

    private static final Logger LOGGER = LogManager.getLogger(DatabaseManager.class);

    private final EntityManager entityManager;

    @Getter
    private final LocationService locationService;
    @Getter
    private final UserService userService;
    @Getter
    private final WeatherService weatherService;
    private SessionFactory sessionFactory;

    private DatabaseManager(Builder builder) {
        this.entityManager = builder.entityManager;
        this.locationService = new LocationService(entityManager);
        this.userService = new UserService(entityManager);
        this.weatherService = new WeatherService(entityManager);
    }

    public static class Builder {

        private EntityManager entityManager;

        private String jdbcDriver;
        private String jdbcUrl;
        private String jdbcUser;
        private String jdbcPassword;
        private String ddlGeneration;
        private String loggingLevelSql;
        private String jdbcDialect;

        public Builder setJdbcDriver(String jdbcDriver) {
            this.jdbcDriver = jdbcDriver;
            return this;
        }

        public Builder setJdbcUrl(String jdbcUrl) {
            this.jdbcUrl = jdbcUrl;
            return this;
        }

        public Builder setJdbcUser(String jdbcUser) {
            this.jdbcUser = jdbcUser;
            return this;
        }

        public Builder setJdbcPassword(String jdbcPassword) {
            this.jdbcPassword = jdbcPassword;
            return this;
        }

        public Builder setJdbcDialect(String jdbcDialect) {
            this.jdbcDialect = jdbcDialect;
            return this;
        }

        public Builder setDdlGeneration(String ddlGeneration) {
            this.ddlGeneration = ddlGeneration;
            return this;
        }

        public Builder setLoggingLevelSql(String loggingLevelSql) {
            this.loggingLevelSql = loggingLevelSql;
            return this;
        }

        public DatabaseManager build() {
            try {

                Properties settings = new Properties();
                settings.put(Environment.DRIVER, jdbcDriver);
                settings.put(Environment.URL, jdbcUrl);
                settings.put(Environment.USER, jdbcUser);
                settings.put(Environment.PASS, jdbcPassword);
                settings.put(Environment.DIALECT, jdbcDialect);
                settings.put(Environment.SHOW_SQL,loggingLevelSql);
                settings.put(Environment.HBM2DDL_AUTO, ddlGeneration);

                Configuration configuration = new Configuration();
                configuration.setProperties(settings);
                configuration.addAnnotatedClass(User.class);
                configuration.addAnnotatedClass(Location.class);
                configuration.addAnnotatedClass(Weather.class);

                LOGGER.info("Hibernate Configuration loaded");

                ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder().applySettings(configuration.getProperties()).build();
                LOGGER.info("Hibernate serviceRegistry created");

                SessionFactory sessionFactory = configuration.buildSessionFactory(serviceRegistry);

                this.entityManager = sessionFactory.openSession().getEntityManagerFactory().createEntityManager();
            } catch (Exception ex) {
                LOGGER.fatal(ex.getMessage());
            }
            return new DatabaseManager(this);
        }
    }

    public void close() {
        if (entityManager != null) {
            entityManager.close();
        }
    }


}
