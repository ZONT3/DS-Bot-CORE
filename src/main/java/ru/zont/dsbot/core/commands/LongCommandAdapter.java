package ru.zont.dsbot.core.commands;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.dsbot.core.ZDSBot;

public abstract class LongCommandAdapter extends CommandAdapter {
    public LongCommandAdapter(ZDSBot bot) throws RegisterException {
        super(bot);
    }

    public abstract void onRequestLong(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException;

    @Override
    public void onRequest(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException {
        event.getChannel().sendTyping().complete();
        onRequestLong(event);
    }
}
