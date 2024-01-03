package com.yuhtin.quotes.bot.thumbnail.manager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yuhtin.quotes.bot.thumbnail.ThumbnailBot;
import com.yuhtin.quotes.bot.thumbnail.model.StatusReward;
import com.yuhtin.quotes.bot.thumbnail.model.StatusUser;
import com.yuhtin.quotes.bot.thumbnail.repository.UserRepository;
import com.yuhtin.quotes.bot.thumbnail.util.BotEmbedBuilder;
import com.yuhtin.quotes.bot.thumbnail.util.PrivateEmbedMessages;
import com.yuhtin.quotes.bot.thumbnail.util.PrivateMessages;
import lombok.Getter;
import lombok.val;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateActivitiesEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Getter
public class RewardsManager extends ListenerAdapter {

    private final ThumbnailBot bot;
    private LinkedHashMap<String, StatusReward> statusRewardMap;

    public RewardsManager(ThumbnailBot bot) {
        this.bot = bot;

        loadStatusRewards();
        updateRewardsMessage();

        bot.getJda().addEventListener(new ListenerAdapter() {
            @Override
            public void onUserUpdateActivities(@Nonnull UserUpdateActivitiesEvent event) {
                if (event.getNewValue() == null || event.getNewValue().isEmpty()) {
                    checkStatus(event.getMember(), null);
                }

                Activity statusActivity = null;
                for (Activity activity : event.getNewValue()) {
                    if (!activity.isRich()
                            && activity.getType() == Activity.ActivityType.CUSTOM_STATUS) {
                        statusActivity = activity;

                        if (activity.getName().contains("https://soba.xyz/")) {
                            break;
                        }
                    }
                }

                if (statusActivity != null) {
                    checkStatus(event.getMember(), statusActivity.getName());
                }
            }
        });
    }

    private void checkStatus(Member member, @Nullable String status) {
        long memberIdLong = member.getIdLong();
        StatusUser statusUser = UserRepository.instance().findByDiscordId(memberIdLong);

        OnlineStatus onlineStatus = member.getOnlineStatus();
        boolean canRateLimit = RateLimitManager.instance().tryUse(memberIdLong);
        if (status != null && status.contains("https://soba.xyz/") && !statusUser.isStatusSet()) {
            //new value correct
            if (canRateLimit) {
                if (OffsetDateTime.now().isBefore(member.getTimeCreated().plusDays(7))) {
                    RateLimitManager.instance().increase(memberIdLong);
                    PrivateMessages.tryPrivateMessage(null, member, "Your account must be at least 7 days from creation to be eligible");
                    return;
                }
            }

            if (canRateLimit) {
                if (onlineStatus != OnlineStatus.INVISIBLE && onlineStatus != OnlineStatus.OFFLINE) {
                    EmbedBuilder embed = BotEmbedBuilder.createDefaultEmbed("Discord Status Streak", "Soba Discord Rewards");
                    embed.addField(
                            ":green_circle: **Success!**",
                            "You have just correctly set your status! At each milestone, you will receive a reward!\n" +
                                    "**BE AWARE**: If you __remove__ your status or stay offline/invisible, your progress will not count!", false
                    );

                    PrivateEmbedMessages.tryPrivateMessage(null, member, embed.build());
                    RateLimitManager.instance().increase(memberIdLong);
                }
            }

            statusUser.setStatusSet(true);
            UserRepository.instance().insert(statusUser);
        } else {
            if (statusUser.isStatusSet()) {
                if (canRateLimit) {
                    if (onlineStatus != OnlineStatus.INVISIBLE && onlineStatus != OnlineStatus.OFFLINE && (status == null || status.isEmpty())) {
                        EmbedBuilder embed = BotEmbedBuilder.createDefaultEmbed("Discord Status Streak", "Soba Discord Rewards");
                        embed.setColor(Color.RED);
                        embed.addField(":red_circle: **Caution!**", "Looks like you removed Soba status from your profile! You won't progress on your reward streak unless you add it again.", false);

                        PrivateEmbedMessages.tryPrivateMessage(null, member, embed.build());

                        RateLimitManager.instance().increase(memberIdLong);
                    }
                }

                statusUser.setStatusSet(false);
                UserRepository.instance().insert(statusUser);
            }
        }
    }

    public void loadStatusRewards() {
        statusRewardMap = new LinkedHashMap<>();
        String rewardsAsJson = bot.getConfig().getRewardsAsJson();

        Type listType = new TypeToken<ArrayList<StatusReward>>() {
        }.getType();
        List<StatusReward> statusRewards = new Gson().fromJson(rewardsAsJson, listType);

        statusRewards.forEach(statusReward -> statusRewardMap.put(statusReward.getId(), statusReward));
    }

