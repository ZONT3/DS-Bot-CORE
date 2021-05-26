package ru.zont.dsbot2.loops;

import ru.zont.dsbot2.ErrorReporter;
import ru.zont.dsbot2.ZDSBot;

import java.util.Timer;
import java.util.TimerTask;

public abstract class LoopAdapter extends TimerTask {

    private final ZDSBot.GuildContext context;
    private Timer timer;

    public LoopAdapter(ZDSBot.GuildContext context) {
        this.context = context;

        if (!runInGlobal() && context.getGuild() == null) return;
        if (!runInLocal() && context.getGuild() != null) return;

        prepare();
        setupTimer(true);
    }

    private void setupTimer(boolean instant) {
        long period = getPeriod();
        if (period <= 0) return;

        if (timer != null) timer.cancel();
        timer = new Timer("Loop " + getClass().getSimpleName(), true);
        timer.schedule(this, instant ? 0 : period, period);
    }

    @Override
    public final void run() {
        try {
            loop();
        } catch (Throwable error) {
            ErrorReporter.inst().reportError(context, getClass(), error);
        }
    }

    public final void consumeNext() {
        setupTimer(false);
    }

    protected final ZDSBot.GuildContext getContext() {
        return context;
    }

    public void prepare() { }

    public abstract void loop() throws Throwable;

    public abstract long getPeriod();

    public abstract boolean runInGlobal();

    public abstract boolean runInLocal();
}
