package test.handlers;

import net.dv8tion.jda.api.events.ReadyEvent;
import ru.zont.dsbot.core.ZDSBot;
import ru.zont.dsbot.core.tools.LOG;
import ru.zont.dsbot.core.handler.LStatusHandler;


public class HEvents extends LStatusHandler {
    public HEvents(ZDSBot bot) {
        super(bot);
    }

    @Override
    public void prepare(ReadyEvent event) throws Exception {
        LOG.d("prepare method");
    }

    @Override
    public void update() throws Exception {
        LOG.d("update method");

    }

    @Override
    public long getPeriod() {
        return 5000;
    }
}
