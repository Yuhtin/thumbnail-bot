package com.yuhtin.quotes.bot.thumbnail.command;

import lombok.Data;
import lombok.EqualsAndHashCode;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

/**
 * @author Yuhtin
 * Github: https://github.com/Yuhtin
 */
@Data
@EqualsAndHashCode(callSuper = true)
public final class CommandCatcher extends ListenerAdapter {

    private static final CommandCatcher INSTANCE = new CommandCatcher(new CommandMap());
    private final CommandMap commandMap;

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        Command command = commandMap.getCommands().get(event.getName());
        try {
            command.execute(event);
        } catch (Exception exception) {
            exception.printStackTrace();
            event.getHook().sendMessage("ERRO!").setEphemeral(true).queue();
        }
    }

    public static CommandCatcher getInstance() {
        return INSTANCE;
    }
}
