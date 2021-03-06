package ru.zont.dsbot2;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.GuildChannel;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zont.dsbot2.commands.CommandAdapter;
import ru.zont.dsbot2.loops.LoopAdapter;
import ru.zont.dsbot2.parser.ZParser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;
import java.util.stream.Stream;

@SuppressWarnings("rawtypes")
public class ZDSBot {
    private static final Logger LOG = LoggerFactory.getLogger(ZDSBot.class);

    private final JDA jda;
    private final Options options;
    private final Config globalConfig;
    private final ArrayList<Class<? extends CommandAdapter>> commandAdapters;
    private final ArrayList<Class<? extends LoopAdapter>> loopAdapters;
    private final ArrayList<Class<? extends ZParser>> parserAdapters;
    private final ArrayList<GuildContext> guilds = new ArrayList<>();
    private GuildContext voidGuild = null;

    ZDSBot(JDA jda, Options options, Config defaultConfig,
           ArrayList<Class<? extends CommandAdapter>> commandAdapters,
           ArrayList<Class<? extends LoopAdapter>> loopAdapters,
           ArrayList<Class<? extends ZParser>> parserAdapters
    ) {
        this.jda = jda;
        this.options = options;
        this.globalConfig = Config.getGlobal(defaultConfig);
        this.commandAdapters = commandAdapters;
        this.loopAdapters = loopAdapters;
        this.parserAdapters = parserAdapters;
        jda.addEventListener(new MainDispatcher(this));
    }

    @Nullable
    public GuildContext forGuild(Guild guild) {
        return guilds.stream()
                .filter(findGuild(guild))
                .findAny()
                .orElse(null);
    }

    public GuildContext getVoidGuildContext() {
        return voidGuild;
    }

    public synchronized void registerVoidGuild() {
        if (voidGuild != null) return;

        voidGuild = new GuildContext(null, commandAdapters, loopAdapters);

        LOG.info("Registered void guild context; CCOUNT={}, LCOUNT={}",
                voidGuild.getCommands().length,
                voidGuild.getLoops().length);
    }

    public synchronized void registerGuild(Guild guild) {
        if (guilds.stream().anyMatch(findGuild(guild))) return;

        GuildContext gc = new GuildContext(guild, commandAdapters, loopAdapters);
        guilds.add(gc);

        LOG.info("Registered guild {} ID{}; FOREIGN={}, CCOUNT={}, LCOUNT={}",
                guild.getName(),
                guild.getId(),
                gc.isForeign(),
                gc.getCommands().length,
                gc.getLoops().length);
    }

    @NotNull
    private Predicate<GuildContext> findGuild(Guild guild) {
        return gc -> gc.guild != null && gc.guild.getId().equals(guild.getId());
    }

    public boolean isTechAdmin(String id) {
        return getOptions().techAdmins.contains(id);
    }

    /**
     * Find a text channel across all guilds (in this shard, if applies)<br/>
     * <b>Weak performance, use {@link GuildContext#getTChannel(String)} instead</b>
     * @param id Text channel ID
     * @return A text channel, or {@code null} if not found
     */
    public TextChannel getTChannel(String id) {
        return jda.getTextChannelById(id);
    }

    /**
     * Find a guild channel across all guilds (in this shard, if applies)<br/>
     * <b>Weak performance, use {@link GuildContext#getChannel(String)} instead</b>
     * @param id Text channel ID
     * @return A text channel, or {@code null} if not found
     */
    private GuildChannel getChannel(String id) {
        for (Guild guild: jda.getGuilds()) {
            GuildChannel channel = guild.getGuildChannelById(id);
            if (channel != null) return channel;
        }

        return null;
    }

    public JDA getJda() {
        return jda;
    }

    public class GuildContext {
        private final CommandAdapter[] commandAdapters;
        private final LoopAdapter[] loopAdapters;
        private final ZParser[] parsers;
        private final Guild guild;
        private final Config config;

