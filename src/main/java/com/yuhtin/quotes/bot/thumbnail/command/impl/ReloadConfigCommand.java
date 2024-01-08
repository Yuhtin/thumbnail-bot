package com.yuhtin.quotes.bot.thumbnail.command.impl;

import com.yuhtin.quotes.bot.thumbnail.ThumbnailBot;
import com.yuhtin.quotes.bot.thumbnail.command.Command;
import com.yuhtin.quotes.bot.thumbnail.command.CommandInfo;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;

@CommandInfo(
        names = {"reloadconfig"},
        description = "Reload the config file"
)
public class ReloadConfigCommand implements Command {

    @Override
    public void execute(CommandInteraction command) throws Exception {
        if (!command.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            command.reply("You don't have permission to do this!").setEphemeral(true).queue();
            return;
        }

        command.reply("Reloading config...").setEphemeral(true).queue(message -> {
            ThumbnailBot.getInstance().loadConfig();
            message.editOriginal("Config reloaded!").queue();
        });

    }
}
