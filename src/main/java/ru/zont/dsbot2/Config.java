package ru.zont.dsbot2;

import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.reflect.Field;
import java.util.HashSet;
import java.util.Objects;
import java.util.Properties;

@SuppressWarnings("ResultOfMethodCallIgnored")
public class Config {
//    private static final Logger LOG = LoggerFactory.getLogger(ZDSBot.class);

    private static final File dir = new File("config");

    public static Config forGuild(Guild guild, Config global) {
        return getConfig(global, getConfigFile(guild), getComment(guild), false);
    }

    public static Config getGlobal(Config dflt) {
        return getConfig(dflt, getConfigFile(null), getComment(null), true);
    }

    @NotNull
    private static Config getConfig(Config dflt, File configFile, String comment, boolean global) {
        Properties config = dflt.toProperties(global);

        try {
            boolean justCreated = false;
            if (!configFile.isFile()) {
                configFile.delete();
                configFile.getParentFile().mkdirs();
                config.store(new FileOutputStream(configFile), comment);
                justCreated = true;
            }

            if (!justCreated)
                config.load(new FileInputStream(configFile));

            return fromProperties(dflt.getClass(), config, global);
        } catch (Throwable e) {
            throw new RuntimeException(e);
        }
    }

    private static Config fromProperties(Class<? extends Config> klass, Properties config, boolean global) throws Throwable {
        Config res = klass.getConstructor().newInstance();
        for (EntryPair e: res.entrySet()) {
            if (e.value.global != global) continue;

            String property = config.getProperty(e.key, null);
            if (property == null) property = "";
            e.value.value = property;
        }
        return res;
    }

    @NotNull
    private static File getConfigFile(Guild guild) {
        if (guild == null) return new File(dir, "global.properties");
        return new File(dir, guild.getId() + ".properties");
    }

    private static String getComment(Guild guild) {
        if (guild == null) return "ZDSBot Global Config";
        return String.format("ZDSBot Config for server \"%s\"", guild.getName());
    }

    public Entry prefix = new Entry("z.", true);
    public Entry approved_guilds = new Entry("", true);
    public Entry channel_log = new Entry("0");

    public synchronized void commit(Guild guild) {
        try {
            toProperties(guild == null).store(new FileOutputStream(getConfigFile(guild)), getComment(guild));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Properties toProperties() {
        return toProperties(false);
    }

    private Properties toProperties(boolean global) {
        Properties config = new Properties();
        for (EntryPair e: entrySet()) {
            if (e.value.global == global) {
                if (config.getProperty(e.key, null) != null && !e.override)
                    continue;
                config.setProperty(e.key, e.value.get());
            }
        }
        return config;
    }

    public HashSet<EntryPair> entrySet() {
        HashSet<EntryPair> res = new HashSet<>();
        for (Field field: getClass().getFields()) {
            if (!field.getType().equals(Entry.class) ||
                    !field.canAccess(this)) continue;
            try {
                if (field.get(this) == null)
                    throw new NullPointerException("Each entry should have a default value");
                res.add(new EntryPair(field.getName(), (Entry) field.get(this), field.isAnnotationPresent(OverrideEntry.class)));
            } catch (IllegalAccessException e) {
                throw new RuntimeException(e);
            }
        }
        return res;
    }

    void verify() {
        entrySet();
    }

    @Override
    public String toString() {
        return toProperties().toString();
    }

    public String toString(boolean global) {
        return toProperties(global).toString();
    }

    public static class EntryPair {
        public String key;
        public Entry value;
        public final boolean override;

        public EntryPair(String key, Entry value, boolean override) {
            this.key = key;
            this.value = value;
            this.override = override;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            EntryPair entryPair = (EntryPair) o;
            return Objects.equals(key, entryPair.key) && Objects.equals(value, entryPair.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(key, value);
        }
    }

    public static class Entry {
        private String value;
        private final boolean global;

        public Entry(String value, boolean global) {
            this.value = value;
            this.global = global;
        }

        public Entry(String defaultValue) {
            this(defaultValue, false);
        }

        public String get() {
            return value;
        }

        public void set(String value) {
            this.value = value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            Entry entry = (Entry) o;
            return global == entry.global && Objects.equals(value, entry.value);
        }

        @Override
        public int hashCode() {
            return Objects.hash(value, global);
        }
    }

    @Retention(RetentionPolicy.RUNTIME)
    public @interface OverrideEntry {}

    public static TextChannel getTChannel(ZDSBot.GuildContext context, String id) {
        if (id.isEmpty() || id.equals("0")) return null;

        Guild guild = context.getGuild();
        return guild != null ? guild.getTextChannelById(id) : null;
    }
}
