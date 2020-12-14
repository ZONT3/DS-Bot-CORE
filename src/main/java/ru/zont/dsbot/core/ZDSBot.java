package ru.zont.dsbot.core;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;
import org.reflections.Reflections;
import org.reflections.scanners.ResourcesScanner;
import org.reflections.scanners.SubTypesScanner;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import ru.zont.dsbot.core.commands.CommandAdapter;
import ru.zont.dsbot.core.handler.LStatusHandler;
import ru.zont.dsbot.core.tools.Configs;
import ru.zont.dsbot.core.tools.LOG;
import ru.zont.dsbot.core.tools.Messages;
import ru.zont.dsbot.core.tools.Strings;

import javax.annotation.Nonnull;
import javax.security.auth.login.LoginException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;

public class ZDSBot extends ListenerAdapter {
    public static final String ZONT_MENTION = "<@331524458806247426>";

    public final String version;
    public final String commandsPkg;
    public final String handlersPkg;
    public final CommandAdapter[] commandAdapters;
    public final LStatusHandler[] statusHandlers;
    public JDA jda;

    private final JDABuilder jdaBuilder;

    public ZDSBot(String token, String version, String commandsPkg, String handlersPkg) {
        this.version = version;
        this.commandsPkg = commandsPkg;
        this.handlersPkg = handlersPkg;

        commandAdapters = registerCommands();
        statusHandlers = registerHandlers();
        jdaBuilder = JDABuilder.createLight(token, GatewayIntent.GUILD_MESSAGES, GatewayIntent.DIRECT_MESSAGES, GatewayIntent.GUILD_MEMBERS)
                .addEventListeners(this)
                .addEventListeners((Object[]) statusHandlers)
                .setMemberCachePolicy(MemberCachePolicy.ALL)
                .setChunkingFilter(ChunkingFilter.ALL);
        Configs.writeDefaultGlobalProps();
    }

    public JDA create() throws LoginException {
        jda = jdaBuilder.build();
        return jda;
    }

    private LStatusHandler[] registerHandlers() {
        return new Register<LStatusHandler>().register(LStatusHandler.class, handlersPkg,
                h -> LOG.d("Successfully registered SH " + h.getClass().getSimpleName()))
                .toArray(new LStatusHandler[0]);
    }

    private CommandAdapter[] registerCommands() {
        return new Register<CommandAdapter>().register(CommandAdapter.class, commandsPkg,
                c -> LOG.d("Successfully registered command " + c.getCommandName()))
                .toArray(new CommandAdapter[0]);
    }

    public JDABuilder getJdaBuilder() {
        return jdaBuilder;
    }
    private class Register<T> {

        private ArrayList<T> register(Class<T> cls, String pkg, Consumer<T> onSuccess) {
            List<ClassLoader> classLoadersList = new LinkedList<>();
            classLoadersList.add(ClasspathHelper.contextClassLoader());
            classLoadersList.add(ClasspathHelper.staticClassLoader());

            Reflections reflections = new Reflections(new ConfigurationBuilder()
                    .setScanners(new SubTypesScanner(false /* don't exclude Object.class */), new ResourcesScanner())
                    .setUrls(ClasspathHelper.forClassLoader(classLoadersList.toArray(new ClassLoader[0])))
                    .filterInputsBy(new FilterBuilder().include(FilterBuilder.prefix(pkg))));
            Set<Class<? extends T>> allClasses =
                    reflections.getSubTypesOf(cls);

            ArrayList<T> res = new ArrayList<>();
            for (Class<? extends T> klass: allClasses) {
                if (Modifier.isAbstract(klass.getModifiers())) continue;
                try {
                    T instance = klass.getDeclaredConstructor(ZDSBot.class)
                            .newInstance(ZDSBot.this);
                    res.add(instance);
                    if (onSuccess != null)
                        onSuccess.accept(instance);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            return res;
        }
    }

    @Override
    public void onMessageReceived(@Nonnull MessageReceivedEvent event) {
        try {
            CommandAdapter.onMessageReceived(event, commandAdapters);
        } catch (Exception e) {
            e.printStackTrace();
            event.getChannel().sendMessage(
                    new EmbedBuilder( Messages.error(
                            Strings.STR.getString("err.unexpected"),
                            Messages.describeException(e)))
                            .setFooter(Strings.STR.getString("err.unexpected.foot"))
                            .build()).queue();
        }
    }
}
