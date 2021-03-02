package ru.zont.dsbot2.tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.MessageBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import static ru.zont.dsbot2.tools.ZDSBStrings.STR;

public class ZDSBMessages {
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

    public static MessageEmbed addTimestamp(MessageEmbed e) {
        return addTimestamp(new EmbedBuilder(e));
    }

    public static MessageEmbed addTimestamp(EmbedBuilder builder) {
        return builder.setTimestamp(Instant.now()).build();
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

    /**
     * Adds text before embed
     * @param build Embed
     * @param delim Delimiter for strings
     * @param prefixStrings Text strings
     * @return Message with text and embed
     */
    public static Message wrapEmbed(MessageEmbed build, String delim, String... prefixStrings) {
        return new MessageBuilder()
                .append(String.join(delim, prefixStrings))
                .setEmbed(build)
                .build();
    }

    public static MessageEmbed notImplemented() {
        return notImplemented(STR.getString("err.cannot_complete"));
    }

    public static MessageEmbed notImplemented(String title) {
        return new EmbedBuilder()
                .setTitle(title)
                .setDescription(STR.getString("err.not_implemented"))
                .setColor(0xC2185B)
                .build();
    }

    public static void sendSplit(MessageChannel channel, List<EmbedBuilder> builders, boolean timestamp) {
        for (EmbedBuilder builder: builders)
            channel.sendMessage(
                    timestamp
                            ? addTimestamp(builder)
                            : builder.build()
            ).complete();
    }

    public static void sendSplit(MessageChannel channel, List<EmbedBuilder> builders) {
        sendSplit(channel, builders, false);
    }

    public static void appendDescriptionSplit(CharSequence append, List<EmbedBuilder> builders) {
        if (builders.size() == 0) return;
        EmbedBuilder toAppend = builders.get(builders.size() - 1);

        try {
            toAppend.appendDescription(append);
        } catch (IllegalArgumentException e) {
            if (append == null) throw e;
            MessageEmbed.Footer footer = toAppend.build().getFooter();
            List<String> strings = splitString(append, MessageEmbed.TEXT_MAX_LENGTH - 5 -
                    (footer == null || footer.getText() == null ? 0 : footer.getText().length()));
            for (String string: strings)
                builders.add(new EmbedBuilder().setColor(toAppend.build().getColor()).appendDescription(string));
        }
    }


    @NotNull
    public static List<String> splitString(CharSequence nextLines, int length) {
        String text = nextLines.toString();
        List<String> ret = new ArrayList<>((text.length() + length - 1) / length);
        for (int start = 0; start < text.length(); start += length)
            ret.add(text.substring(start, Math.min(text.length(), start + length)));
        return ret;
    }
}
