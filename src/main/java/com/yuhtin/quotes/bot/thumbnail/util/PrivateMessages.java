package com.yuhtin.quotes.bot.thumbnail.util;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.ContextException;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PrivateMessages {
  private static final Map<Long, Long> LAST_TIMEOUT = new HashMap<>();

    public static void tryPrivateMessage(MessageChannel channel, Member recipient, String... messages) {
        tryPrivateMessage(channel, recipient, 15, messages);
    }

    public static void tryPrivateMessage(MessageChannel channel, Member recipient, Runnable onSuccess, String... messages) {
        tryPrivateMessage(channel, recipient, onSuccess, 15, messages);
    }

    public static void tryPrivateMessage(MessageChannel channel, User recipient, String... messages) {
        tryPrivateMessage(channel, recipient, 15, messages);
    }

    public static void tryPrivateMessage(MessageChannel channel, User recipient, Runnable onSuccess, String... messages) {
        tryPrivateMessage(channel, recipient, onSuccess, 15, messages);
    }

    public static void tryPrivateMessage(MessageChannel channel, Member recipient, int delay, String... messages) {
        tryPrivateMessage(channel, recipient, null, delay, messages);
    }

    public static void tryPrivateMessage(MessageChannel channel, Member recipient, Runnable onSuccess, int delay, String... messages) {
        recipient.getUser().openPrivateChannel().queue(pm -> sendMessages(channel, recipient.getUser(), pm, onSuccess, null, messages, 0, delay), t -> {
            if (t instanceof net.dv8tion.jda.api.exceptions.ContextException)
                t.printStackTrace();
            timedMessagePrompt(channel, recipient.getUser(), delay);
        });
    }

    public static void tryPrivateMessage(MessageChannel channel, User recipient, int delay, String... messages) {
        tryPrivateMessage(channel, recipient, null, delay, messages);
    }

    public static void tryPrivateMessage(MessageChannel channel, User recipient, Runnable onSuccess, int delay, String... messages) {
        recipient.openPrivateChannel().queue(pm -> sendMessages(channel, recipient, pm, onSuccess, null, messages, 0, delay), t -> {
            if (t instanceof net.dv8tion.jda.api.exceptions.ContextException)
                t.printStackTrace();
            timedMessagePrompt(channel, recipient, delay);
        });
    }

    public static void tryPrivateMessage(MessageChannel channel, User recipient, Runnable onSuccess, Runnable onFail, int delay, String... messages) {
        recipient.openPrivateChannel().queue(pm -> sendMessages(channel, recipient, pm, onSuccess, onFail, messages, 0, delay), t -> {
            if (t instanceof ContextException)
                t.printStackTrace();
            if (onFail != null)
                onFail.run();
            timedMessagePrompt(channel, recipient, delay);
        });
    }

    private static void sendMessages(MessageChannel channel, User recipient, PrivateChannel pm, Runnable onSuccess, Runnable onFail, String[] messages, int index, int delay) {
        pm.sendMessage(messages[index]).queue(success -> {
            if (index + 1 >= messages.length) {
                if (onSuccess != null)
                    onSuccess.run();
                return;
            }
            sendMessages(channel, recipient, pm, onSuccess, onFail, messages, index + 1, delay);
        }, failure -> {
            if (failure instanceof ContextException)
                failure.printStackTrace();
            if (onFail != null)
                onFail.run();
            timedMessagePrompt(channel, recipient, delay);
        });
    }

    private static void sendEmbedMessages(MessageChannel channel, User recipient, PrivateChannel pm, Runnable onSuccess, Runnable onFail, MessageEmbed[] messages, int index, int delay) {
        pm.sendMessageEmbeds(messages[index]).queue(success -> {
            if (index + 1 >= messages.length) {
                if (onSuccess != null)
                    onSuccess.run();
                return;
            }
            sendEmbedMessages(channel, recipient, pm, onSuccess, onFail, messages, index + 1, delay);
        }, failure -> {
            if (failure instanceof ContextException)
                failure.printStackTrace();
            if (onFail != null)
                onFail.run();
            timedMessagePrompt(channel, recipient, delay);
        });
    }

    private static void timedMessagePrompt(MessageChannel channel, User recipient, int delay) {
        Long lastTimeout = LAST_TIMEOUT.get(recipient.getIdLong());
        if (lastTimeout != null && System.currentTimeMillis() - lastTimeout < (delay * 1000L))
            return;
        LAST_TIMEOUT.put(recipient.getIdLong(), System.currentTimeMillis());
        if (channel != null)
            channel.sendMessage("<@" + recipient.getId() + "> please turn on private messages from our server!")
                    .queue(message -> {
                        message.delete().queueAfter(delay, TimeUnit.SECONDS);
                        LAST_TIMEOUT.entrySet().removeIf(longLongEntry -> longLongEntry.equals(recipient.getIdLong()));
                    });
    }
}

