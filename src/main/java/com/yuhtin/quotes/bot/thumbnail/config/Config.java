package com.yuhtin.quotes.bot.thumbnail.config;

import com.google.common.collect.ImmutableMap;
import com.yuhtin.quotes.bot.thumbnail.util.Serializer;
import lombok.Getter;

import java.io.*;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

/**
 * @author <a href="https://github.com/Yuhtin">Yuhtin</a>
 */
@Getter
public class Config {

    private static final Logger logger = Logger.getLogger("ThumbnailBot");

    private String token = "none";

    private String rewardsAsJson = "[{\"id\": reward_1, \"minutesWithStatus\": 10, \"rewardDesc\": \"x1 **Nothing $-$**\"}," +
            "{\"id\": reward_2, \"minutesWithStatus\": 20, \"rewardDesc\": \"x3 **Nothing $-$**\"}]";

    private Map<String, String> rewardsMessages = ImmutableMap.of(
            ":pencil: **Discord Status Rewards**",
            "> Change your Discord profile status to the following message: \n'**https://soba.xyz/**'\n" +
                    "> and __instantly__ receive amazing rewards!\n" +
                    "\n" +
                    "Click the button bellow to receive the instructions on how to participate!\n");

    private long rewardsChannelId = 0L;

    private long sobaServerId = 0L;

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

                logger.severe("Config not found, creating a new config!");
                logger.severe("Put a valid token in the bot's config");
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
