package com.yuhtin.quotes.bot.thumbnail.command.impl;

import com.google.gson.TypeAdapter;
import com.yuhtin.quotes.bot.thumbnail.http.impl.ProfilesRanksRequest;
import com.yuhtin.quotes.bot.thumbnail.model.ProfileRankData;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.hooks.SubscribeEvent;
import net.dv8tion.jda.api.interactions.InteractionHook;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.logging.Logger;

public class ProfileCommand extends ListenerAdapter {

    private static final long UPDATE_DELAY = TimeUnit.MINUTES.toMillis(3);
    private final HashMap<String, String> cache = new HashMap<>();

    private int byteSize = 0;
    private long lastUpdate = 0;

    @SubscribeEvent
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equalsIgnoreCase("profile")) return;

        OptionMapping option = event.getOption("username");
        if (option == null) {
            event.getInteraction().reply("You need to provide a username!").setEphemeral(true).queue();
            return;
        }

        String username = option.getAsString();
        if (username.contains(" ")) {
            event.getInteraction().reply("Username cannot contain spaces!").setEphemeral(true).queue();
            return;
        }

        if (lastUpdate == 0 || System.currentTimeMillis() - lastUpdate > UPDATE_DELAY) {
            event.getInteraction().reply("Updating data, please wait...")
                    .queue(message -> update().thenAccept($ -> searchProfile(username, message)));

            return;
        }

        event.getInteraction().reply("Searching profile, please wait...")
                .queue(message -> searchProfile(username, message));
    }

    private void searchProfile(String username, InteractionHook message) {
        String sha256 = cache.get(username.toLowerCase());
        if (sha256 == null) {
            message.editOriginal("The games from this username need a few more visits. Keep working on it!").queue();
            return;
        }

        message.editOriginal("You have a shining profile <:soba:898148685039415297>: https://soba.xyz/profiles/" + sha256).queue();
    }

    private CompletableFuture<Void> update() {
        ProfilesRanksRequest request = new ProfilesRanksRequest();
        return request.task().thenAccept(response -> {
            if (response == null) return;
            if (response.getStatusCode() != 200) return;

            String body = response.getBody();
            if (body == null || byteSize == body.getBytes().length) return;

            adaptData(body);

            byteSize = body.getBytes().length;
            lastUpdate = System.currentTimeMillis();
        });
    }

    private void adaptData(String body) {
        TypeAdapter<List<ProfileRankData>> data = ProfileRankData.ADAPTER;

        try {
            List<ProfileRankData> profiles = data.fromJson(body);
            Map<String, ProfileRankData> uniqueProfiles = new HashMap<>();

            for (ProfileRankData profile : profiles) {
                ProfileRankData existingProfile = uniqueProfiles.get(profile.getDisplay_name().toLowerCase());

                if (existingProfile == null || profile.getRank() < existingProfile.getRank()) {
                    if (existingProfile != null) {
                        Logger logger = Logger.getLogger("ThumbnailBot");
                        logger.info("Replacing " + existingProfile.getDisplay_name() + " with " + profile.getDisplay_name() + " because of rank!");
                        logger.info("Old rank: " + existingProfile.getRank() + " New rank: " + profile.getRank());
                    }

                    uniqueProfiles.put(profile.getDisplay_name().toLowerCase(), profile);
                }
            }

            List<ProfileRankData> filteredProfiles = new ArrayList<>(uniqueProfiles.values());
            cache.clear();

            for (ProfileRankData profile : filteredProfiles) {
                try {
                    String transformed = transformToSha256(profile.getUser_uuid());
                    String firstLetters = transformed.substring(0, 7);

                    cache.put(profile.getDisplay_name().toLowerCase(), firstLetters);
                } catch (Exception exception) {
                    exception.printStackTrace();
                }
            }
        } catch (Exception exception) {
            exception.printStackTrace();
        }
    }

    public String transformToSha256(String input) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder();

            for (byte b : hash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) hexString.append('0');

                hexString.append(hex);
            }

            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException(e);
        }
    }

}
