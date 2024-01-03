package com.yuhtin.quotes.bot.thumbnail.util;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.User;
import net.dv8tion.jda.api.entities.channel.concrete.PrivateChannel;
import net.dv8tion.jda.api.entities.channel.middleman.MessageChannel;
import net.dv8tion.jda.api.exceptions.ContextException;
import net.dv8tion.jda.api.requests.restaction.MessageCreateAction;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Function;

public class PrivateEmbedMessages {

    private static final Map<Long, Long> LAST_TIMEOUT = new HashMap<>();

    public static void tryPrivateMessage(MessageChannel channel, Member recipient, MessageEmbed... messages) {
        tryPrivateMessage(channel, recipient, 15, messages);
    }

    public static void tryPrivateMessage(MessageChannel channel, Member recipient, int delay, MessageEmbed... messages) {
        tryPrivateMessage(channel, recipient, null, null, delay, messages);
    }

    public static void tryPrivateMessage(MessageChannel channel, Member recipient, Function<MessageCreateAction, MessageCreateAction> procedures, Consumer<Message> onSuccess, int delay, MessageEmbed... messages) {
        recipient.getUser().openPrivateChannel().queue(pm -> sendMessages(channel, recipient.getUser(), pm, procedures, onSuccess, null, messages, 0, delay), t -> {
            if (t instanceof net.dv8tion.jda.api.exceptions.ContextException)
                t.printStackTrace();
            timedMessagePrompt(channel, recipient.getUser(), delay);
        });
    }

    public static void tryPrivateMessage(MessageChannel channel, User recipient, Function<MessageCreateAction, MessageCreateAction> procedures, Consumer<Message> onSuccess, int delay, MessageEmbed... messages) {
        recipient.openPrivateChannel().queue(pm -> sendMessages(channel, recipient, pm, procedures, onSuccess, null, messages, 0, delay), t -> {
            if (t instanceof net.dv8tion.jda.api.exceptions.ContextException)
                t.printStackTrace();
            timedMessagePrompt(channel, recipient, delay);
        });
    }

    private static void sendMessages(MessageChannel channel, User recipient, PrivateChannel pm, Function<MessageCreateAction, MessageCreateAction> procedures, Consumer<Message> onSuccess, Runnable onFail, MessageEmbed[] messages, int index, int delay) {
        MessageCreateAction messageAction = pm.sendMessageEmbeds(messages[index]);
        if (procedures != null) messageAction = procedures.apply(messageAction);

        messageAction.queue(success -> {
            if (index + 1 >= messages.length) {
                if (onSuccess != null)
                    onSuccess.accept(success);

                return;
            }
            sendMessages(channel, recipient, pm, procedures, onSuccess, onFail, messages, index + 1, delay);
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
                        LAST_TIMEOUT.entrySet().removeIf(longLongEntry -> longLongEntry.getKey().equals(recipient.getIdLong()));
                    });
    }
}

