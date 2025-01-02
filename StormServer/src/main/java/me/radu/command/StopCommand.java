package me.radu.command;

import me.radu.core.ServerInstance;

public class StopCommand extends ICommand {

    private final ServerInstance serverInstance;

    public StopCommand(ServerInstance serverInstance) {
        this.serverInstance = serverInstance;
        this.name = "stop";
        this.usage = "stop";
        this.description = "Stopping server";
    }

    @Override
    public void execute() {
        this.serverInstance.stop();
    }
}