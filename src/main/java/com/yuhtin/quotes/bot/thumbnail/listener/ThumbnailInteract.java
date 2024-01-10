package com.yuhtin.quotes.bot.thumbnail.listener;

import com.yuhtin.quotes.bot.thumbnail.model.Thumbnail;
import com.yuhtin.quotes.bot.thumbnail.repository.ThumbnailRepository;
import com.yuhtin.quotes.bot.thumbnail.util.ThumbnailGameGenerator;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * @author <a href="https://github.com/Yuhtin">Yuhtin</a>
 */
public class ThumbnailInteract extends ListenerAdapter {

    private final HashMap<Long, Long> cooldown = new HashMap<>();

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Message.Interaction interaction = event.getMessage().getInteraction();
        if (interaction == null) return;

        if (interaction.getUser().getIdLong() != event.getUser().getIdLong()) {
            event.reply("You can't vote in other people's buttons!").setEphemeral(true).queue();
            return;
        }

        Thumbnail thumbnail = ThumbnailRepository.instance().findById(event.getButton().getId());
        if (thumbnail == null) return;

        thumbnail.setVotes(thumbnail.getVotes() + 1);
        ThumbnailRepository.instance().insert(thumbnail);

        ThumbnailGameGenerator.generate(null, event.deferEdit());
    }
}
