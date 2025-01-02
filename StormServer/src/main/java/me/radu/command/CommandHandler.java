package me.radu.command;

import lombok.Getter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CommandHandler {

    private static final Logger LOGGER = LogManager.getLogger(CommandHandler.class);

    private final ExecutorService service = Executors.newSingleThreadExecutor();

    private List<ICommand> commands;

    public CommandHandler() {
        commands = new ArrayList<>();
    }

    public CommandHandler addCommand(ICommand command) {
        commands.add(command);
        return this;
    }

    public CommandHandler addCommands(ICommand... commands) {
        for (ICommand command : commands)
            addCommand(command);
        return this;
    }

    public CommandHandler listen() {
        service.execute(() -> {
            Thread.currentThread().setName("CommandHandler");

            String consoleLine;

            while (true) {

                consoleLine = ConsoleInput.readLine();

                boolean commandFound = false;

                for (ICommand command : commands) {
                    if (consoleLine.startsWith(command.name)) {
                        commandFound = true;
                        command.input = consoleLine;
                        try {
                            command.execute();
                        } catch (Exception e) {
                           LOGGER.warn("Error parsing command. Not enough arguments.");
                        }
                    }
                }
                if (consoleLine.startsWith("help")) {
                    StringBuilder row = new StringBuilder();
                    for (ICommand command : commands) {
                        row.append(command.name).append(" -> ").append(command.usage).append("\n");
                    }
                    LOGGER.info("List of all commands: \n{0}".replace("{0}", row));
                    continue;
                }

                if (!commandFound) {
                    LOGGER.warn("Unknown command.");
                }

            }
        });
        return this;
    }

    public void stop() {
        service.shutdownNow();
    }

    public boolean isStarted() {
        return !service.isTerminated();
    }

    private static class ConsoleInput {

        static BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));

        public static String readLine() {
            try {
                return reader.readLine();
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }
}
