package com.yuhtin.quotes.bot.thumbnail.bot;

import com.yuhtin.quotes.bot.thumbnail.listener.ThumbnailInteract;
import com.yuhtin.quotes.bot.thumbnail.manager.RewardsManager;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.Arrays;

public class DiscordBotConnector {

    public static void connect(DiscordBot bot) {
        bot.onEnable();

        JDABuilder.createDefault(bot.getConfig().getToken())
                .setEnabledIntents(GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES)
                .disableCache(Arrays.asList(CacheFlag.values()))
                .addEventListeners(new BotConnectionListener(bot), new ThumbnailInteract(), RewardsManager.instance())
                .build();
    }

}
