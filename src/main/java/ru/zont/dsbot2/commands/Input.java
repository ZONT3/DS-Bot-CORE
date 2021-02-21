package ru.zont.dsbot2.commands;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import ru.zont.dsbot2.ZDSBot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Input {
    private final MessageReceivedEvent event;
    private final ZDSBot.GuildContext gc;
    private final String content;

    public Input(MessageReceivedEvent event, ZDSBot.GuildContext gc, String content) {
        this.event = event;
        this.gc = gc;
        this.content = content;
    }

    public ZDSBot.GuildContext getContext() {
        return gc;
    }

    public String getContentRaw() {
        if (content != null) return content;
        return event.getMessage().getContentRaw();
    }

    public String getContentRaw(String[] args, int shift) {
        String pattern = "";
        int lastSize = 0;
        String contentRaw = getContentRaw();
        for (int i = 0, argsLength = args.length; i < argsLength && i < shift; i++) {
            String regex = ".*" + Pattern.quote(args[i]) + " *";
            Matcher matcher = Pattern.compile(regex).matcher(contentRaw);

            if (!matcher.find()) continue;
            int length = matcher.group().length();
            if (lastSize < length) {
                pattern = regex;
                lastSize = length;
            }
        }
        if (pattern.isEmpty()) return contentRaw;
        return contentRaw.replaceFirst(pattern, "");
    }

    public CommandLine getCommandLine() {
        return getCommandLine(new Options());
    }

    public CommandLine getCommandLine(Options options) {
        DefaultParser parser = new DefaultParser();
        try {
            return parser.parse(options, ArgumentTokenizer.tokenize(getContentRaw()).toArray(new String[0]));
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public MessageReceivedEvent getEvent() {
        return event;
    }

    public MessageChannel getChannel() {
        return event.getChannel();
    }
}
