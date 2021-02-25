package ru.zont.dsbot2.tools;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.entities.MessageEmbed;
import ru.zont.dsbot2.NotImplementedException;

import java.util.ArrayList;
import java.util.function.Consumer;

/**
 * <b>Not yet tested nor done</b>
 */
public class EmbedBuilderSplitter {
    private final ArrayList<EmbedBuilder> list = new ArrayList<>();
    private final String title;
    private final String url;

    public EmbedBuilderSplitter(String title) {
        this(title, null);
    }

    public EmbedBuilderSplitter(String title, String url) {
        if (title == null) throw new IllegalArgumentException("Title should not be null!");

        list.add(new EmbedBuilder().setTitle(title, url));
        this.title = title;
        this.url = url;
    }

    /**
     * Do something with a builder.
     * If after this operation, builder becomes longer than max length,
     * this operation is applied to a new builder.
     * @param consumer Operation
     * @throws IllegalArgumentException if that operation is invalid to empty builder as well
     */
    public void o(Consumer<EmbedBuilder> consumer) {
        try {
            EmbedBuilder original = list.get(list.size() - 1);
            EmbedBuilder builder = new EmbedBuilder(original);
            consumer.accept(builder);
            if (builder.length() > MessageEmbed.EMBED_MAX_LENGTH_BOT)
                throw new IllegalStateException("Overall Length");

            consumer.accept(original);
        } catch (IllegalArgumentException | IllegalStateException e) {
            try {
                EmbedBuilder builder = new EmbedBuilder().setTitle(title, url);
                consumer.accept(builder);
                if (builder.length() > MessageEmbed.EMBED_MAX_LENGTH_BOT)
                    throw new IllegalStateException("Overall Length");

                list.add(builder);
            } catch (IllegalArgumentException | IllegalStateException ee) {
                throw new IllegalArgumentException(ee);
            }

        }
    }

    public void appendDescription(String s) {
        Commons.splitLength(s, MessageEmbed.TEXT_MAX_LENGTH);
        throw new NotImplementedException();
    }

    public ArrayList<EmbedBuilder> getList() {
        return new ArrayList<>(list);
    }

    public void sendAll(MessageChannel channel) {
        for (EmbedBuilder builder: list) {
            channel.sendMessage(builder.build()).complete();
        }
    }

}
