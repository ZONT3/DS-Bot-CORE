package ru.zont.dsbot.core.tools;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.*;
import net.dv8tion.jda.api.exceptions.ErrorResponseException;
import org.jetbrains.annotations.NotNull;
import ru.zont.dsbot.core.commands.CommandAdapter;
import ru.zont.dsbot.core.commands.Commands;

import java.io.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.zont.dsbot.core.tools.Strings.STR;

public class Tools {

    @NotNull
    public static GuildChannel tryFindChannel(String channelID, JDA jda) throws NullPointerException {
        GuildChannel channel = null;
        for (Guild guild: jda.getGuilds()) {
            channel = guild.getGuildChannelById(channelID);
            if (channel != null) break;
        }
        if (channel == null) throw new NullPointerException("Cannot find channel");
        return channel;
    }

    public static Role tryFindRole(String roleID, JDA jda) {
        Role role = null;
        for (Guild guild: jda.getGuilds()) {
            role = guild.getRoleById(roleID);
            if (role != null) break;
        }
        if (role == null) throw new NullPointerException("Cannot find role");
        return role;
    }

    @NotNull
    public static Message tryFindMessage(String messageID, JDA jda) throws NullPointerException {
        Message msg = null;
        for (Guild guild: jda.getGuilds()) {
            for (TextChannel channel: guild.getTextChannels()) {
                try {
                    msg = channel.retrieveMessageById(messageID).complete();
                } catch (ErrorResponseException ignored) { }
            }
            if (msg != null) break;
        }
        if (msg == null) throw new NullPointerException("Cannot find msg");
        return msg;
    }

    @NotNull
    public static TextChannel tryFindTChannel(String channelID, JDA jda) throws NullPointerException {
        TextChannel channel = null;
        for (Guild guild: jda.getGuilds()) {
            channel = guild.getTextChannelById(channelID);
            if (channel != null) break;
        }
        if (channel == null) throw new NullPointerException("Cannot find channel");
        return channel;
    }

    public static void reportError(Throwable e, Class<?> klass, JDA jda) {
        e.printStackTrace();
        Messages.tryPrintError(STR.getString("err.unexpected"), Messages.describeException(klass, e), jda);
    }

    public static boolean guildAllowed(Guild guild) {
        final String p = Configs.getGlobalProps().getProperty("ALLOWED_SERVERS");
        return p == null || p.contains(guild.getId());
    }

    public static long userMentionToID(String mention) {
        final Matcher m = Pattern.compile("<@!?(\\d+)>").matcher(mention);
        if (!m.find()) throw new CommandAdapter.UserInvalidArgumentException(STR.getString("comms.err.invalid_mention"));
        return Long.parseLong(m.group(1));
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public static Object retrieveObject(File f) {
        if (!f.exists()) {
            f.getParentFile().mkdirs();
            return null;
        }

        try (FileInputStream fis = new FileInputStream(f);
             ObjectInputStream ois = new ObjectInputStream(fis)) {
            return ois.readObject();
        } catch (IOException | ClassNotFoundException e) {
            f.delete();
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings({"ResultOfMethodCallIgnored"})
    public static void commitObject(File f, Object o) {
        if (!f.exists())
            f.getParentFile().mkdirs();

        try (FileOutputStream fos = new FileOutputStream(f);
             ObjectOutputStream oos = new ObjectOutputStream(fos)) {
            oos.writeObject(o);
            oos.flush();
        } catch (IOException e) {
            f.delete();
            throw new RuntimeException(e);
        }
    }
}
