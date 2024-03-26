package com.yuhtin.quotes.bot.thumbnail.command;

import com.google.common.reflect.ClassPath;
import com.yuhtin.quotes.bot.thumbnail.command.impl.ProfileCommand;
import com.yuhtin.quotes.bot.thumbnail.config.Config;
import lombok.Data;
import lombok.val;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.SlashCommandData;
import net.dv8tion.jda.internal.interactions.CommandDataImpl;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

@Data(staticConstructor = "of")
public class CommandRegistry {

    private final JDA client;
    private final Config config;

    public void register() {
        client.addEventListener(CommandCatcher.getInstance());
        client.addEventListener(new ProfileCommand());

        Logger logger = Logger.getLogger("ThumbnailBot");
        ClassPath classPath;
        try {
            classPath = ClassPath.from(getClass().getClassLoader());
        } catch (IOException exception) {
            logger.severe("ClassPath could not be instantiated");
            return;
        }

        CommandMap commandMap = CommandCatcher.getInstance().getCommandMap();

        List<SlashCommandData> commands = new ArrayList<>();
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

        commands.add(new CommandDataImpl("profile", "View profile by username")
                .addOption(OptionType.STRING, "username", "Username to search for", true));

        Guild guild = client.getGuildById(config.getSobaServerId());
        if (guild == null) {
            logger.severe("Guild not found");
            return;
        }

        guild.updateCommands().addCommands(commands).queue();
        logger.info("Registered " + commandMap.getCommands().size() + " commands successfully");
    }

}