package com.yuhtin.quotes.bot.thumbnail.util;

/**
 * @author <a href="https://github.com/Yuhtin">Yuhtin</a>
 */
public final class DiscordUtil {

    private static final String EMAIL_REGEX = "^[\\w-\\.]+@([\\w-]+\\.)+[\\w-]{2,4}$";

    public static String normalizeDiscordName(String discord) {
        if (discord == null || discord.isEmpty()) return null;
        if (discord.matches(EMAIL_REGEX)) return null;

        String normalized = discord.contains("#") ? discord.split("#")[0] : discord;
        return normalized.toLowerCase();
    }

}
