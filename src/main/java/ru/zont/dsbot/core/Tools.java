package ru.zont.dsbot.core;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.io.*;

import static ru.zont.dsbot.core.Strings.STR;

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
        e.printStackTrace(); // TODO smart printing
        Messages.tryPrintError(STR.getString("err.unexpected"), Messages.describeException(klass, e), jda);
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
