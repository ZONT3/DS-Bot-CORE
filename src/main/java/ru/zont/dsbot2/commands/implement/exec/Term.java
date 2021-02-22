package ru.zont.dsbot2.commands.implement.exec;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageChannel;
import ru.zont.dsbot2.ZDSBot;
import ru.zont.dsbot2.commands.CommandAdapter;
import ru.zont.dsbot2.commands.Input;
import ru.zont.dsbot2.commands.UserInvalidInputException;
import ru.zont.dsbot2.tools.ZDSBMessages;

import static ru.zont.dsbot2.tools.ZDSBStrings.STR;

public class Term extends CommandAdapter {

    public Term(ZDSBot.GuildContext context) {
        super(context);
    }

    @Override
    public void onCall(Input input) {
        if (!input.get().matches("\\d+")) throw new UserInvalidInputException("PID **only** required");

        MessageChannel channel = input.getChannel();

        ExecHandler h = Exec.findProcess(Long.parseLong(input.get()));
        if (h == null) {
            channel.sendMessage(new EmbedBuilder()
                    .setColor(0xAC3311)
                    .setTitle("No such process")
                    .build()).queue();
            return;
        }

        h.terminate();
        ZDSBMessages.addOK(input.getMessage());
    }

    @Override
    public String getCommandName() {
        return "term";
    }

    @Override
    public String getSynopsis() {
        return "term <pid>";
    }

    @Override
    public String getDescription() {
        return STR.getString("comms.term.desc");
    }

    @Override
    public boolean checkPermission(Input input) {
        return false;
    }

    @Override
    public boolean allowPM() {
        return true;
    }

    @Override
    public boolean allowForeignGuilds() {
        return false;
    }
}
