package ru.zont.dsbot2.tools;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import ru.zont.dsbot2.commands.CommandAdapter;
import ru.zont.dsbot2.commands.Input;
import ru.zont.dsbot2.commands.UserInvalidInputException;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.zont.dsbot2.tools.ZDSBStrings.*;

public class Commons {
    public static boolean isWindows() {
        return (System.getProperty("os.name").toLowerCase().contains("win"));
    }

    public static boolean isMac() {
        return (System.getProperty("os.name").toLowerCase().contains("mac"));
    }

    public static boolean isUnix() {
        final String os = System.getProperty("os.name");
        return (os.toLowerCase().contains("nix")
                || os.toLowerCase().contains("nux")
                || os.toLowerCase().contains("aix"));
    }

    public static boolean rolesLikePermissions(Input input, List<String> values) {
        if (!input.argEquals(0, values)) return true;

        final Member member = input.getEvent().getMember();
        return
                member != null && (
                        member.hasPermission(Permission.ADMINISTRATOR) ||
                        member.hasPermission(Permission.MANAGE_PERMISSIONS) );
    }

    public static CRouter rolesLikeRouter(int index, CRouter.Case set, CRouter.Case rm, CRouter.Case get) {
        return new CRouter(index)
                .addCase(set, "set", "add")
                .addCase(rm, "rm", "del")
                .addCase(get, "get", "list");
    }

    public static String assertSteamID(String arg) {
        if (!arg.matches("7656\\d+"))
            throw new UserInvalidInputException(STR.getString("comms.err.invalid_steamid64"));
        return arg;
    }

    public static long userMentionToID(String mention) {
        final Matcher m = Pattern.compile("<@!?(\\d+)>").matcher(mention);
        if (!m.find()) throw new UserInvalidInputException(STR.getString("comms.err.invalid_mention"));
        return Long.parseLong(m.group(1));
    }
}
