package ru.zont.dsbot2.commands.implement;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ru.zont.dsbot2.ZDSBot;
import ru.zont.dsbot2.commands.CommandAdapter;
import ru.zont.dsbot2.commands.Input;

import java.awt.*;

import static ru.zont.dsbot2.tools.ZDSBMessages.printError;
import static ru.zont.dsbot2.tools.ZDSBStrings.STR;

public class Help extends CommandAdapter {
    public Help(ZDSBot.GuildContext context) {
        super(context);
    }

    @Override
    public String getCommandName() {
        return "help";
    }

    @Override
    public boolean checkPermission(MessageReceivedEvent event) {
        return true;
    }

    @Override
    public void onCall(Input input) {
        String inpt = input.get();
        CommandAdapter comm = null;
        boolean b = !inpt.isEmpty();
        if (b) comm = getContext().commandForName(inpt);
        if (comm == null) {
            if (b) printError(input.getChannel(), STR.getString("comm.help.err.unknown.title"), STR.getString("comm.help.err.unknown"));

            EmbedBuilder builder = new EmbedBuilder()
                    .setTitle(STR.getString("comm.help.list.title"))
                    .setColor(Color.LIGHT_GRAY);
            for (CommandAdapter command: getContext().getCommands()) {
                if (command.isHidden()) continue;
                builder.addField(
                        command.getCommandName(),
                        String.format("`%s%s`: %s",
                                input.getMember() != null ? getContext().getPrefix() : "",
                                getFirstSynopsis(command.getSynopsis()),
                                command.getDescription().substring(0, Math.min(90, command.getDescription().length()))
                                        + (command.getDescription().length() > 90 ? "..." : "")),
                        false);
            }
            builder.setFooter(String.format(getContext().getGlobalConfig().version_str.get(), getContext().getGlobalConfig().version.get()));
            input.getChannel().sendMessage(builder.build()).queue();
        } else { // Exact command
            input.getChannel().sendMessage(
                    new EmbedBuilder()
                            .setTitle(comm.getCommandName())
                            .addField(STR.getString("comm.help.entry.example"), formatSynopsis(comm.getSynopsis(),
                                    input.getMember() == null ? "" : getContext().getPrefix()), false)
                            .addField(STR.getString("comm.help.entry.desc"), comm.getDescription(), false)
                            .setColor(Color.LIGHT_GRAY)
                            .build()
            ).queue();
        }
    }

    private String getFirstSynopsis(String synopsis) {
        int i = synopsis.indexOf('\n');
        return synopsis.substring(0, i <= 0 ? synopsis.length() : i);
    }

    private String formatSynopsis(String synopsis, String prefix) {
        return String.format("```\n%s%s```", prefix, synopsis.replaceAll("\n", "\n" + prefix));
    }

    @Override
    public String getSynopsis() {
        return "help [command]";
    }

    @Override
    public String getDescription() {
        return STR.getString("comm.help.desc");
    }

    @Override
    public boolean allowPM() {
        return true;
    }

    @Override
    public boolean allowForeignGuilds() {
        return true;
    }
}
