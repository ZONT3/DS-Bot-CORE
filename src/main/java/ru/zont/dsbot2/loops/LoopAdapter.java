package ru.zont.dsbot2.loops;

import ru.zont.dsbot2.ErrorReporter;
import ru.zont.dsbot2.ZDSBot;

import java.util.Timer;
import java.util.TimerTask;

public abstract class LoopAdapter extends TimerTask {

    private final ZDSBot.GuildContext context;

    public LoopAdapter(ZDSBot.GuildContext context) {
        this.context = context;

        if (!runInGlobal() && context.getGuild() == null) return;
        if (!runInLocal() && context.getGuild() != null) return;

        Timer loop = new Timer("Loop " + getClass().getSimpleName(), true);
        loop.schedule(this, 0, getPeriod());
    }

    @Override
    public final void run() {
        try {
            loop();
        } catch (Throwable error) {
            ErrorReporter.inst().reportError(context, getClass(), error);
        }
    }

    protected final ZDSBot.GuildContext getContext() {
        return context;
    }

    public abstract void loop() throws Throwable;

    public abstract long getPeriod();

    public abstract boolean runInGlobal();

    public abstract boolean runInLocal();
}
