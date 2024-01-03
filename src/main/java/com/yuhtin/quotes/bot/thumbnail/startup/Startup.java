package com.yuhtin.quotes.bot.thumbnail.startup;


import com.yuhtin.quotes.bot.thumbnail.ThumbnailBot;
import com.yuhtin.quotes.bot.thumbnail.bot.DiscordBotConnector;

public class Startup {

    public static void main(String[] args) {
        DiscordBotConnector.connect(ThumbnailBot.getInstance());
    }

}
