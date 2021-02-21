package ru.zont.dsbot2.commands;

import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zont.dsbot2.Config;
import ru.zont.dsbot2.ZDSBot;
import ru.zont.dsbot2.tools.ZDSBMessages;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static ru.zont.dsbot2.tools.ZDSBStrings.STR;

public abstract class CommandAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(CommandAdapter.class);

    private final ZDSBot.GuildContext context;

    public CommandAdapter(ZDSBot.GuildContext context) {
        this.context = context;
        String commandName = getCommandName();
        if (!commandName.matches("[\\w.!-=+-@#$]+") && !commandName.isEmpty())
            throw new RegisterException("Bad command name: " + commandName);
        LOG.info("Successfully registered command {} ({})", getClass().getSimpleName(), getCommandName());
    }

    /**********                                        Command logic                                         **********/

    public abstract void onCall(Input input);

    public abstract String getCommandName();

    public List<String> getAliases() { return Collections.emptyList(); }

    public abstract boolean checkPermission(MessageReceivedEvent event);

    public abstract boolean allowPM();

    public abstract boolean allowForeignGuilds();

    public abstract String getSynopsis();

    public abstract String getDescription();

    public boolean isHidden() { return false; }

    public void onInsufficientPermissions(MessageReceivedEvent event) {
        ZDSBMessages.printError(event.getChannel(),
                STR.getString("err.permission.title"),
                STR.getString("err.permission"));
    }

    /**********                                        Context tools                                         **********/

    protected Config getConfig() {
        return context.getConfig();
    }

    protected final TextChannel getTChannel(String id) {
        return Config.getTChannel(context, id);
    }

    protected final void call(Class<? extends CommandAdapter> klass, MessageReceivedEvent event, String... args) {
        Optional<CommandAdapter> any = Arrays.stream(context.getCommands()).filter(a -> a.getClass().equals(klass)).findAny();
        if (any.isEmpty()) throw new RuntimeException("Command with such class hadn't registered");
        any.get().onCall(new Input(event, context, String.join(" ", args)));
    }

    private static class RegisterException extends RuntimeException {
        public RegisterException(String s) {
            super(s);
        }
    }

    public ZDSBot.GuildContext getContext() {
        return context;
    }
}
