package com.yuhtin.quotes.bot.thumbnail.util;

import net.dv8tion.jda.api.EmbedBuilder;

import java.awt.*;

public class BotEmbedBuilder {

    public static EmbedBuilder createDefaultEmbed(String title, String footer) {
        EmbedBuilder embed = new EmbedBuilder();

        embed.setTitle(title);
        embed.setColor(Color.GREEN);
        embed.setFooter(footer, "https://i.imgur.com/qljiIQc.png");

        return embed;
    }

}