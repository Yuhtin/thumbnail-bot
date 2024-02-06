package com.yuhtin.quotes.bot.thumbnail.config;

import com.google.common.collect.ImmutableMap;
import com.yuhtin.quotes.bot.thumbnail.util.Serializer;
import lombok.Getter;

import java.io.*;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author <a href="https://github.com/Yuhtin">Yuhtin</a>
 */
@Getter
public class Config {

    private static final Logger LOGGER = Logger.getLogger("ThumbnailBot");

    private String token = "none";

    private String neededStatus = "I'm making fun games - www.soba.xyz";

    private String fixedMessage = ":pencil: Use /thumnail to vote in the best thumbnail!";
    private String fixedMessage2 = ":pencil: Use /thumnail to vote in the best thumbnail!";

    private long fixedMessageChannelId = 0L;
    private long fixedMessageChannelId2 = 0L;

    private String rewardsAsJson = "[{\"id\": reward_1, \"hoursWithStatus\": 2, \"rewardDesc\": \"x1 **Nothing $-$**\"}," +
            "{\"id\": reward_2, \"hoursWithStatus\": 4, \"rewardDesc\": \"x3 **Nothing $-$**\"}]";

    private Map<String, String> rewardsMessages = ImmutableMap.of(
            ":pencil: **Discord Status Rewards**",
            "> Change your Discord profile status to the following message: \n'**https://soba.xyz/**'\n" +
                    "> and __instantly__ receive amazing rewards!\n" +
                    "\n" +
                    "Click the button bellow to receive the instructions on how to participate!\n");

    private long rewardsChannelId = 0L;

    private long sobaServerId = 0L;

    private String sobaIconUrl = "https://cdn.discordapp.com/icons/844543952563863553/6c368627a8bac85d0b5e2d36a956925d.webp?size=96";

    private String thumbnailEmbedTitle = "Thumbnail battle!";
    private String thumbnailEmbedDescription = "Vote on the best thumbnail bellow or check the thumbnail rank by using /trank\n";

    private String newAccountError = "Your account must be at least 7 days from creation to be eligible";

    private String announceRewardsEmbedTitle = "**Soba Discord Rewards** :gift:";

    private String defaultEmbedTitle = "Discord Status Rewards";
    private String defaultEmbedFooter = "Soba Discord Rewards";

    private String setupSuccessFieldName = ":green_circle: **Success!**";
    private String setupSuccessFieldValue = "You have just correctly set your status! At each milestone, you will receive a reward!\n" +
            "**BE AWARE**: If you __remove__ your status or stay offline/invisible, your progress will not count!";

    private String setupErrorFieldName = ":red_circle: **Caution!**";
    private String setupErrorFieldValue = "Looks like you removed Soba status from your profile! You won't progress on your reward unless you add it again.";

    private String lookPlayerRewardsButton = "Your rewards";
    private String lookStatusRewardsButton = "How to: Status Rewards";

    private String howToSetStatusFieldName = ":pencil: **HOW TO**";
    private String howToSetStatusFieldValue = "Set your profile status as: \n-\n> https://soba.xyz/\n-\n and receive unique rewards for keeping your status unchanged!";

    private String beawareField = "**BE AWARE**: If you __remove__ your status or stay offline/invisible, your progress will not count!";
    private String rewardsFieldName = ":gift: **REWARDS**";

    private String yourStatusFieldName = ":small_red_triangle_down: **__YOUR STATUS__**";
    private String yourStatusFieldValue = "\n";

    private String statusRewardsFieldName = ":calendar: **Status Rewards**";
    private String statusRewardsFieldValue = "> Status Set: {isStatusSet}\n" +
            "> Time Elapsed: {time}\n" +
            "> Rewards Received: {rewardsReceived}/{totalRewards}\n" +
            "> Rewards: \n" +
            "{rewards}";


    private String userReceivedReward = ":gift: **<@{user}>** just received an reward: {reward}";

    private String rewardsImageUrl = "https://i.imgur.com/Iae1SGf.png";

    public static Config loadConfig(String path) {
        try {
            File file = new File(path);
            if (!file.exists()) {

                if (!file.createNewFile()) return null;

                Config config = new Config();
                try (BufferedWriter writer = new BufferedWriter(new FileWriter(file.getAbsolutePath()))) {
                    writer.write(Serializer.CONFIG.serialize(config));
                    writer.newLine();
                    writer.flush();
                }

                LOGGER.severe("Config not found, creating a new config!");
                LOGGER.severe("Put a valid token in the bot's config");
                return null;
            }

            String line;
            StringBuilder responseContent = new StringBuilder();
            try (BufferedReader reader = new BufferedReader(new FileReader(file.getAbsolutePath()))) {
                while ((line = reader.readLine()) != null) responseContent.append(line);
            }

            return Serializer.CONFIG.deserialize(responseContent.toString());
        } catch (Exception exception) {
            return null;
        }
    }

}
