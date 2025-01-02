package me.radu.command;

public abstract class ICommand {

    public String name = null;

    public String usage = "no usage available";

    public String description = "no description available";

    public String input = null;

    public abstract void execute() throws Exception;
}
