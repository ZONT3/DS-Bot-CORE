package ru.zont.dsbot2;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.events.ReadyEvent;
import net.dv8tion.jda.api.events.guild.GuildJoinEvent;
import net.dv8tion.jda.api.events.guild.GuildReadyEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zont.dsbot2.commands.CommandAdapter;
import ru.zont.dsbot2.commands.Input;
import ru.zont.dsbot2.commands.UserInvalidInputException;

import java.util.regex.Pattern;
import java.util.stream.Stream;

import static ru.zont.dsbot2.tools.ZDSBMessages.*;
import static ru.zont.dsbot2.tools.ZDSBStrings.STR;

public class MainDispatcher extends ListenerAdapter {
    private static final Logger LOG = LoggerFactory.getLogger(CommandAdapter.class);

    private final ZDSBot context;

    public MainDispatcher(ZDSBot context) {
        this.context = context;
    }

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (event.isWebhookMessage() && context.getOptions().ignoreWebhooks()) return;

        ZDSBot.GuildContext gc;
        boolean fromGuild = event.isFromGuild();
        if (fromGuild)
            gc = context.forGuild(event.getGuild());
        else gc = context.getVoidGuildContext();

        if (gc == null) {
            NullPointerException e = new NullPointerException("Guild Context");
            ErrorReporter.printStackTrace(e, getClass());
            return;
        }

        String prefix = gc.getPrefix();
        String contentDisplay = event.getMessage().getContentDisplay();
        String content = event.getMessage().getContentRaw();

        if (!prefix.isEmpty() && content.startsWith(prefix))
            content = content.replaceFirst(Pattern.quote(prefix), "");
        else if (fromGuild) return;

        CommandAdapter cmd = null;
        for (CommandAdapter adapter: gc.getCommands())
            if (Stream.concat(
                    Stream.of(adapter.getCommandName()),
                    adapter.getAliases().stream())
                        .anyMatch(content::startsWith))
                cmd = adapter;

        String person;
        Member member = event.getMember();
        if (member != null)
            person = member.getNickname();
        else person = event.getAuthor().getAsTag();

        if (cmd == null) {
            LOG.info("Unknown command received from {}: {}", person, contentDisplay);
            printError(event.getChannel(),
                    STR.getString("err.unknown_command.title"),
                    String.format(STR.getString("err.unknown_command"), "<@331524458806247426>"));
            return;
        } else LOG.info("Command received from {}: {}", person, contentDisplay);

        Input input = new Input(event, gc, content.replaceFirst("[^ ]+ *", ""));

        if (!context.isTechAdmin(event.getAuthor().getId())) {
            if (member != null && !cmd.allowForeignGuilds() && gc.isForeign()) {
                LOG.info("Blocked foreign guild request");
                printError(event.getChannel(),
                        STR.getString("err"),
                        STR.getString("err.foreign_server"));
                return;
            }
            if (member == null && !cmd.allowPM()) {
                LOG.info("Blocked PM request");
                printError(event.getChannel(),
                        STR.getString("err"),
                        STR.getString("err.unknown_perm"));
                return;
            }
            if (!cmd.checkPermission(input)) {
                LOG.info("Blocked insufficient permissions request");
                cmd.onInsufficientPermissions(event);
                return;
            }
        }

        try {
            cmd.onCall(input);
        } catch (UserInvalidInputException e) {
            LOG.info("UserInvalidInputException thrown");
            event.getChannel()
                    .sendMessage(error(
                            STR.getString("err.args.title"),
                            e.getMessage() + (e.printSyntax ? ("\n\n" +
                                    String.format(STR.getString("err.args.syntax"),
                                            cmd.getSynopsis(),
                                            fromGuild ? prefix : "",
                                            cmd.getCommandName())) : "") ))
                    .queue();
        } catch (NotImplementedException e) {
            LOG.info("NotImplementedException thrown");
            event.getChannel().sendMessage(notImplemented()).queue();
        } catch (DescribedException e) {
            LOG.info("DescribedException thrown");
            ErrorReporter.printStackTrace(e, getClass());
            if (e.getCause() == null)
                printError(event.getChannel(), e.getTitle(), e.getDescription());
            else printError(event.getChannel(), e.getTitle(),
                    String.format("%s\n\n%s", e.getDescription(), describeException(e.getCause())));
        } catch (Throwable e) {
            LOG.info("Error thrown");
            event.getChannel().sendMessage(
                    new EmbedBuilder( error(
                                    STR.getString("err.unexpected"),
                                    describeException(e)))
                            .setFooter(STR.getString("err.unexpected.foot"))
                            .build()).queue();
            ErrorReporter.printStackTrace(e, getClass());
        }
    }

    @Override
    public void onGuildJoin(@NotNull GuildJoinEvent event) {
        LOG.debug("onGuildJoin");
        try {
            context.registerGuild(event.getGuild());
        } catch (Exception e) {
            ErrorReporter.printStackTrace(e, getClass());
        }
    }

    @Override
    public void onGuildReady(@NotNull GuildReadyEvent event) {
        LOG.debug("onGuildReady");
        try {
            context.registerGuild(event.getGuild());
        } catch (Exception e) {
            ErrorReporter.printStackTrace(e, getClass());
        }
    }

    @Override
    public void onReady(@NotNull ReadyEvent event) {
        try {
            context.registerVoidGuild();
        } catch (Exception e) {
            ErrorReporter.printStackTrace(e, getClass());
        }
    }
}
