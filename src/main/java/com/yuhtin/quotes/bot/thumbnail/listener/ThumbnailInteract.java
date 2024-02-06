package com.yuhtin.quotes.bot.thumbnail.listener;

import com.yuhtin.quotes.bot.thumbnail.ThumbnailBot;
import com.yuhtin.quotes.bot.thumbnail.config.Config;
import com.yuhtin.quotes.bot.thumbnail.model.Thumbnail;
import com.yuhtin.quotes.bot.thumbnail.repository.ThumbnailRepository;
import com.yuhtin.quotes.bot.thumbnail.util.ThumbnailGameGenerator;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.ChannelType;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;

import java.util.List;

/**
 * @author <a href="https://github.com/Yuhtin">Yuhtin</a>
 */
public class ThumbnailInteract extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Message.Interaction interaction = event.getMessage().getInteraction();
        if (interaction == null) return;

        if (interaction.getUser().getIdLong() != event.getUser().getIdLong()) {
            event.reply("You can't vote in other people's buttons!").setEphemeral(true).queue();
            return;
        }

        ThumbnailGameGenerator.getInteractionMap().remove(interaction.getIdLong());

        String id = event.getButton().getId();
        if (id != null && id.equalsIgnoreCase("skip")) {
            ThumbnailGameGenerator.generate(null, event.deferEdit());
            return;
        }

        Thumbnail thumbnail = ThumbnailRepository.instance().findById(id);
        if (thumbnail != null) {
            thumbnail.setVotes(thumbnail.getVotes() + 1);
            ThumbnailRepository.instance().insert(thumbnail);
        }

        ThumbnailGameGenerator.generate(null, event.deferEdit());
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getChannel().getType() != ChannelType.TEXT) return;
        if (event.getAuthor().getIdLong() == ThumbnailBot.getInstance().getJda().getSelfUser().getIdLong()) return;

        Config config = ThumbnailBot.getInstance().getConfig();
        if (event.getChannel().getIdLong() == config.getFixedMessageChannelId()) {
            updateFixedMessage(event.getChannel().asTextChannel());
        }

        if (event.getChannel().getIdLong() == config.getFixedMessageChannelId2()) {
            updateFixedMessage2(event.getChannel().asTextChannel());
        }

    }

    public void updateFixedMessage(TextChannel channel) {
        Config config = ThumbnailBot.getInstance().getConfig();

        List<Message> rewardsMessages = channel.getHistory().retrievePast(100).complete();
        rewardsMessages.forEach(message -> {
            if (message.getAuthor().getIdLong() == channel.getJDA().getSelfUser().getIdLong()) {
                if (message.getContentRaw().equalsIgnoreCase(config.getFixedMessage())) {
                    message.delete().complete();
                }
            }
        });

        channel.sendMessage(config.getFixedMessage()).queue();
    }

    public void updateFixedMessage2(TextChannel channel) {
        Config config = ThumbnailBot.getInstance().getConfig();

        List<Message> rewardsMessages = channel.getHistory().retrievePast(100).complete();
        rewardsMessages.forEach(message -> {
            if (message.getAuthor().getIdLong() == channel.getJDA().getSelfUser().getIdLong()) {
                if (message.getContentRaw().equalsIgnoreCase(config.getFixedMessage2())) {
                    message.delete().complete();
                }
            }
        });

        channel.sendMessage(config.getFixedMessage2()).queue();
    }
}