        public GuildContext(Guild guild,
                            ArrayList<Class<? extends CommandAdapter>> commandAdapters,
                            ArrayList<Class<? extends LoopAdapter>> loopAdapters) {
            this.guild = guild;
            config = guild != null ? Config.forGuild(guild, globalConfig) : Config.getGlobal(globalConfig);

            this.commandAdapters = new CommandAdapter[commandAdapters.size()];
            this.loopAdapters = new LoopAdapter[loopAdapters.size()];
            this.parsers = new ZParser[parserAdapters.size()];
            for (int i = 0; i < commandAdapters.size() || i < loopAdapters.size() || i < parserAdapters.size(); i++) {
                Class<? extends CommandAdapter> commandAdapter = commandAdapters.size() > i ? commandAdapters.get(i) : null;
                Class<? extends LoopAdapter> loopAdapter = loopAdapters.size() > i ? loopAdapters.get(i) : null;
                Class<? extends ZParser> parser = parserAdapters.size() > i ? parserAdapters.get(i) : null;
                try {
                    if (commandAdapter != null)
                        this.commandAdapters[i] = commandAdapter.getDeclaredConstructor(GuildContext.class)
                              .newInstance(this);
                } catch (Throwable e) {
                    ErrorReporter.printStackTrace(e, getClass());
                }
                try {
                    if (loopAdapter != null)
                        this.loopAdapters[i] = loopAdapter.getDeclaredConstructor(GuildContext.class)
                              .newInstance(this);
                } catch (Throwable e) {
                    ErrorReporter.printStackTrace(e, getClass());
                }
                try {
                    if (guild == null && parser != null)
                        this.parsers[i] = parser.getDeclaredConstructor(GuildContext.class)
                              .newInstance(this);
                } catch (Throwable e) {
                    ErrorReporter.printStackTrace(e, getClass());
                }
            }
        }

        public ZDSBot getBot() {
            return ZDSBot.this;
        }

        public String getPrefix() {
            return getConfig().prefix.get();
        }

        public Config getConfig() {
            return config;
        }

        public Config getGlobalConfig() {
            return globalConfig;
        }

        /**
         * Seek for a text channel in <b>current guild</b>.<br/>
         * If called from void context, then it calls to {@link ZDSBot#getTChannel(String)}
         * @param id Channel's ID
         * @return {@link TextChannel} or {@code null} if not found
         */
        public TextChannel getTChannel(String id) {
            if (getGuild() == null) return ZDSBot.this.getTChannel(id);
            return Config.getTChannel(this, id);
        }

        public GuildChannel getChannel(String id) {
            if (getGuild() == null) return ZDSBot.this.getChannel(id);
            return Config.getChannel(this, id);
        }

        public boolean isForeign() {
            return guild == null || !globalConfig.approved_guilds.get().contains(guild.getId());
        }

        public CommandAdapter commandForName(String name) {
            for (CommandAdapter command: getCommands())
                if (Stream.concat(
                        Stream.of(command.getCommandName()),
                        command.getAliases().stream()).anyMatch(name::equals))
                    return command;
            return null;
        }

        public CommandAdapter[] getCommands() {
            return commandAdapters;
        }

        public LoopAdapter[] getLoops() {
            return loopAdapters;
        }

        public ZParser[] getParsers() {
            return parsers;
        }

        public void tickLoop(Class<? extends LoopAdapter> clazz) {
            tickLoop(clazz, true);
        }

        public void tickLoop(Class<? extends LoopAdapter> clazz, boolean consumeNext) {
            for (LoopAdapter adapter: loopAdapters) {
                if (clazz.isInstance(adapter)) {
                    try {
                        adapter.loop();

                    } catch (Throwable throwable) {
                        ErrorReporter.inst().reportError(this, adapter.getClass(), throwable);
                    }
                }
            }
        }

        @Nullable
        public Guild getGuild() {
            return guild;
        }
    }

    public Options getOptions() {
        return options;
    }

    static class Options {
        boolean ignoreWebhooks = true;
        List<String> techAdmins = Collections.singletonList("331524458806247426");

        public boolean ignoreWebhooks() {
            return ignoreWebhooks;
        }
    }
}
