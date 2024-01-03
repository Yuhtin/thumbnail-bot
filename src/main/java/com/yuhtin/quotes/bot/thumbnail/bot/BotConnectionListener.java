package com.yuhtin.quotes.bot.thumbnail.bot;

import com.yuhtin.quotes.bot.thumbnail.util.TaskHelper;
import lombok.AllArgsConstructor;
import net.dv8tion.jda.api.events.session.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

@AllArgsConstructor
public class BotConnectionListener extends ListenerAdapter {

    private final DiscordBot bot;

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        bot.serve(event.getJDA());
        TaskHelper.runAsync(bot::onReady);
    }
}