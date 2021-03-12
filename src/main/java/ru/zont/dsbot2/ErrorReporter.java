package ru.zont.dsbot2;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;

import static ru.zont.dsbot2.tools.ZDSBMessages.*;
import static ru.zont.dsbot2.tools.ZDSBStrings.*;

public class ErrorReporter {
    private static final Logger LOG = LoggerFactory.getLogger(ZDSBot.class);

    private static final long PERIOD = 10 * 60 * 1000;

    private final HashMap<String, ReportedError> reportedMap = new HashMap<>();

    private ErrorReporter() { }

    private static final ErrorReporter instance = new ErrorReporter();

    public static ErrorReporter inst() {
        return instance;
    }

    public synchronized void reportError(ZDSBot.GuildContext context, Class<?> klass, Throwable error) {
        TextChannel logChannel = context.getTChannel(context.getConfig().channel_log.get());
        reportError(logChannel, context, klass, error);
    }

    public synchronized void reportError(TextChannel logChannel, ZDSBot.GuildContext context, Class<?> klass, Throwable error) {
        String identity = String.format("%s::%s",
                klass.getName(),
                error.getClass().getName());
        ReportedError re = reportedMap.getOrDefault(identity, null);

        if (re != null && re.count() < 1) {
            reportedMap.remove(identity);
            re = null;
        }

        if (logChannel == null) {
            LOG.info("logChannel for guild {} not stated",
                    context.getGuild() == null ? "null" : context.getGuild().getName());
            logChannel = context.getTChannel(context.getGlobalConfig().channel_log.get());
        }
        try {
            if (logChannel != null) {
                if (re == null) {
                    Message msg = logChannel.sendMessage(getError(klass, error)).complete();
                    re = new ReportedError(msg);
                } else {
                    try { re.msg = logChannel.retrieveMessageById(re.msg.getId()).complete(); }
                    catch (Throwable e) { ErrorReporter.printStackTrace(e, getClass()); re.msg = null; }
                    List<MessageEmbed> embeds;
                    if (re.msg == null || (embeds = re.msg.getEmbeds()).size() <= 0)
                        embeds = Collections.singletonList(getError(klass, error));

                    re.increment();
                    MessageEmbed embed = new EmbedBuilder(
                            embeds.get(0)).setFooter(String.format(STR.getString("err.multiple"),
                                    getPlural(re.count(),
                                            STR.getString("plurals.count.other"),
                                            STR.getString("plurals.count.few"),
                                            STR.getString("plurals.count.other")
                                    )
                            )
                    ).build();

                    if (re.msg == null)
                        re.msg = logChannel.sendMessage(embed).complete();
                    else re.msg.editMessage(embed).complete();
                }
            } else {
                LOG.info("for the whole bot, too.");
                if (re == null)
                    re = new ReportedError(null);
                else re.count();
            }

            reportedMap.put(identity, re);
        } catch (Throwable t) {
            ErrorReporter.printStackTrace(t, getClass());
            LOG.error("Error occurred while posting an error :/");
        }

        ErrorReporter.printStackTrace(error, getClass());
    }

    public static void printStackTrace(Throwable e, Class<?> klass) {
        e.printStackTrace();
        StringWriter sw = new StringWriter();
        e.printStackTrace(new PrintWriter(sw));
        LoggerFactory.getLogger(klass).error(sw.toString());
    }

    @NotNull
    private static MessageEmbed getError(Class<?> klass, Throwable error) {
        return error(
                STR.getString("err.unexpected"),
                describeException(klass, error));
    }

    private static class ReportedError {
        private int reported;
        private Message msg;
        private long upd = 0;

        public ReportedError(Message msg) {
            this.msg = msg;
            increment();
        }

        private void upd() {
            long current = System.currentTimeMillis();
            if (current - upd > PERIOD)
                reported = 0;
            upd = System.currentTimeMillis();
        }

        public void increment() {
            upd();
            reported++;
        }

        public int count() {
            upd();
            return reported;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            ReportedError that = (ReportedError) o;
            return reported == that.reported && upd == that.upd && msg.getId().equals(that.msg.getId());
        }

        @Override
        public int hashCode() {
            return Objects.hash(reported, upd, msg.getId());
        }
    }
}
