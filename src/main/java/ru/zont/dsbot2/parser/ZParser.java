package ru.zont.dsbot2.parser;

import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zont.dsbot2.ErrorReporter;
import ru.zont.dsbot2.ZDSBot;
import ru.zont.dsbot2.tools.DataList;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

public abstract class ZParser<T extends ZParserElement> {
    private static final Logger LOG = LoggerFactory.getLogger(ZParser.class);

    private final ZDSBot.GuildContext context;

    private final DataList<String> data;
    private boolean waitNext = false;

    public ZParser(ZDSBot.GuildContext context) {
        this.context = context;

        data = new DataList<>(getName());
        setup();
    }

    private void setup() {
        final String name = getName();
        if (data.getData().isEmpty()) {
            final List<T> retrieve = wRetrieve();
            if (retrieve == null) return;
            if (!retrieve.isEmpty()) {
                if (!tryUpdate(Collections.singletonList(retrieve.get(0))).isEmpty())
                    data.op(list -> retrieve.parallelStream().forEach(t -> list.add(t.getSignature())));
                LOG.info("Parser '{}'s initial retrieve has returned {} elements", name, retrieve.size());
            } else LOG.warn("Parser '{}'s initial retrieve has returned zero elements!", name);
            waitNext = true;
        }

        final Thread loop = new Thread(() -> {
            while (!Thread.interrupted())
                loop();
        }, name + " ZParser Loop");
        loop.setDaemon(true);
        loop.start();
    }

    private void loop() {
        if (waitNext) {
            try {
                Thread.sleep(nextUpdate());
            } catch (InterruptedException e) { LOG.warn("ZParser loop interrupted!"); }
        } else waitNext = true;

        final List<T> list = wRetrieve();
        if (list == null || list.isEmpty()) return;
        data.op(sl -> {
            final List<T> newElements = new ArrayList<>();
            for (T e: list) {
                if (sl.parallelStream().noneMatch(se -> se.equals(e.getSignature())))
                    newElements.add(e);
                else break;
            }
            for (T t: tryUpdate(newElements)) sl.add(t.getSignature());
        });
    }

    private List<T> wRetrieve() {
        try {
            return retrieve();
        } catch (Throwable t) {
            ErrorReporter.inst().reportError(context, getClass(), t);
            return null;
        }
    }

    private List<T> tryUpdate(List<T> list) {
        try {
            return onUpdate(list);
        } catch (Throwable t) {
            ErrorReporter.inst().reportError(context, getClass(), t);
            return Collections.emptyList();
        }
    }

    public abstract String getName();

    /**
     * MUST be sorted: first - the newest, last - the oldest
     * @return current observable list of elements
     */
    public abstract List<T> retrieve() throws Throwable;

    /**
     *
     * @param newElements new fucking elements
     * @return successfully committed
     */
    public abstract List<T> onUpdate(List<T> newElements) throws Throwable;

    public abstract long nextUpdate();

    public ZDSBot.GuildContext getContext() {
        return context;
    }

    @NotNull
    protected final LinkedList<T> handleNewElements(List<T> newElements, Consumer<T> committer) {
        final LinkedList<T> res = new LinkedList<>();
        for (T entry: newElements) {
            try {
                committer.accept(entry);
                res.add(entry);
            } catch (Throwable t) {
                ErrorReporter.inst().reportError(getContext(), getClass(), t);
            }
        }
        return res;
    }

}
