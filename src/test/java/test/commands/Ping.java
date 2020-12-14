package test.commands;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.jetbrains.annotations.NotNull;
import ru.zont.dsbot.core.CommandAdapter;
import ru.zont.dsbot.core.Strings;
import ru.zont.dsbot.core.ZDSBot;

import java.util.Properties;

public class Ping extends CommandAdapter {
    public Ping(ZDSBot bot) throws RegisterException {
        super(bot);
    }

    @Override
    public String getCommandName() {
        return "ping";
    }

    @Override
    public Properties getPropsDefaults() {
        return null;
    }

    @Override
    public String getSynopsis() {
        return "ping";
    }

    @Override
    public String getDescription() {
        return Strings.STR.getString("comm.ping.desc");
    }

    @Override
    public void onRequest(@NotNull MessageReceivedEvent event) {
        Message origin = event.getMessage();
        long found = System.currentTimeMillis();
        event.getChannel().sendMessage(
                new EmbedBuilder()
                        .setTitle("Pong!")
                        .build()
        ).queue(message -> message.editMessage(
                new EmbedBuilder()
                        .setTitle("Pong!")
                        .addField("Messages diff",
                                String.format("%d ms", message.getTimeCreated().toInstant().toEpochMilli() - origin.getTimeCreated().toInstant().toEpochMilli()),
                                false)
                        .addField("Server delay",
                                String.format("%d ms", System.currentTimeMillis() - found),
                                false)
                        .build()
        ).queue());
    }
}
