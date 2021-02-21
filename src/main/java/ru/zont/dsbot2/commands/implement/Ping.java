package ru.zont.dsbot2.commands.implement;

import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import ru.zont.dsbot2.ZDSBot;
import ru.zont.dsbot2.commands.CommandAdapter;
import ru.zont.dsbot2.commands.Input;
import ru.zont.dsbot2.tools.ZDSBStrings;

public class Ping extends CommandAdapter {
    public Ping(ZDSBot.GuildContext context) {
        super(context);
    }

    @Override
    public void onCall(Input input) {

    }

    @Override
    public String getCommandName() {
        return "ping";
    }

    @Override
    public boolean checkPermission(MessageReceivedEvent event) {
        return true;
    }

    @Override
    public boolean allowPM() {
        return true;
    }

    @Override
    public boolean allowForeignGuilds() {
        return true;
    }

    @Override
    public String getSynopsis() {
        return "ping";
    }

    @Override
    public String getDescription() {
        return ZDSBStrings.STR.getString("comms.ping.desc");
    }
}
