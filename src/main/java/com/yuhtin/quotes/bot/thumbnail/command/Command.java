package com.yuhtin.quotes.bot.thumbnail.command;

import net.dv8tion.jda.api.interactions.commands.CommandInteraction;

/**
 * @author Yuhtin
 * Github: https://github.com/Yuhtin
 */
public interface Command {

    void execute(CommandInteraction command) throws Exception;

}
