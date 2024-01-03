package com.yuhtin.quotes.bot.thumbnail.command;

import lombok.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * @author Yuhtin
 * Github: https://github.com/Yuhtin
 */
@Data
public final class CommandMap {

    private Map<String, Command> commands = new HashMap<>();

    public void register(String key, Command value) {
        commands.put(key, value);
    }

}