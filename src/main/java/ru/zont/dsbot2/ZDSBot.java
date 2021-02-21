package ru.zont.dsbot2;

import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.TextChannel;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.zont.dsbot2.commands.CommandAdapter;
import ru.zont.dsbot2.loops.LoopAdapter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Predicate;

public class ZDSBot {
    private static final Logger LOG = LoggerFactory.getLogger(ZDSBot.class);

    private final JDA jda;
    private final Options options;
    private final Config globalConfig;
    private final ArrayList<Class<? extends CommandAdapter>> commandAdapters;
    private final ArrayList<Class<? extends LoopAdapter>> loopAdapters;
    private final ArrayList<GuildContext> guilds = new ArrayList<>();
    private GuildContext voidGuild = null;

    ZDSBot(JDA jda, Options options, Config defaultConfig, ArrayList<Class<? extends CommandAdapter>> commandAdapters, ArrayList<Class<? extends LoopAdapter>> loopAdapters) {
        this.jda = jda;
        this.options = options;
        this.globalConfig = Config.getGlobal(defaultConfig);
        this.commandAdapters = commandAdapters;
        this.loopAdapters = loopAdapters;
        jda.addEventListener(new MainDispatcher(this));
    }

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

    public class GuildContext {
        private final CommandAdapter[] commandAdapters;
        private final LoopAdapter[] loopAdapters;
        private final Guild guild;
        private final Config config;

        public GuildContext(Guild guild,
                            ArrayList<Class<? extends CommandAdapter>> commandAdapters,
                            ArrayList<Class<? extends LoopAdapter>> loopAdapters) {
            this.guild = guild;
            config = guild != null ? Config.forGuild(guild, globalConfig) : Config.getGlobal(globalConfig);

            this.commandAdapters = new CommandAdapter[commandAdapters.size()];
            this.loopAdapters = new LoopAdapter[loopAdapters.size()];
            for (int i = 0; i < commandAdapters.size() || i < loopAdapters.size(); i++) {
                Class<? extends CommandAdapter> commandAdapter = commandAdapters.size() > i ? commandAdapters.get(i) : null;
                Class<? extends LoopAdapter> loopAdapter = loopAdapters.size() > i ? loopAdapters.get(i) : null;
                try {
                    if (commandAdapter != null)
                        this.commandAdapters[i] = commandAdapter.getDeclaredConstructor(GuildContext.class)
                              .newInstance(this);
                    if (loopAdapter != null)
                        this.loopAdapters[i] = loopAdapter.getDeclaredConstructor(GuildContext.class)
                              .newInstance(this);
                } catch (Throwable e) {
                    throw new RuntimeException(e); // TODO trace this
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

        public TextChannel getTChannel(String id) {
            return Config.getTChannel(this, id);
        }

        public boolean isForeign() {
            return guild == null || !getConfig().approved_guilds.get().contains(guild.getId());
        }

        public CommandAdapter[] getCommands() {
            return commandAdapters;
        }

        public LoopAdapter[] getLoops() {
            return loopAdapters;
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
        boolean useRawContent = false;
        List<String> techAdmins = Collections.emptyList();

        public boolean ignoreWebhooks() {
            return ignoreWebhooks;
        }

        /**
         * @return Using raw message content <b>for detecting command prefix</b>
         */
        public boolean useRawContent() {
            return useRawContent;
        }
    }

}