package com.yuhtin.quotes.bot.thumbnail.util;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.yuhtin.quotes.bot.thumbnail.config.Config;
import lombok.AllArgsConstructor;

@AllArgsConstructor
public class Serializer<T> {

    public static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();

    public static final Serializer<Config> CONFIG = new Serializer<>(Config.class);

    private final Class<T> type;

    public String serialize(T type) {
        return GSON.toJson(type);
    }

    public T deserialize(String json) {
        return GSON.fromJson(json, type);
    }

}