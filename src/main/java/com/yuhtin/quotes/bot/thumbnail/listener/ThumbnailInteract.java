package com.yuhtin.quotes.bot.thumbnail.listener;

import com.yuhtin.quotes.bot.thumbnail.model.Thumbnail;
import com.yuhtin.quotes.bot.thumbnail.repository.ThumbnailRepository;
import com.yuhtin.quotes.bot.thumbnail.util.ThumbnailGameGenerator;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

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

        String id = event.getButton().getId();
        if (id != null && id.equalsIgnoreCase("skip")) {
            ThumbnailGameGenerator.generate(null, event.deferEdit());
            return;
        }

        Thumbnail thumbnail = ThumbnailRepository.instance().findById(id);
        if (thumbnail == null) return;

        thumbnail.setVotes(thumbnail.getVotes() + 1);
        ThumbnailRepository.instance().insert(thumbnail);

        ThumbnailGameGenerator.generate(null, event.deferEdit());
    }
}
