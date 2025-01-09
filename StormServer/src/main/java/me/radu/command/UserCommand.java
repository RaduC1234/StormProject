package me.radu.command;

import me.radu.data.User;
import me.radu.data.UserService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.Collections;
import java.util.List;

public class UserCommand extends ICommand {

    private static final Logger LOGGER = LogManager.getLogger(UserCommand.class);

    private final UserService userService;

    public UserCommand(UserService userService) {
        this.userService = userService;

        this.name = "user";
        this.description = "Command for basic user management.";
        this.usage = "user [delete/add] <username> <password> <firstName> <lastName> <Type[NORMAL,ADMIN]> | user list ";
    }

    @Override
    public void execute() throws Exception {
        String[] args = input.split(" ");

        switch (args[1]) {
            case "add" -> {
                User user = new User();
                user.setUsername(args[2]);
                user.setPassword(args[3]);
                user.setFirstName(args[4]);
                user.setLastName(args[5]);
                user.setType(User.UserType.valueOf(args[6]));


                if (userService.existsByUsername(user.getUsername())) {
                    LOGGER.error("Error: User already exists.");
                    return;
                }
                userService.save(user);
                LOGGER.info("New user created with the following properties: \n" +
                        "Username--> '" + user.getUsername() + "'\n" +
                        "Password--> '" + user.getPassword() + "'\n" +
                        "User type--> '" + user.getType().toString() + "'"
                );
            }
            case "delete" -> {
                if (userService.deleteByUsername((args[2]))) {
                    LOGGER.info("User successfully deleted.");
                }
            }
            case "list" -> {
                List<User> users = userService.findAll();
                Collections.sort(users);
                StringBuilder builder = new StringBuilder();
                for (User user : users) {
                    builder.append("Id: ").append(user.getId())
                            .append(". Username: ").append(user.getUsername())
                            .append(". Password: ").append(user.getPassword())
                            .append(". Type:").append(user.getType())
                            .append("\n");
                }
                LOGGER.info("\n{}", builder);
            }
            default -> LOGGER.error("Syntax error.");
        }
    }
}