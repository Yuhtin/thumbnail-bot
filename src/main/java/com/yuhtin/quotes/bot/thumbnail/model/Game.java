package com.yuhtin.quotes.bot.thumbnail.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.InteractionHook;

import java.awt.image.BufferedImage;

@AllArgsConstructor
@Getter
public class Game {

    private final InteractionHook hook;
    private final EmbedBuilder embedBuilder;
    private final long initialTime;
    private final BufferedImage image;

}