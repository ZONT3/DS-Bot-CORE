package ru.zont.dsbot2.commands.implement.exec;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ru.zont.dsbot2.ZDSBot;
import ru.zont.dsbot2.commands.CommandAdapter;
import ru.zont.dsbot2.commands.Input;
import ru.zont.dsbot2.tools.ZDSBStrings;

public class Cmd extends CommandAdapter {

    public Cmd(ZDSBot.GuildContext context) {
        super(context);
    }

    @Override
    public void onCall(Input input) {

    }

    @Override
    public String getCommandName() {
        return "cmd";
    }

    @Override
    public boolean checkPermission(MessageReceivedEvent event) {
        return false;
    }

    @Override
    public boolean allowPM() {
        return false;
    }

    @Override
    public boolean allowForeignGuilds() {
        return false;
    }

    @Override
    public String getSynopsis() {
        return "cmd <commands>";
    }

    @Override
    public String getDescription() {
        return ZDSBStrings.STR.getString("comms.cmd.desc");
    }
}
