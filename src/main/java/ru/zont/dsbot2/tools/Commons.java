package ru.zont.dsbot2.tools;

import com.sun.net.httpserver.HttpExchange;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageChannel;
import org.apache.commons.io.IOUtils;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zont.dsbot2.ErrorReporter;
import ru.zont.dsbot2.ZDSBot;
import ru.zont.dsbot2.commands.Input;
import ru.zont.dsbot2.commands.UserInvalidInputException;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.zont.dsbot2.tools.ZDSBStrings.STR;

public class Commons {
    private static final Logger LOG = LoggerFactory.getLogger(Commons.class);

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
     * Permission that allows any input with first argument **not** from {@code values} list
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

    /**
     * @param mention Channel, User or Role mention
     * @return ID
     */
    public static long mentionToID(String mention) {
        final Matcher m = Pattern.compile("<[@#]!?&?(\\d+)>").matcher(mention);
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

    public static void tryReport(ZDSBot.GuildContext context, Class<?> clazz, Runnable r) {
        try {
            r.run();
        } catch (Throwable throwable) {
            ErrorReporter.inst().reportError(context, clazz, throwable);
        }
    }

    public static void httpResponse(HttpExchange exchange, String response, int code) throws IOException {
        exchange.sendResponseHeaders(code, response.getBytes().length);
        OutputStream os = exchange.getResponseBody();
        os.write(response.getBytes());
        os.close();
    }

    /**
     * @param exchange Input exchange (must be open)
     * @return Content, that must (but may not) be a JSON <br/>or {@code null} if {@code Content-Type != application/json} or {@code method != POST}
     * @throws IOException exception on retrieving request body
     */
    @Nullable
    public static String requireJson(HttpExchange exchange) throws IOException {
        if (!"post".equalsIgnoreCase(exchange.getRequestMethod())) {
            httpResponse(exchange, "Only POST method is acceptable", 400);
            return null;
        }

        final List<String> contentType = exchange.getRequestHeaders().get("Content-type");
        if (contentType.size() < 1 || !contentType.contains("application/json")) {
            httpResponse(exchange, "Content-type should be JSON", 400);
            return null;
        }

        return IOUtils.toString(exchange.getRequestBody(), StandardCharsets.UTF_8);
    }

    /**
     * Removes every message in text channel.
     * Exceptions allowed
     * @param except list of message IDs to except
     * @param channel
     */
    public static void clearTextChannel(MessageChannel channel, List<String> except) {
        if (channel == null) {
            LOG.warn("Tried to clear NULL channel");
            return;
        }
        for (Message msg: channel.getHistory().retrievePast(100).complete()) {
            if (except.parallelStream().anyMatch(s -> s.equals(msg.getId())))
                continue;
            msg.delete().queue();
        }
    }
}
