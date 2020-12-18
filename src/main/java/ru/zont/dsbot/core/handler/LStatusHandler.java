package ru.zont.dsbot.core.handler;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import ru.zont.dsbot.core.ZDSBot;
import ru.zont.dsbot.core.tools.Tools;

import javax.annotation.Nonnull;


public abstract class LStatusHandler extends ListenerAdapter {
    private CallerThread callerThread;
    private final ZDSBot bot;

    public abstract void prepare(ReadyEvent event) throws Exception;
    public abstract void update() throws Exception;
    public abstract long getPeriod();

    public LStatusHandler(ZDSBot bot) {
        this.bot = bot;
    }

    public ZDSBot getBot() {
        return bot;
    }

    public JDA getJda() {
        return bot.jda;
    }

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        try {
            prepare(event);
        } catch (Throwable e) {
            Tools.reportError(e, getClass(), bot.jda);
        }

        if (getPeriod() <= 0) return;
        callerThread = new CallerThread();
        callerThread.start();
    }

    public CallerThread getCallerThread() { return callerThread; }

    @NotNull
    public GuildChannel tryFindChannel(String channelID) throws NullPointerException {
        return Tools.tryFindChannel(channelID, bot.jda);
    }

    @NotNull
    public MessageChannel tryFindTChannel(String channelID) throws NullPointerException {
        return Tools.tryFindTChannel(channelID, bot.jda);
    }

    @SuppressWarnings("BusyWait")
    private class CallerThread extends Thread {

        private CallerThread() {
            super("SH$CT-" + LStatusHandler.this.getClass().getSimpleName());
        }

        @Override
        public void run() {
            while (!interrupted()) {
                try { update(); }
                catch (Exception e) {
                    Tools.reportError(e, getClass(), bot.jda);
                }
                try { sleep(getPeriod()); }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
