package com.yuhtin.quotes.bot.thumbnail.command.impl;

import com.yuhtin.quotes.bot.thumbnail.command.Command;
import com.yuhtin.quotes.bot.thumbnail.command.CommandInfo;
import com.yuhtin.quotes.bot.thumbnail.model.Thumbnail;
import com.yuhtin.quotes.bot.thumbnail.repository.ThumbnailRepository;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.requests.restaction.interactions.ReplyCallbackAction;
import net.dv8tion.jda.api.utils.FileUpload;
import net.dv8tion.jda.internal.utils.tuple.Pair;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

/**
 * @author <a href="https://github.com/Yuhtin">Yuhtin</a>
 */
@CommandInfo(
        names = {"thumbnail"},
        description = "Vote in the best thumbnail!"
)
public class ThumbnailCommand implements Command {

    @Override
    public void execute(CommandInteraction command) throws Exception {
        ReplyCallbackAction callback = command.deferReply(true);

        Pair<Thumbnail, Thumbnail> pair = ThumbnailRepository.instance().selectRandom();

        BufferedImage bufferedImage = new BufferedImage(1920, 1080, BufferedImage.TYPE_INT_RGB);
        Graphics2D graphics = bufferedImage.createGraphics();

        graphics.setColor(new Color(34, 36, 38));
        graphics.fillRect(0, 0, 1920, 1080);
        graphics.setColor(Color.WHITE);
        graphics.setFont(new Font("Arial", Font.BOLD, 100));
        graphics.drawString("x", 935, 540);

        Thumbnail thumbnail1 = pair.getLeft();
        Thumbnail thumbnail2 = pair.getRight();

        thumbnail1.setViews(thumbnail1.getViews() + 1);
        thumbnail2.setViews(thumbnail2.getViews() + 1);

        ThumbnailRepository.instance().insert(thumbnail1);
        ThumbnailRepository.instance().insert(thumbnail2);

        BufferedImage file1 = ImageIO.read(thumbnail1.getFile());
        BufferedImage file2 = ImageIO.read(thumbnail2.getFile());

        graphics.drawImage(file1, 0, 0, 930, 1080, null);
        graphics.drawImage(file2, 995, 0, 930, 1080, null);

        ByteArrayOutputStream os = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "jpeg", os);
        InputStream is = new ByteArrayInputStream(os.toByteArray());

        callback.addEmbeds(new EmbedBuilder()
                        .setTitle("Thumbnail battle!")
                        .setColor(Color.RED)
                        .setDescription("Vote on the best thumbnail bellow or check the thumbnail rank by using /trank\n")
                        .setImage("attachment://thumbnail.png")
                        .build()
                )
                .addFiles(FileUpload.fromData(is, "thumbnail.png"))
                .addActionRow(
                        Button.success(thumbnail1.getId(), thumbnail1.getName() + " (" + thumbnail1.getVotes() + " 👍)"),
                        Button.success(thumbnail2.getId(), thumbnail2.getName() + " (" + thumbnail2.getVotes() + " 👍)" )
                ).queue();

    }

}
