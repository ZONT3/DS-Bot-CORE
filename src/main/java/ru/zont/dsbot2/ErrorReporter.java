package ru.zont.dsbot2;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

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

    public void reportError(TextChannel logChannel, ZDSBot.GuildContext context, Class<?> klass, Throwable error) {
        String identity = String.format("%s::%s",
                klass.getName(),
                error.getClass().getName());
        ReportedError re = reportedMap.getOrDefault(identity, null);

        if (logChannel == null) {
            LOG.info("logChannel for guild {} not stated",
                    context.getGuild() == null ? "null" : context.getGuild().getName());
            logChannel = context.getTChannel(context.getGlobalConfig().channel_log.get());
        }
        try {
            if (logChannel != null) {
                if (re == null || re.count() < 1) {
                    Message msg = logChannel.sendMessage(getError(klass, error)).complete();
                    re = new ReportedError(msg);
                } else {
                    try { re.msg = logChannel.retrieveMessageById(re.msg.getId()).complete(); }
                    catch (Throwable e) { e.printStackTrace(); re.msg = null; }
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
                if (re == null || re.count() < 1)
                    re = new ReportedError(null);
                else re.count();
            }

            reportedMap.put(identity, re);
        } catch (Throwable t) {
            t.printStackTrace();
            LOG.error("Error occurred while posting an error :/");
        }

        error.printStackTrace();
    }

    @NotNull
    private static MessageEmbed getError(Class<?> klass, Throwable error) {
        return error(
                STR.getString("err.unexpected"),
                describeException(klass, error));
    }

    private static class ReportedError {
        private final ArrayList<Long> reported;
        private Message msg;
        private long upd = 0;

        public ReportedError(Message msg) {
            this.msg = msg;
            reported = new ArrayList<>();
            reported.add(System.currentTimeMillis());
        }

        private long update() {
            long current = System.currentTimeMillis();
            if (current - upd >= 2000) {
                reported.removeIf(l -> current - l > PERIOD);
                upd = current;
            }
            return current;
        }

        public void increment() {
            long current = update();
            reported.add(current);
        }

        public int count() {
            update();
            return reported.size();
        }
    }
}