    public void updateRewardsMessage() {
        TextChannel rewardsChannel = getRewardsChannel();

        List<Message> rewardsMessages = rewardsChannel.getHistory().retrievePast(100).complete();
        rewardsMessages.forEach(reward -> {
            if (!reward.getAuthor().isBot()) {
                reward.delete().complete();
            } else {
                if (!reward.getButtons().isEmpty()) {
                    reward.delete().complete();
                }
            }
        });

        rewardsChannel.sendMessageEmbeds(getRewardsMessage(rewardsChannel.getGuild().getIconUrl())).setActionRow(
                Button.of(ButtonStyle.SUCCESS, "rewardsStatus", "Your rewards").withEmoji(Emoji.fromUnicode("U+1F5D3")),
                Button.of(ButtonStyle.SECONDARY, "statusRewardInfo", "How to: Status Rewards").withEmoji(Emoji.fromUnicode("U+1F4DD"))
        ).complete();
    }

    public TextChannel getRewardsChannel() {
        return bot.getJda().getTextChannelById(bot.getConfig().getRewardsChannelId());
    }

    public MessageEmbed getRewardsMessage(String serverIconUrl) {
        EmbedBuilder embed = new EmbedBuilder();

        int count = 0;
        for (val rulesFields : bot.getConfig().getRewardsMessages().entrySet()) {
            embed.addField(rulesFields.getKey(), rulesFields.getValue(), false);
            count++;
            if (count < bot.getConfig().getRewardsMessages().size()) {
                embed.addBlankField(true);
            }

        }

        embed.setTitle("**Soba Discord Rewards** :gift:");
        embed.setColor(new Color(242, 105, 92));
        embed.setFooter("Soba Rewards", serverIconUrl);

        return embed.build();
    }

    public void sendRewardMessage(long userId, StatusReward statusReward) {
        getRewardsChannel().sendMessage(":gift: **<@" + userId + ">** just received an reward: " + statusReward.getRewardDesc()).queue();
        updateRewardsMessage();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.getComponentId().equals("statusRewardInfo")) {
            EmbedBuilder embed = BotEmbedBuilder.createDefaultEmbed("**Discord Status Rewards** :gift:", "Soba Discord Rewards");
            embed.setColor(Color.CYAN);
            embed.addField(":pencil: **HOW TO**", "Set your profile status as: \n-\n> https://soba.xyz/\n-\n and receive unique rewards for keeping your status unchanged!", false);

            StringBuilder rewards = new StringBuilder();
            for (StatusReward statusReward : statusRewardMap.values()) {
                rewards.append("> ")
                        .append(statusReward.getRewardDesc())
                        .append(" (**")
                        .append(statusReward.getMinutesWithStatus())
                        .append("m**)\n");
            }

            rewards.append("**BE AWARE**: If you __remove__ your status or stay offline/invisible, your progress will not count!");
            embed.addField(":gift: **REWARDS**", rewards.toString(), false);

            //PrivateEmbedMessages.tryPrivateMessage(null, event.getUser(), embed.build());

            //event.reply("**<@" + event.getUser().getId() + ">, follow the instructions sent by the Bot on your private inbox!**").setEphemeral(true).queue();
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        }

        if (event.getComponentId().equals("rewardsStatus")) {
            EmbedBuilder embed = BotEmbedBuilder.createDefaultEmbed("**Soba Discord Rewards** :gift:", "Soba Discord Rewards");
            embed.setColor(Color.CYAN);
            embed.addField(":small_red_triangle_down: **__YOUR STATUS__**", "\n", true);

            String timeElapsed = "N/A";
            boolean statusSet = false;

            StatusUser discordUser = UserRepository.instance().findByDiscordId(event.getUser().getIdLong());
            if (discordUser.isStatusSet()) {
                statusSet = true;
                timeElapsed = TimeUnit.MILLISECONDS.toHours(discordUser.getStatusSetInMillis()) + "h";
            }

            int rewardsReceived = discordUser.getReceivedRewardsIds().size();

            StringBuilder rewards = new StringBuilder();
            for (StatusReward statusReward : statusRewardMap.values()) {
                rewards.append("> ");

                if (discordUser.getReceivedRewardsIds().contains(statusReward.getId())) {
                    rewards.append(":white_check_mark: ");
                } else {
                    rewards.append(":x: ");
                }

                rewards.append(statusReward.getRewardDesc())
                        .append(" (**")
                        .append(statusReward.getMinutesWithStatus())
                        .append("m**)\n");
            }

            rewards.append("**BE AWARE**: If you __remove__ your status or stay offline/invisible, your progress will not count!");
            embed.addField(":calendar: **Status Rewards**",
                    "> Status Set: " + (statusSet ? "Yes" : "Pending") + "\n" +
                            "> Time Elapsed: " + timeElapsed + "\n" +
                            "> Rewards Received: " + rewardsReceived + "/" + statusRewardMap.size() + "\n" +
                            "> Rewards: \n" +
                            rewards,
                    false);

            //PrivateEmbedMessages.tryPrivateMessage(null, event.getUser(), embed.build());

            //event.reply("**<@" + event.getUser().getId() + ">, check your inbox!**").setEphemeral(true).queue();
            event.replyEmbeds(embed.build()).setEphemeral(true).queue();
        }
    }

}
