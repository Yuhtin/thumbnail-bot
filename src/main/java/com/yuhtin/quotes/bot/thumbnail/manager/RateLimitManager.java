package com.yuhtin.quotes.bot.thumbnail.manager;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.collect.Lists;
import com.yuhtin.quotes.bot.thumbnail.model.RateLimit;
import lombok.var;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class RateLimitManager {

    private static final RateLimitManager INSTANCE = new RateLimitManager();

    private final Cache<Long, Long> USER_COOLDOWN = CacheBuilder.newBuilder()
            .expireAfterWrite(10, TimeUnit.MINUTES)
            .build();

    private final Cache<Long, Long> USER_DELAY = CacheBuilder.newBuilder()
            .expireAfterWrite(30, TimeUnit.MINUTES)
            .build();

    public boolean tryUse(Long user) {
        if (USER_DELAY.getIfPresent(user) != null) return false;
        if (USER_COOLDOWN.getIfPresent(user) != null) {
            USER_DELAY.put(user, 0L);
            USER_COOLDOWN.invalidate(user);
            return false;
        }

        return true;
    }

    public void increase(Long user) {
        USER_COOLDOWN.put(user, 0L);
    }

    public static RateLimitManager instance() {
        return INSTANCE;
    }

}
