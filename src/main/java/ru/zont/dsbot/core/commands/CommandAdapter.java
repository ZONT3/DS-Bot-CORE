package ru.zont.dsbot.core.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.dsbot.core.tools.Configs;
import ru.zont.dsbot.core.tools.LOG;
import ru.zont.dsbot.core.tools.Messages;
import ru.zont.dsbot.core.ZDSBot;
import ru.zont.dsbot.core.tools.Tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import static ru.zont.dsbot.core.tools.Strings.*;

public abstract class CommandAdapter {
    private Properties propertiesCache = null;
    private long propertiesCacheTS = 0;
    private final ZDSBot bot;

    public abstract void onRequest(@NotNull MessageReceivedEvent event) throws UserInvalidArgumentException;

    public abstract String getCommandName();

    public List<String> getAliases() { return new ArrayList<>(); }

    public abstract String getSynopsis();

    public abstract String getDescription();

    protected abstract Properties getPropsDefaults();

    public abstract boolean checkPermission(MessageReceivedEvent event);

    public boolean isHidden() { return false; }

    public boolean allowForeignGuilds(@NotNull MessageReceivedEvent event) { return true; }

    protected void onInsufficientPermissions(@NotNull MessageReceivedEvent event) {
        Messages.printError(event.getChannel(), STR.getString("err.insufficient_perm.title"), STR.getString("err.insufficient_perm"));
    }

    public CommandAdapter(ZDSBot bot) throws RegisterException {
        this.bot = bot;
        String commandName = getCommandName();
        if (!commandName.matches("[\\w.!-=+-@#$]+") && !commandName.isEmpty())
            throw new RegisterException("Bad command name: " + commandName);
        if (getPropsDefaults() != null)
            writeDefaultProps();
        LOG.d("Successfully registered command " + getCommandName() + " (" + getClass().getSimpleName() + ")");
    }

    public ZDSBot getBot() {
        return bot;
    }

    public Properties getProps() {
        long current = System.currentTimeMillis();
        if (propertiesCache != null && current - propertiesCacheTS <= Configs.CACHE_LIFETIME)
            return propertiesCache;

        Properties props = Configs.getProps(getCommandName(), getPropsDefaults());
        propertiesCache = props;
        propertiesCacheTS = current;
        return props;
    }

    public void storeProps(Properties properties) {
        Configs.storeProps(getCommandName(), properties);
        propertiesCache = properties;
        propertiesCacheTS = System.currentTimeMillis();
    }

    public static void onMessageReceived(@NotNull MessageReceivedEvent event, CommandAdapter[] adapters) {
        if (event.getAuthor().isBot()) return;
        String prefix = Configs.getPrefix();
        String content = event.getMessage().getContentRaw();
        boolean inGuild = event.getChannelType().isGuild();
        if (inGuild && !content.startsWith(prefix))
            return;
        if (content.startsWith(prefix))
            content = content.substring(prefix.length());
        CommandAdapter adapter  = null;
        for (CommandAdapter a: adapters) {
            ArrayList<String> commandNames = new ArrayList<>(a.getAliases());
            commandNames.add(a.getCommandName());
            for (String name: commandNames) {
                if (content.startsWith(name)) {
                    adapter = a;
                    break;
                }
            }
        }

        LOG.d("Command received: '%s' from user %s", event.getMessage().getContentRaw(), event.getAuthor().getAsTag());
        if (adapter == null) {
            Messages.printError(event.getChannel(), STR.getString("err.unknown_command.title"), String.format(STR.getString("err.unknown_command"), ZDSBot.ZONT_MENTION));
            return;
        }
        if (event.isWebhookMessage()) {
            System.err.println("This is a webhook message, idk how to handle it");
            return;
        }

        if (!Configs.isTechAdmin(event.getAuthor().getId())) {
            if (!adapter.allowForeignGuilds(event) && !Tools.guildAllowed(event.getGuild())) {
                Messages.printError(event.getChannel(),
                        STR.getString("err.cannot_complete"), STR.getString("err.foreign_server"));
                return;
            }
            boolean permission = adapter.checkPermission(event);
            if (!permission && event.getMember() == null) {
                Messages.printError(event.getChannel(),
                        STR.getString("err.unknown_perm.title"), STR.getString("err.unknown_perm"));
                return;
            }
            if (!permission) {
                adapter.onInsufficientPermissions(event);
                return;
            }
        }

        try {
            adapter.onRequest(event);
        } catch (UserInvalidArgumentException e) {
            event.getChannel()
                    .sendMessage(Messages.error(
                            STR.getString("err.args.title"),
                            e.getMessage() + (e.printSyntax ? ("\n\n" +
                                    String.format(STR.getString("err.args.syntax"), adapter.getSynopsis(), inGuild ? prefix : "", adapter.getCommandName())) : "") ))
                    .queue();
        } catch (NotImplementedException e) {
            event.getChannel().sendMessage(new EmbedBuilder()
                    .setTitle(STR.getString("err.cannot_complete"))
                    .setDescription(STR.getString("err.not_implemented"))
                    .setColor(0xc2185b)
                    .build()).queue();
        } catch (DescribedException e) {
            e.printStackTrace();
            if (e.getCause() == null)
                Messages.printError(event.getChannel(), e.getTitle(), e.getDescription());
            else Messages.printError(event.getChannel(), e.getTitle(),
                    String.format("%s\n\n%s", e.getDescription(), Messages.describeException(e.getCause())));
        }
    }

    private void writeDefaultProps() {
        String name = getCommandName();
        if (!new File(Configs.DIR_PROPS, name + ".properties").exists())
            Configs.storeProps(name, getPropsDefaults());
    }

    protected static class RegisterException extends RuntimeException {
        public RegisterException(String message) {
            super(message);
        }
    }

    public static class UserInvalidArgumentException extends RuntimeException {
        boolean printSyntax = true;
        public UserInvalidArgumentException(String s) {
            super(s);
        }
        public UserInvalidArgumentException(String s, boolean printSyntax) {
            super(s);
            this.printSyntax = printSyntax;
        }
    }

}
