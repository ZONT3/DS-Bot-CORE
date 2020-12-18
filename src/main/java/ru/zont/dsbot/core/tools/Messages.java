package ru.zont.dsbot.core.tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.*;

import java.awt.*;
import java.time.Instant;
import java.util.List;

public class Messages {

    public static final String EMOJI_OK = "\u2705";

    public static MessageEmbed error(String title, String description) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(description)
                .setColor(Color.RED)
                .build();
    }

    public static void printError(MessageChannel channel, String title, String description) {
        channel.sendMessage(error(title, description)).queue();
    }

    public static void tryPrintError(String title, String description, JDA jda) {
        TextChannel channel = null;
        try {
            channel = Tools.tryFindTChannel(Configs.getLogChannelID(), jda);
        } catch (Exception ignored) { }
        if (channel == null) {
            for (Guild guild: jda.getGuilds()) {
                channel = guild.getSystemChannel();
                if (channel != null) break;
            }
            if (channel == null) {
                for (Guild guild: jda.getGuilds()) {
                    channel = guild.getDefaultChannel();
                    if (channel != null) break;
                }
            }
        }
        if (channel != null) channel.sendMessage(
                new EmbedBuilder(error(title, description))
                        .setFooter(Strings.STR.getString("err.unexpected.foot"))
                        .build()).queue();
    }

    public static MessageEmbed addTimestamp(MessageEmbed e) {
        return addTimestamp(new EmbedBuilder(e));
    }

    public static MessageEmbed addTimestamp(EmbedBuilder builder) {
        return builder.setTimestamp(Instant.now()).build();
    }

    public static void appendDescriptionSplit(CharSequence append, List<EmbedBuilder> builders) {
        if (builders.size() == 0) return;
        EmbedBuilder toAppend = builders.get(builders.size() - 1);

        try {
            toAppend.appendDescription(append);
        } catch (IllegalArgumentException e) {
            if (append == null) throw e;
            builders.add(new EmbedBuilder().setColor(toAppend.build().getColor()).appendDescription(append));
        }
    }

    public static void sendSplit(MessageChannel channel, List<EmbedBuilder> builders, boolean timestamp) {
        for (EmbedBuilder builder: builders)
            channel.sendMessage(
                    timestamp
                    ? Messages.addTimestamp(builder)
                    : builder.build()
            ).complete();
    }

    public static void sendSplit(MessageChannel channel, List<EmbedBuilder> builders) {
        sendSplit(channel, builders, false);
    }

    public static String describeException(Throwable e) {
        return describeException(null, e);
    }

    public static String describeException(Class<?> klass, Throwable e) {
        String localizedMessage = e.getLocalizedMessage();
        return (klass != null ? (klass.getSimpleName() + ": ") : "") + e.getClass().getSimpleName() + (localizedMessage == null ? "" : ": " + localizedMessage);
    }

    public static void addOK(Message msg) {
        msg.addReaction(EMOJI_OK).queue();
    }

    public static Message pushEveryone(MessageEmbed build) {
        return new MessageBuilder()
                .append("@everyone")
                .setEmbed(build)
                .build();
    }
}
