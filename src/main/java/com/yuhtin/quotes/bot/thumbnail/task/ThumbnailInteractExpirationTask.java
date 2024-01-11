package com.yuhtin.quotes.bot.thumbnail.task;

import com.yuhtin.quotes.bot.thumbnail.model.Game;
import com.yuhtin.quotes.bot.thumbnail.util.ThumbnailGameGenerator;
import lombok.val;
import net.dv8tion.jda.api.EmbedBuilder;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.TimeUnit;

public class ThumbnailInteractExpirationTask extends TimerTask {

    @Override
    public void run() {
        List<Long> toBeRemoved = new ArrayList<>();

        for (val entry : ThumbnailGameGenerator.getInteractionMap().entrySet()) {
            Game game = entry.getValue();
            if (game.getInitialTime() + TimeUnit.SECONDS.toMillis(30) < System.currentTimeMillis()) continue;

            EmbedBuilder embedBuilder = game.getEmbedBuilder();
            embedBuilder.setTitle("This battle already ended!");

            game.getHook().editOriginalEmbeds(embedBuilder.build())
                    .setActionRow()
                    .queue();

            toBeRemoved.add(entry.getKey());
        }

        toBeRemoved.forEach(ThumbnailGameGenerator.getInteractionMap()::remove);
    }

}
