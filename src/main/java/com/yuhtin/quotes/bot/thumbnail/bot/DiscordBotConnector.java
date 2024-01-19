package com.yuhtin.quotes.bot.thumbnail.bot;

import com.yuhtin.quotes.bot.thumbnail.listener.ThumbnailInteract;
import com.yuhtin.quotes.bot.thumbnail.manager.RewardsManager;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

public class DiscordBotConnector {

    public static void connect(DiscordBot bot) {
        bot.onEnable();

        JDABuilder.create(bot.getConfig().getToken(), GatewayIntent.GUILD_MEMBERS, GatewayIntent.GUILD_MESSAGES, GatewayIntent.MESSAGE_CONTENT, GatewayIntent.GUILD_PRESENCES)
                .enableCache(CacheFlag.ACTIVITY, CacheFlag.CLIENT_STATUS, CacheFlag.ONLINE_STATUS)
                .disableCache(CacheFlag.VOICE_STATE, CacheFlag.SCHEDULED_EVENTS, CacheFlag.EMOJI, CacheFlag.STICKER, CacheFlag.MEMBER_OVERRIDES, CacheFlag.ROLE_TAGS)
                .setChunkingFilter(ChunkingFilter.ALL)
                .addEventListeners(new BotConnectionListener(bot), new ThumbnailInteract(), RewardsManager.instance())
                .build();
    }

}
