package com.yuhtin.quotes.bot.thumbnail.listener;

import com.yuhtin.quotes.bot.thumbnail.model.Thumbnail;
import com.yuhtin.quotes.bot.thumbnail.repository.ThumbnailRepository;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.ActionRow;
import net.dv8tion.jda.api.interactions.components.LayoutComponent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="https://github.com/Yuhtin">Yuhtin</a>
 */
public class ThumbnailInteract extends ListenerAdapter {

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Thumbnail thumbnail = ThumbnailRepository.instance().findById(event.getButton().getId());
        if (thumbnail == null) return;

        thumbnail.setVotes(thumbnail.getVotes() + 1);
        ThumbnailRepository.instance().insert(thumbnail);

        List<Button> editedComponents = new ArrayList<>();
        for (LayoutComponent component : event.getMessage().getComponents()) {
            for (Button button : component.getButtons()) {
                if (button.getId().equals(event.getButton().getId())) {
                    editedComponents.add(button
                            .withLabel(thumbnail.getName() + " (" + thumbnail.getVotes() + " 👍)")
                            .asDisabled()
                    );
                } else {
                    editedComponents.add(Button.danger(button.getId(), button.getLabel()).asDisabled());
                }
            }
        }

        event.editComponents(ActionRow.of(editedComponents))
                .setContent("You voted in the thumbnail " + thumbnail.getName() + "!").queue();
    }
}
