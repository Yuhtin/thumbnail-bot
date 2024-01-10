package com.yuhtin.quotes.bot.thumbnail.util;

import com.yuhtin.quotes.bot.thumbnail.ThumbnailBot;
import com.yuhtin.quotes.bot.thumbnail.config.Config;
import com.yuhtin.quotes.bot.thumbnail.model.Thumbnail;
import com.yuhtin.quotes.bot.thumbnail.repository.ThumbnailRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.interactions.MessageEditCallbackAction;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.Nullable;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class ThumbnailGameGenerator {

    public static void generate(@Nullable CommandInteraction command, @Nullable MessageEditCallbackAction edit) {
        ReplyCallbackAction replyCallbackAction = command == null ? null : command.deferReply();

        Pair<Thumbnail, Thumbnail> pair = ThumbnailRepository.instance().selectRandom();

        BufferedImage bufferedImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bufferedImage.createGraphics();

        graphics.setColor(new Color(34, 36, 38));
        graphics.fillRect(0, 0, 1920, 1080);
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Arial", Font.BOLD, 100));
        graphics.drawString("x", 933, 540);

        try {
            Thumbnail thumbnail1 = pair.getLeft();
            Thumbnail thumbnail2 = pair.getRight();

            thumbnail1.setViews(thumbnail1.getViews() + 1);
            thumbnail2.setViews(thumbnail2.getViews() + 1);

            ThumbnailRepository.instance().insert(thumbnail1);
            ThumbnailRepository.instance().insert(thumbnail2);

            BufferedImage bufferedImage1 = ImageIO.read(thumbnail1.getFile());
            BufferedImage bufferedImage2 = ImageIO.read(thumbnail2.getFile());

            BufferedImage file1 = cutCenter(bufferedImage1, 930, 1080);
            BufferedImage file2 = cutCenter(bufferedImage2, 930, 1080);

            graphics.drawImage(file1, 0, 0, 930, 1080, null);
            graphics.drawImage(file2, 995, 0, 930, 1080, null);

            ByteArrayOutputStream os = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpeg", os);
            InputStream is = new ByteArrayInputStream(os.toByteArray());

            Config config = ThumbnailBot.getInstance().getConfig();
            MessageEmbed embed = new EmbedBuilder()
                    .setTitle(config.getThumbnailEmbedTitle())
                    .setColor(Color.RED)
                    .setDescription(config.getThumbnailEmbedDescription())
                    .setImage("attachment://thumbnail.jpeg")
                    .build();

            FileUpload fileUpload = FileUpload.fromData(is, "thumbnail.jpeg");

            Button firstButton = Button.success(thumbnail1.getId(), thumbnail1.getName() + " (" + thumbnail1.getVotes() + " 👍)");
            Button seccondButton = Button.success(thumbnail2.getId(), thumbnail2.getName() + " (" + thumbnail2.getVotes() + " 👍)");

            if (replyCallbackAction == null) {
                edit.setEmbeds(embed)
                        .setFiles(fileUpload)
                        .setActionRow(firstButton, seccondButton)
                        .queue();
            } else {
                replyCallbackAction.setEmbeds(embed)
                        .setFiles(fileUpload)
                        .setActionRow(firstButton, seccondButton)
                        .queue();
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    private static BufferedImage cutCenter(BufferedImage originalImage, int width, int height) {
        int centerX = originalImage.getWidth() / 2;
        int centerY = originalImage.getHeight() / 2;

        int x = Math.max(0, centerX - (width / 2));
        int y = Math.max(0, centerY - (height / 2));

        int subimageWidth = Math.min(originalImage.getWidth() - x, width);
        int subimageHeight = Math.min(originalImage.getHeight() - y, height);

        return originalImage.getSubimage(x, y, subimageWidth, subimageHeight);
    }

}
