import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.cache.CacheFlag;
import ru.zont.dsbot2.ZDSBot;
import ru.zont.dsbot2.ZDSBotBuilder;
import ru.zont.dsbot2.commands.implement.Clear;
import ru.zont.dsbot2.commands.implement.Help;
import ru.zont.dsbot2.commands.implement.Say;

import javax.security.auth.login.LoginException;
import java.util.List;

public class ExampleMain {
    public static class Config extends ru.zont.dsbot2.Config {
        public final Entry role_checked = new Entry("0");

        public Config() {
            super.prefix = new Entry("t.");
            super.channel_log = new Entry("814472065574109184", true);
            super.version = new Entry("TEST", true, false);
            super.version_str = new Entry("TEST DS BOT v.%s", true);
            super.approved_guilds = new Entry("785203451797569626,331526118635208716", true);
        }
    }

    public static void main(String[] args) throws LoginException, InterruptedException {

        ZDSBotBuilder builder = new ZDSBotBuilder(args[0])
                .defaultSetup()
                .setConfig(new Config())
                .addCommands(Help.class, Clear.class, Say.class)
                //.addLoops()
                .setTechAdmins(List.of("375638389195669504", "331524458806247426"))
                //.addListeners()
                ;

        builder.getJdaBuilder()
                .enableCache(CacheFlag.VOICE_STATE)
                .enableIntents(GatewayIntent.GUILD_MESSAGE_REACTIONS);

        final ZDSBot bot = builder.build();

        final JDA jda = bot.getJda();
        jda.awaitReady();

        final Guild guild = jda.getGuildById("331526118635208716");
        guild.moveVoiceMember(guild.getMemberById("153532216671076352"), guild.getVoiceChannelById("630067912862072842")).queue();
    }
}
