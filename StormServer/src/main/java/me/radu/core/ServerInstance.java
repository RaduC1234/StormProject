package me.radu.core;

import me.radu.command.CommandHandler;
import me.radu.data.DatabaseManager;
import me.radu.network.ServerNetworkService;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.io.IOException;
import java.util.Enumeration;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

public class ServerInstance {

    private static final Logger LOGGER = LogManager.getLogger(ServerInstance.class);

    private ServerNetworkService serverNetworkService;
    private CommandHandler commandHandler;
    private DatabaseManager databaseManager;

    public ServerInstance() {
        this.serverNetworkService = new ServerNetworkService(this);
        this.commandHandler = new CommandHandler();
    }

    public void start() {
        LOGGER.info("Starting server...");

        try {
            LOGGER.info("Starting Database...");
            ResourceBundle config = ResourceBundle.getBundle("server");
            this.databaseManager = new DatabaseManager.Builder()
                    .setJdbcDriver(config.getString("hibernate.connection.driver_class"))
                    .setJdbcUrl(config.getString("hibernate.connection.url"))
                    .setJdbcUser(config.getString("hibernate.connection.username"))
                    .setJdbcPassword(config.getString("hibernate.connection.password"))
                    .setJdbcDialect(config.getString("hibernate.connection.dialect"))
                    .setDdlGeneration(config.getString("hibernate.connection.hbm2ddl.auto"))
                    .setLoggingLevelSql(config.getString("hibernate.connection.show_sql"))
                    .build();

            LOGGER.info("DatabaseManager initialized successfully.");
        } catch (NullPointerException | MissingResourceException e) {
            LOGGER.fatal("Fatal error. Config file does not exist or contains invalid keys.", e);
            stop();
        } catch (Exception e) {
            LOGGER.fatal("Unexpected error during DatabaseManager initialization.", e);
            stop();
        }

        try {
            LOGGER.info("Starting Network Service...");
            this.serverNetworkService.start(8080);
        } catch (IOException e) {
            LOGGER.error(e.getMessage());
        }
    }

    public void stop() {

    }
}
