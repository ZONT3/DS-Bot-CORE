package ru.zont.dsbot2.tools;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import ru.zont.dsbot2.commands.CommandAdapter;
import ru.zont.dsbot2.commands.Input;
import ru.zont.dsbot2.commands.UserInvalidInputException;

import java.util.ArrayList;
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

    /**
     * Permission that allows any input with first argument from {@code values} list
     * or {@link Member} with {@link Permission#ADMINISTRATOR} or {@link Permission#MANAGE_PERMISSIONS}
     * @param input Input param
     * @param values allowed first argument values
     * @return permission check result
     */
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

    public static String[] splitLength(String s, int len) {
        String[] ret = new String[(s.length() + len - 1) / len];

        int i = 0;
        for (int start = 0; start < s.length(); start += len) {
            ret[i] = (s.substring(start, Math.min(s.length(), start + len)));
            i++;
        }
        return ret;
    }

    /**
     * Parse IDs from string, delimited with any other char than a digit
     * @param s String to parse
     * @return List of IDs
     */
    public static ArrayList<String> getIDs(String s) {
        ArrayList<String> res = new ArrayList<>();
        Matcher matcher = Pattern.compile("\\d+").matcher(s);
        while (matcher.find())
            res.add(matcher.group());
        return res;
    }
}
