package ru.zont.dsbot2.commands;

import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;
import ru.zont.dsbot2.ZDSBot;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.zont.dsbot2.commands.ArgumentTokenizer.*;

public class Input {
    private final MessageReceivedEvent event;
    private final ZDSBot.GuildContext gc;
    private final String content;
    private CommandLine blankCL = null;

    public Input(MessageReceivedEvent event, ZDSBot.GuildContext gc, String content) {
        this.event = event;
        this.gc = gc;
        this.content = content;
    }

    public ZDSBot.GuildContext getContext() {
        return gc;
    }

    public String get() {
        return getContentRaw();
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
        return getCommandLine(options, false);
    }

    public CommandLine getCommandLine(Options options, boolean onlySpecified) {
        DefaultParser parser = new DefaultParser();
        try {
            return parser.parse(options,
                    tokenize(getContentRaw()).toArray(new String[0]),
                    onlySpecified);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
    }

    public String getArg(int i) {
        if (blankCL == null)
            blankCL = getCommandLine();
        String[] args = blankCL.getArgs();
        if (i >= args.length) return null;
        return args[i];
    }

    public MessageReceivedEvent getEvent() {
        return event;
    }

    public MessageChannel getChannel() {
        return event.getChannel();
    }

    public Message getMessage() {
        return event.getMessage();
    }

    public Member getMember() {
        return event.getMember();
    }

    public String stripPrefixOpts() {
        return getContentRaw()
                .replaceFirst("(--?[^ ]+ +)*", "")
                .replaceFirst("(\"--?[^\"]+\" +)", "");
    }
}
