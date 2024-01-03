package com.yuhtin.quotes.bot.thumbnail.manager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yuhtin.quotes.bot.thumbnail.ThumbnailBot;
import com.yuhtin.quotes.bot.thumbnail.model.Manager;
import com.yuhtin.quotes.bot.thumbnail.model.StatusReward;
import lombok.Getter;
import lombok.val;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateActivitiesEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import net.dv8tion.jda.internal.utils.tuple.Pair;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nonnull;
import java.awt.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

@Getter
public class RewardsManager extends ListenerAdapter implements Manager {

    private final ThumbnailBot bot;
    private LinkedHashMap<String, StatusReward> statusRewardMap;

    public RewardsManager(ThumbnailBot bot) {
        this.bot = bot;
    }

    @Override
    public void initialize() {
        loadStatusRewards();
        updateRewardsMessage();

        bot.getJda().addEventListener(new ListenerAdapter() {

            @Override
            public void onUserUpdateActivities(@Nonnull UserUpdateActivitiesEvent event) {
                if (event.getNewValue() == null || event.getNewValue().isEmpty()) {
                    if (bot.getUserManager().isRegistered(mcPair.getLeft())) {
                        bot.getUserManager().checkStatus(mcPair.getLeft(), null, event.getUser());
                    }
                }

                Activity statusActivity = null;
                for (Activity activity : event.getNewValue()) {
                    if (!activity.isRich() && activity.getType() == Activity.ActivityType.CUSTOM_STATUS) {
                        if (bot.getUserManager().isRegistered(mcPair.getLeft())) {
                            statusActivity = activity;
                        } else {
                            bot.getUserManager().syncUserData(mcPair.getLeft());
                        }
                    }
                }

                if (statusActivity != null) {
                    if (bot.getUserManager().isRegistered(mcPair.getLeft())) {
                        bot.getUserManager().checkStatus(mcPair.getLeft(), statusActivity.getName(), event.getUser());
                    }
                }


            }
        });


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

        rewardsChannel.sendMessageEmbeds(getRewardsMessage(rewardsChannel.getGuild().getIconUrl()))
                .setActionRow(
                        Button.of(ButtonStyle.SUCCESS, "rewardsStatus", "Your rewards")
                                .withEmoji(Emoji.fromUnicode("U+1F5D3")),
                        Button.of(ButtonStyle.SECONDARY, "statusRewardInfo", "How to: Status Rewards")
                                .withEmoji(Emoji.fromUnicode("U+1F4DD"))
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


    public void sendRewardMessage(String username, String rewardId) {
        StatusReward statusReward = statusRewardMap.get(rewardId);
        if (statusReward != null) {
            getRewardsChannel().sendMessage(":gift: **" + username + "** just received an in-game reward: " + statusReward.getRewardDesc()).queue();
        }

        updateRewardsMessage();
    }


    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        if (event.getComponentId().equals("statusRewardInfo")) {

            EmbedBuilder embed = BotEmbedBuilder.createDefaultEmbed("**Discord Status Rewards** :gift:", "MineTr.ee Discord Rewards");
            embed.setColor(Color.CYAN);
            embed.addField(":pencil: **HOW TO**", "Set your profile status as: \n-\n> " + DiscordUserCommon.CUSTOM_STATUS + "\n-\n and receive unique rewards for keeping your status unchanged!", false);
            String rewards = "";
            for (StatusReward statusReward : TreeDiscordBot.rewardsManager.statusRewardMap.values()) {
                rewards += "> " + statusReward.getRewardDesc() + " (**" + statusReward.getHoursWithStatus() + "h** | _" + statusReward.getRealm() + "_)\n";


            }
            rewards += "**BE AWARE**: If you __remove__ your status or stay offline/invisible, your progress will not count!";
            embed.addField(":gift: **REWARDS**", rewards, false);
            PrivateEmbedMessages.tryPrivateMessage(null, event.getUser(),
                    embed.build());

            event.reply("**<@" + event.getUser().getId() + ">, follow the instructions sent by the Bot on your private inbox!**").setEphemeral(true).queue();

        }

        if (event.getComponentId().equals("rewardsStatus")) {
            Embed embed = BotEmbedBuilder.createDefaultEmbed("**MineTree Discord Rewards** :gift:", "MineTr.ee Discord Rewards");
            embed.setColor(Color.CYAN);
            embed.addField(":small_red_triangle_down: **__YOUR STATUS__**", "\n", true);
            Pair<UUID, String> userPair = TreeDiscordBot.syncManager.getLinkedMinecraftData(event.getUser().getId());
            boolean isSynched = userPair != null;
            boolean statusSet = false;
            DiscordUser discordUser = null;
            String timeElapsed = "N/A";
            int rewardsReceived = 0;
            String rewards = "";
            if (userPair != null) {
                discordUser = TreeDiscordBot.botDiscordUserManager.discordUserMap.get(userPair.getLeft());
                if (discordUser != null && discordUser.isStatusSet()) {
                    statusSet = true;
                    timeElapsed = String.valueOf(TimeUnit.MILLISECONDS.toHours(System.currentTimeMillis() - discordUser.getStatusSetTimestamp())) + "h";
                }
                if (discordUser != null) {
                    rewardsReceived = discordUser.getReceivedRewardsIds().stream().filter(received -> received.length() > 1).collect(Collectors.toList()).size();

                }
            }

            for (StatusReward statusReward : TreeDiscordBot.rewardsManager.statusRewardMap.values()) {
                if (discordUser != null && discordUser.getReceivedRewardsIds().contains(statusReward.getId())) {
                    rewards += "> :white_check_mark: " + statusReward.getRewardDesc() + " (**" + statusReward.getHoursWithStatus() + "h** | _" + statusReward.getRealm() + "_)\n";
                } else {
                    rewards += "> :x: " + statusReward.getRewardDesc() + " (**" + statusReward.getHoursWithStatus() + "h** | _" + statusReward.getRealm() + "_)\n";

                }
            }
            rewards += "**BE AWARE**: If you __remove__ your status or stay offline/invisible, your progress will not count!";
            embed.addField(":calendar: **Status Rewards**",
                    "> Account Sync: " + (isSynched ? "Enabled" : "Disabled") + "\n" +
                            "> Status Set: " + (statusSet ? "Yes" : "Pending") + "\n" +
                            "> Time Elapsed: " + timeElapsed + "\n" +
                            "> Rewards Received: " + rewardsReceived + "/" + BotSettings.REWARDS_HOURS.size() + "\n" +
                            "> Rewards: \n" +
                            rewards
                    , false);
            PrivateEmbedMessages.tryPrivateMessage(null, event.getUser(),
                    embed.build());

            event.reply("**<@" + event.getUser().getId() + ">, check your inbox!**").setEphemeral(true).queue();
        }
    }

    @Override
    public void shutdown() {

    }


}
