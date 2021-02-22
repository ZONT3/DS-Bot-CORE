package ru.zont.dsbot2.commands.implement;

import ru.zont.dsbot2.ZDSBot;
import ru.zont.dsbot2.commands.CommandAdapter;
import ru.zont.dsbot2.commands.Input;
import ru.zont.dsbot2.tools.ZDSBStrings;

public class Say extends CommandAdapter {
    public Say(ZDSBot.GuildContext context) {
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
    public boolean checkPermission(Input input) {
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
        return ZDSBStrings.STR.getString("comms.say.desc");
    }
}
