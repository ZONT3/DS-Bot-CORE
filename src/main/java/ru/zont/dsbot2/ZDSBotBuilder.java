package ru.zont.dsbot2;

import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import ru.zont.dsbot2.commands.CommandAdapter;
import ru.zont.dsbot2.loops.LoopAdapter;

import javax.security.auth.login.LoginException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ZDSBotBuilder {
    private final JDABuilder jdaBuilder;
    private final ArrayList<Class<? extends CommandAdapter>> commands = new ArrayList<>();
    private final ArrayList<Class<? extends LoopAdapter>> loops = new ArrayList<>();
    private final ZDSBot.Options options = new ZDSBot.Options();
    private Config config;

    /**
     * Creates a new builder for ZDSBot instance, shipped with {@link JDABuilder#createLight(String)}
     *
     * @param key Discord API key
     */
    public ZDSBotBuilder(String key) {
        this(JDABuilder.createLight(key));
    }

    /**
     * Creates a new builder for ZDSBot instance.
     *
     * @param jdaBuilder already created {@link JDABuilder}
     */
    public ZDSBotBuilder(JDABuilder jdaBuilder) {
        this.jdaBuilder = jdaBuilder;
    }

    public ZDSBot build() throws LoginException {
        return new ZDSBot(jdaBuilder.build(), options, config, commands, loops);
    }

    /**
     * Sets following intents for JDA:
     * <li>{@link GatewayIntent#GUILD_MESSAGES}</li>
     * <li>{@link GatewayIntent#DIRECT_MESSAGES}</li>
     * <li>{@link GatewayIntent#GUILD_MEMBERS}</li>
     */
    public ZDSBotBuilder addDefaultIntents() {
        jdaBuilder.enableIntents(
                GatewayIntent.GUILD_MESSAGES,
                GatewayIntent.DIRECT_MESSAGES,
                GatewayIntent.GUILD_MEMBERS
        );
        return this;
    }

    /**
     * Sets up {@link JDABuilder} with:
     * <li>{@link ZDSBotBuilder#addDefaultIntents()}</li>
     * <li>{@link JDABuilder#setMemberCachePolicy(MemberCachePolicy)} : {@link MemberCachePolicy#ALL}</li>
     * <li>{@link JDABuilder#setChunkingFilter(ChunkingFilter)} : {@link ChunkingFilter#ALL}</li>
     */
    public ZDSBotBuilder defaultSetup() {
        addDefaultIntents();
        jdaBuilder
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL);
        return this;
    }

    @SafeVarargs
    public final ZDSBotBuilder addCommands(Class<? extends CommandAdapter>... commands) {
        Collections.addAll(this.commands, commands);
        return this;
    }

    @SafeVarargs
    public final ZDSBotBuilder addLoops(Class<? extends LoopAdapter>... loops) {
        Collections.addAll(this.loops, loops);
        return this;
    }

    public ZDSBotBuilder setConfig(Config config) {
        config.verify();
        this.config = config;
        return this;
    }

    /**
     * Mirror for {@link JDABuilder#addEventListeners(Object...)}
     */
    public ZDSBotBuilder addListeners(Object... adapters) {
        jdaBuilder.addEventListeners(adapters);
        return this;
    }

    public ZDSBotBuilder setIgnoreWebhooks(boolean b) {
        options.ignoreWebhooks = b;
        return this;
    }

    public ZDSBotBuilder setUseRawContent(boolean b) {
        options.useRawContent = b;
        return this;
    }

    public ZDSBotBuilder setTechAdmins(List<String> b) {
        options.techAdmins = b;
        return this;
    }

    public JDABuilder getJdaBuilder() {
        return jdaBuilder;
    }
}
