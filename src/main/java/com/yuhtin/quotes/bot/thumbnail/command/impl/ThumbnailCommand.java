package com.yuhtin.quotes.bot.thumbnail.command.impl;

import com.yuhtin.quotes.bot.thumbnail.command.Command;
import com.yuhtin.quotes.bot.thumbnail.command.CommandInfo;
import com.yuhtin.quotes.bot.thumbnail.model.Thumbnail;
import com.yuhtin.quotes.bot.thumbnail.repository.ThumbnailRepository;
import com.yuhtin.quotes.bot.thumbnail.util.ThumbnailGameGenerator;
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
        ThumbnailGameGenerator.generate(command, null);
    }

}
