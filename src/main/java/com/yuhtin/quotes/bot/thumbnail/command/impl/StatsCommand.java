package com.yuhtin.quotes.bot.thumbnail.command.impl;

import com.yuhtin.quotes.bot.thumbnail.ThumbnailBot;
import com.yuhtin.quotes.bot.thumbnail.command.Command;
import com.yuhtin.quotes.bot.thumbnail.command.CommandInfo;
import com.yuhtin.quotes.bot.thumbnail.model.StatusUser;
import com.yuhtin.quotes.bot.thumbnail.model.Thumbnail;
import com.yuhtin.quotes.bot.thumbnail.repository.ThumbnailRepository;
import com.yuhtin.quotes.bot.thumbnail.repository.UserRepository;
import com.yuhtin.quotes.bot.thumbnail.util.BotEmbedBuilder;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.interactions.commands.CommandInteraction;

import java.util.Set;
import java.util.concurrent.TimeUnit;

@CommandInfo(
        names = {"statistics"},
        description = "See bot statistics!"
)
public class StatsCommand implements Command {

    @Override
    public void execute(CommandInteraction command) {
        if (!command.getMember().hasPermission(Permission.ADMINISTRATOR)) {
            command.reply("You don't have permission to do this!").setEphemeral(true).queue();
            return;
        }

        command.deferReply(true).queue(hook -> {
            Set<Thumbnail> thumbnails = ThumbnailRepository.instance().findAll();

            int totalBattles = thumbnails.stream().mapToInt(Thumbnail::getViews).sum();
            int totalLikes = thumbnails.stream().mapToInt(Thumbnail::getVotes).sum();
            int totalSkips = totalBattles - totalLikes;

            Set<StatusUser> statusUsers = UserRepository.instance().selectAll("");
            int totalUsers = (int) statusUsers.stream().filter(statusUser -> statusUser.getStatusSetInMillis() > 0).count();
            int totalUsersWithStatus = (int) statusUsers.stream().filter(StatusUser::isStatusSet).count();
            long totalMillis = statusUsers.stream().mapToLong(StatusUser::getStatusSetInMillis).sum();

            EmbedBuilder builder = BotEmbedBuilder.createDefaultEmbed("Soba Bot Statistics", "Soba Statistics");
            builder.setDescription(
                    "Total thumbnail battles - " + totalBattles + "\n" +
                            "Total skips - " + totalSkips + "\n" +
                            "Total likes - " + totalLikes + "\n" +
                            "\n" +
                            "Users that used the status - " + totalUsers + "\n" +
                            "Users with status set currently - " + totalUsersWithStatus + "\n" +
                            "Total hours with status - " + TimeUnit.MILLISECONDS.toHours(totalMillis) + "h\n"
            );

            hook.sendMessageEmbeds(builder.build()).queue();
        });
    }

}