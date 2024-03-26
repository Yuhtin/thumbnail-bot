package com.yuhtin.quotes.bot.thumbnail.task;

import com.yuhtin.quotes.bot.thumbnail.model.Game;
import com.yuhtin.quotes.bot.thumbnail.util.ThumbnailGameGenerator;
import lombok.val;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.utils.FileUpload;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
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
            if (game.getInitialTime() + TimeUnit.SECONDS.toMillis(30) > System.currentTimeMillis()) continue;
            if (game.getHook().isExpired()) {
                toBeRemoved.add(entry.getKey());
                continue;
            }

            EmbedBuilder embedBuilder = game.getEmbedBuilder();
            embedBuilder.setTitle("This battle already ended!");
            embedBuilder.setImage("attachment://image.png");

            BufferedImage image = game.getImage();

            Graphics2D graphics = (Graphics2D) image.getGraphics();

            graphics.setColor(Color.RED);
            graphics.setFont(new Font("Arial", Font.BOLD, 250));
            graphics.drawString("BATTLE ENDED", 225, 550);

            ByteArrayOutputStream os = new ByteArrayOutputStream();

            try {
                ImageIO.write(image, "png", os);
            } catch (IOException e) {
                toBeRemoved.add(entry.getKey());
                throw new RuntimeException(e);
            }

            InputStream is = new ByteArrayInputStream(os.toByteArray());

            try {
                game.getHook().editOriginalEmbeds(embedBuilder.build())
                        .setComponents(new ArrayList<>())
                        .setFiles(FileUpload.fromData(is, "image.png"))
                        .queue();
            } catch (Exception ignored) {}

            toBeRemoved.add(entry.getKey());
        }

        toBeRemoved.forEach(ThumbnailGameGenerator.getInteractionMap()::remove);
    }

}
