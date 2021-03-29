import net.dv8tion.jda.api.entities.TextChannel;
import ru.zont.dsbot2.Config;
import ru.zont.dsbot2.ConfigCaster;
import ru.zont.dsbot2.ZDSBot;
import ru.zont.dsbot2.ZDSBotBuilder;
import ru.zont.dsbot2.commands.CommandAdapter;
import ru.zont.dsbot2.commands.Input;
import ru.zont.dsbot2.commands.UserInvalidInputException;
import ru.zont.dsbot2.commands.implement.Help;
import ru.zont.dsbot2.commands.implement.exec.Cmd;
import ru.zont.dsbot2.commands.implement.exec.Do;
import ru.zont.dsbot2.commands.implement.exec.Exec;
import ru.zont.dsbot2.commands.implement.exec.Term;
import ru.zont.dsbot2.loops.LoopAdapter;

import javax.security.auth.login.LoginException;
import java.util.concurrent.BlockingDeque;


public class Main {

    public static class Kek extends CommandAdapter {
        public Kek(ZDSBot.GuildContext context) {
            super(context);
        }

        @Override
        public void onCall(Input input) {
            String[] args = input.getArgs();
            if (args.length < 1) throw new UserInvalidInputException("Too few args");

            String id = args[0];
            TextChannel channel = getTChannel(id);
            if (channel == null) throw new UserInvalidInputException("Invalid channel ID");

            channel.sendMessage(input.getContentRaw(args, 1)).queue();
        }

        @Override
        public String getCommandName() {
            return "kek";
        }

        @Override
        public boolean checkPermission(Input input) {
            return true;
        }

        @Override
        public boolean allowPM() {
            return true;
        }

        @Override
        public boolean allowForeignGuilds() {
            return true;
        }

        @Override
        public String getSynopsis() {
            return "kek <id> <msg>";
        }

        @Override
        public String getDescription() {
            return "KEKW";
        }
    }

    public static class Pepega extends CommandAdapter {

        public Pepega(ZDSBot.GuildContext context) {
            super(context);
        }

        @Override
        public void onCall(Input input) {
            MyConfig conf = ConfigCaster.cast(input.getContext().getConfig());
            String content = input.getContentRaw();

            input.getChannel().sendMessage(content).queue();

            TextChannel channel = getTChannel(conf.myChannel.get());
            call(Kek.class, input.getEvent(), channel.getId(), "Pepega says:", content);
        }

        @Override
        public String getCommandName() {
            return "pepega";
        }

        @Override
        public boolean checkPermission(Input input) {
            return true;
        }

        @Override
        public boolean allowPM() {
            return true;
        }

        @Override
        public boolean allowForeignGuilds() {
            return true;
        }

        @Override
        public String getSynopsis() {
            return "pepega";
        }

        @Override
        public String getDescription() {
            return "Just a Pepega";
        }
    }

    public static class MyLoop extends LoopAdapter {
        public MyLoop(ZDSBot.GuildContext context) {
            super(context);
        }

        @Override
        public void loop() throws Throwable {
            throw new RuntimeException("A ya tupa KEK oshibka xDDDDD " + getContext().getGuild());
        }

        @Override
        public long getPeriod() {
            return 10000;
        }

        @Override
        public boolean runInGlobal() {
            return true;
        }

        @Override
        public boolean runInLocal() {
            return true;
        }
    }

    public static class MyConfig extends Config {
        public final Entry myChannel = new Entry("792009238062039070");
        public final Entry myRole = new Entry("777555228");

        public final Entry prefix = new Entry("t.", true);
        public final Entry channel_log = new Entry("450293189711101952", true);

        public MyConfig() {
            super.prefix = prefix;
            super.channel_log = channel_log;
        }
    }
}
