package ru.zont.dsbot2.commands.implement.exec;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import ru.zont.dsbot2.ZDSBot;
import ru.zont.dsbot2.commands.CommandAdapter;
import ru.zont.dsbot2.commands.Input;
import ru.zont.dsbot2.commands.UserInvalidInputException;
import ru.zont.dsbot2.tools.Commons;

import java.io.File;

import static ru.zont.dsbot2.tools.ZDSBStrings.STR;

public class Do extends CommandAdapter {

    public Do(ZDSBot.GuildContext context) {
        super(context);
    }

    @Override
    public void onCall(Input input) {
        String[] args = input.getArgs();
        if (args.length < 1) throw new UserInvalidInputException(STR.getString("err.insufficient_args"));

        String name = args[0];
        if (!name.matches("[\\w\\-.]+"))
            throw new UserInvalidInputException(STR.getString("comms.do.err.name"));
        if (name.endsWith(".py"))
            name = name.substring(0, name.length() - 3);

        String utf = "";
        if (Commons.isWindows()) utf = "-X utf8";
        call(Exec.class, input.getEvent(),
                String.format("-V \"--name=%s\" %s %s %s %s",
                        name,
                        Exec.PYTHON,
                        utf,
                        resolveScript(name),
                        input.getContentRaw(args, 1)
                ));
    }

    private String resolveScript(String raw) {
        File main = new File("scripts", raw + ".py");
        if (!main.exists()) throw new UserInvalidInputException(STR.getString("comms.do.err.name"));
        return main.getAbsolutePath();
    }

    @Override
    public boolean checkPermission(Input input) {
        Member member = input.getMember();
        return member != null && member.hasPermission(Permission.ADMINISTRATOR);
    }

    @Override
    public boolean allowPM() {
        return true;
    }

    @Override
    public boolean allowForeignGuilds() {
        return false;
    }

    @Override
    public String getCommandName() {
        return "do";
    }

    @Override
    public String getSynopsis() {
        return "do <script_name>";
    }

    @Override
    public String getDescription() {
        return STR.getString("comms.do.desc");
    }

}
