package com.yuhtin.quotes.bot.thumbnail.model;

import lombok.Getter;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@Getter
public class RateLimit {

    private final long time = System.currentTimeMillis();

}
