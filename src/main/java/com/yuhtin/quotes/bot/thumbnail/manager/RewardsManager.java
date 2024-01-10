package com.yuhtin.quotes.bot.thumbnail.manager;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.yuhtin.quotes.bot.thumbnail.ThumbnailBot;
import com.yuhtin.quotes.bot.thumbnail.config.Config;
import com.yuhtin.quotes.bot.thumbnail.model.StatusReward;
import com.yuhtin.quotes.bot.thumbnail.model.StatusUser;
import com.yuhtin.quotes.bot.thumbnail.repository.UserRepository;
import com.yuhtin.quotes.bot.thumbnail.util.BotEmbedBuilder;
import com.yuhtin.quotes.bot.thumbnail.util.PrivateEmbedMessages;
import com.yuhtin.quotes.bot.thumbnail.util.PrivateMessages;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.val;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.OnlineStatus;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.entities.emoji.Emoji;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.user.update.UserUpdateActivitiesEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import net.dv8tion.jda.api.interactions.components.buttons.ButtonStyle;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.Nonnull;
import java.awt.*;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.Type;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.concurrent.TimeUnit;

@NoArgsConstructor
@Getter
public class RewardsManager extends ListenerAdapter {

    private static final RewardsManager INSTANCE = new RewardsManager();

    private ThumbnailBot bot;
    private LinkedHashMap<String, StatusReward> statusRewardMap;

    public void init() {
        bot = ThumbnailBot.getInstance();

        loadStatusRewards();
        updateRewardsMessage();
    }

    @SubscribeEvent
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

