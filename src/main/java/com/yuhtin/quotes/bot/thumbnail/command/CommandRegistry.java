package com.yuhtin.quotes.bot.thumbnail.command;

import com.google.common.reflect.ClassPath;
import lombok.Data;
import lombok.val;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Data(staticConstructor = "of")
public class CommandRegistry {

    private final JDA client;

    public void register() {
        client.addEventListener(CommandCatcher.getInstance());

        Logger logger = Logger.getLogger("ThumbnailBot");
        ClassPath classPath;
        try {
            classPath = ClassPath.from(getClass().getClassLoader());
        } catch (IOException exception) {
            logger.severe("ClassPath could not be instantiated");
            return;
        }

        CommandMap commandMap = CommandCatcher.getInstance().getCommandMap();

        List<CommandDataImpl> commands = new ArrayList<>();
        for (val info : classPath.getTopLevelClassesRecursive("com.yuhtin.quotes.bot.thumbnail.command.impl")) {
            try {
                Class className = Class.forName(info.getName());
                Object object = className.newInstance();

                if (className.isAnnotationPresent(CommandInfo.class)) {
                    Command command = (Command) object;
                    CommandInfo handler = (CommandInfo) className.getAnnotation(CommandInfo.class);

                    for (String name : handler.names()) {
                        commandMap.register(name, command);
                        commands.add(new CommandDataImpl(name, handler.description()));
                    }

                } else throw new InstantiationException();
            } catch (Exception exception) {
                exception.printStackTrace();
                logger.severe("The " + info.getName() + " class could not be instantiated");
            }
        }

        client.retrieveCommands().queue(createdCommands -> {
            for (val command : commands) {
                boolean exists = false;
                for (val createdCommand : createdCommands) {
                    if (createdCommand.getName().equals(command.getName())) {
                        exists = true;
                        break;
                    }
                }

                if (!exists) {
                    logger.info("Adding " + command.getName() + " because is a new command.");
                    client.upsertCommand(command).queue();
                }
            }
        });

        logger.info("Registered " + commandMap.getCommands().size() + " commands successfully");
    }

}