package com.yuhtin.quotes.bot.thumbnail.manager;

import com.google.common.collect.Lists;
import com.yuhtin.quotes.bot.thumbnail.model.RateLimit;
import lombok.var;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class RateLimitManager {

    private static final RateLimitManager INSTANCE = new RateLimitManager();

    private final ConcurrentHashMap<Long, List<RateLimit>> RATE_LIMITS = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<Long, Long> USER_DELAY = new ConcurrentHashMap<>();

    private static final int MAX_THRESHOLD = 4;
    private static final int CACHE_THRESHOLD_SECONDS = 120;

    public void clean() {
        List<Long> toRemove = new ArrayList<>();
        for (var entry : RATE_LIMITS.entrySet()) {
            if (entry.getValue().isEmpty()) {
                toRemove.add(entry.getKey());
            } else if (entry.getValue().get(entry.getValue().size() - 1).getTime() < System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(10)) {
                toRemove.add(entry.getKey());
            }
        }

        toRemove.forEach(RATE_LIMITS::remove);
    }

    public boolean tryUse(Long user) {
        long delay = USER_DELAY.getOrDefault(user, 0L);
        return delay <= System.currentTimeMillis();
    }

    public void increase(Long user) {
        List<RateLimit> oldRateLimits = RATE_LIMITS.getOrDefault(user, Lists.newArrayList());
        List<RateLimit> newRateLimits = Lists.newArrayList();
        for (RateLimit rateLimit : oldRateLimits) {
            if (rateLimit.getTime() + TimeUnit.SECONDS.toMillis(CACHE_THRESHOLD_SECONDS) > System.currentTimeMillis()) {
                newRateLimits.add(rateLimit);
            }
        }

        newRateLimits.add(new RateLimit());

        if (RATE_LIMITS.containsKey(user)) RATE_LIMITS.replace(user, newRateLimits);
        else RATE_LIMITS.put(user, newRateLimits);

        if (newRateLimits.size() >= MAX_THRESHOLD) {
            USER_DELAY.remove(user);
            USER_DELAY.put(user, System.currentTimeMillis() + TimeUnit.HOURS.toMillis(1));

            newRateLimits.clear();
        }
    }

    public static RateLimitManager instance() {
        return INSTANCE;
    }

}