    private void checkStatus(Member member, @Nullable String status) {
        long memberIdLong = member.getIdLong();
        StatusUser statusUser = UserRepository.instance().findByDiscordId(memberIdLong);

        Config config = ThumbnailBot.getInstance().getConfig();

        OnlineStatus onlineStatus = member.getOnlineStatus();
        boolean canRateLimit = RateLimitManager.instance().tryUse(memberIdLong);
        EmbedBuilder defaultEmbed = BotEmbedBuilder.createDefaultEmbed(config.getDefaultEmbedTitle(), config.getDefaultEmbedFooter());

        if (status != null && status.contains("https://soba.xyz/") && !statusUser.isStatusSet()) {
            //new value correct
            if (canRateLimit) {
                if (OffsetDateTime.now().isBefore(member.getTimeCreated().plusDays(7))) {
                    RateLimitManager.instance().increase(memberIdLong);
                    PrivateMessages.tryPrivateMessage(null, member, config.getNewAccountError());
                    return;
                }
            }

            if (canRateLimit) {
                if (onlineStatus != OnlineStatus.INVISIBLE && onlineStatus != OnlineStatus.OFFLINE) {
                    defaultEmbed.addField(
                            config.getSetupSuccessFieldName(),
                            config.getSetupSuccessFieldValue(),
                            false
                    );

                    PrivateEmbedMessages.tryPrivateMessage(null, member, defaultEmbed.build());
                    RateLimitManager.instance().increase(memberIdLong);
                }
            }

            statusUser.setStatusSet(true);
            UserRepository.instance().insert(statusUser);
        } else {
            if (statusUser.isStatusSet()) {
                if (canRateLimit) {
                    if (onlineStatus != OnlineStatus.INVISIBLE && onlineStatus != OnlineStatus.OFFLINE && (status == null || status.isEmpty())) {
                        defaultEmbed.setColor(Color.RED);
                        defaultEmbed.addField(
                                config.getSetupErrorFieldName(),
                                config.getSetupErrorFieldValue(),
                                false
                        );

                        PrivateEmbedMessages.tryPrivateMessage(null, member, defaultEmbed.build());

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
        String rewardsAsJson = ThumbnailBot.getInstance().getConfig().getRewardsAsJson();

        Type listType = new TypeToken<ArrayList<StatusReward>>() {
        }.getType();
        List<StatusReward> statusRewards = new Gson().fromJson(rewardsAsJson, listType);

        statusRewards.forEach(statusReward -> statusRewardMap.put(statusReward.getId(), statusReward));
    }

    public void updateRewardsMessage() {
        TextChannel rewardsChannel = getRewardsChannel();
        if (rewardsChannel == null) {
            ThumbnailBot.getInstance().getLogger().severe("Rewards channel not found!");
            return;
        }

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

        Config config = ThumbnailBot.getInstance().getConfig();
        rewardsChannel.sendMessageEmbeds(getRewardsMessage(config.getSobaIconUrl())).setActionRow(
                Button.of(ButtonStyle.SUCCESS, "rewardsStatus", config.getLookPlayerRewardsButton()).withEmoji(Emoji.fromUnicode("U+1F5D3")),
                Button.of(ButtonStyle.SECONDARY, "statusRewardInfo", config.getLookStatusRewardsButton()).withEmoji(Emoji.fromUnicode("U+1F4DD"))
        ).complete();
    }

    @Nullable
    public TextChannel getRewardsChannel() {
        return ThumbnailBot.getInstance().getJda().getTextChannelById(ThumbnailBot.getInstance().getConfig().getRewardsChannelId());
    }

    public MessageEmbed getRewardsMessage(String serverIconUrl) {
        EmbedBuilder embed = new EmbedBuilder();

        int count = 0;
        Config config = ThumbnailBot.getInstance().getConfig();
        for (val rulesFields : config.getRewardsMessages().entrySet()) {
            embed.addField(rulesFields.getKey(), rulesFields.getValue(), false);
            count++;
            if (count < config.getRewardsMessages().size()) {
                embed.addBlankField(true);
            }

        }

        embed.setTitle(config.getAnnounceRewardsEmbedTitle());
        embed.setColor(new Color(242, 105, 92));
        embed.setFooter(config.getDefaultEmbedFooter(), serverIconUrl);

        return embed.build();
    }

    public void giveReward(long userId, StatusReward statusReward) {
        File projectRootPath = new File(System.getProperty("user.dir"));
        File rewardFile = new File(projectRootPath, "received-rewards.json");
        if (!rewardFile.exists()) {
            try {
                rewardFile.createNewFile();
            } catch (Exception exception) {
                exception.printStackTrace();
            }
        }

        User userById = bot.getJda().getUserById(userId);
        if (userById != null) {
            try (FileWriter writer = new FileWriter(rewardFile, true)) {
                writer.write("User: @" + userById.getName() + " | User ID: " + userId + " | Reward: " + statusReward.getId() + "\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        sendRewardMessage(userId, statusReward);
    }

    public void sendRewardMessage(long userId, StatusReward statusReward) {
        TextChannel rewardsChannel = getRewardsChannel();
        if (rewardsChannel == null) {
            ThumbnailBot.getInstance().getLogger().severe("Rewards channel not found!");
            return;
        }

        Config config = ThumbnailBot.getInstance().getConfig();
        rewardsChannel.sendMessage(config.getUserReceivedReward()
                .replace("{user}", String.valueOf(userId))
                .replace("{reward}", statusReward.getRewardDesc())
        ).queue();

        updateRewardsMessage();
    }

    @Override
    public void onButtonInteraction(@NotNull ButtonInteractionEvent event) {
        Config config = ThumbnailBot.getInstance().getConfig();
        EmbedBuilder defaultEmbed = BotEmbedBuilder.createDefaultEmbed(config.getDefaultEmbedTitle(), config.getDefaultEmbedFooter());

        if (event.getComponentId().equals("statusRewardInfo")) {
            defaultEmbed.setColor(Color.CYAN);
            defaultEmbed.addField(config.getHowToSetStatusFieldName(), config.getHowToSetStatusFieldValue(), false);

            StringBuilder rewards = new StringBuilder();
            for (StatusReward statusReward : statusRewardMap.values()) {
                rewards.append("> ")
                        .append(statusReward.getRewardDesc())
                        .append(" (**")
                        .append(statusReward.getHoursWithStatus())
                        .append("m**)\n");
            }

            rewards.append(config.getBeawareField());
            defaultEmbed.addField(config.getRewardsFieldName(), rewards.toString(), false);

            event.replyEmbeds(defaultEmbed.build()).setEphemeral(true).queue();
        }

        if (event.getComponentId().equals("rewardsStatus")) {
            defaultEmbed.setColor(Color.CYAN);
            defaultEmbed.addField(config.getYourStatusFieldName(), config.getYourStatusFieldValue(), true);

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
                        .append(statusReward.getHoursWithStatus())
                        .append("m**)\n");
            }

            rewards.append(config.getBeawareField());
            defaultEmbed.addField(config.getStatusRewardsFieldName(),
                    config.getStatusRewardsFieldValue()
                            .replace("{isStatusSet}", statusSet ? "Yes" : "Pending")
                            .replace("{time}", timeElapsed)
                            .replace("{rewardsReceived}", String.valueOf(rewardsReceived))
                            .replace("{totalRewards}", String.valueOf(statusRewardMap.size()))
                            .replace("{rewards}", rewards),
                    false);

            event.replyEmbeds(defaultEmbed.build()).setEphemeral(true).queue();
        }
    }

    public static RewardsManager instance() {
        return INSTANCE;
    }

}
