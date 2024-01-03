package com.yuhtin.quotes.bot.thumbnail.bot;

import lombok.Builder;
import lombok.Getter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;

import java.util.List;

@Builder
@Getter
public class BotProperties {

    private final List<CacheFlag> disabledCaches;
    private final List<GatewayIntent> enabledIntents;

}
