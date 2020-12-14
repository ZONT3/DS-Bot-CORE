package ru.zont.dsbot.core.handler;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import ru.zont.dsbot.core.tools.Tools;

import javax.annotation.Nonnull;


public abstract class LStatusHandler extends ListenerAdapter {
    private CallerThread callerThread;
    private JDA jda;

    public abstract void prepare(ReadyEvent event) throws Exception;
    public abstract void update() throws Exception;
    public abstract long getPeriod();

    @Override
    public void onReady(@Nonnull ReadyEvent event) {
        jda = event.getJDA();
        try {
            prepare(event);
        } catch (Throwable e) {
            Tools.reportError(e, getClass(), getJda());
        }
        callerThread = new CallerThread();
        callerThread.start();
    }

    public JDA getJda() { return jda; }

    public CallerThread getCallerThread() { return callerThread; }

    @NotNull
    public GuildChannel tryFindChannel(String channelID) throws NullPointerException {
        return Tools.tryFindChannel(channelID, getJda());
    }

    @NotNull
    public MessageChannel tryFindTChannel(String channelID) throws NullPointerException {
        return Tools.tryFindTChannel(channelID, getJda());
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
                    Tools.reportError(e, getClass(), getJda());
                }
                try { sleep(getPeriod()); }
                catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
