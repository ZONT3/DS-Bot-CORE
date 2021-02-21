import net.dv8tion.jda.api.entities.TextChannel;
import ru.zont.dsbot2.Config;
import ru.zont.dsbot2.ConfigCaster;
import ru.zont.dsbot2.ZDSBot;
import ru.zont.dsbot2.ZDSBotBuilder;
import ru.zont.dsbot2.commands.CommandAdapter;
import ru.zont.dsbot2.commands.Input;
import ru.zont.dsbot2.commands.UserInvalidInputException;

import javax.security.auth.login.LoginException;

public class Main {

    public static class Kek extends CommandAdapter {
        public Kek(ZDSBot.GuildContext context) {
            super(context);
        }

        @Override
        public void onCall(Input input) {
            String[] args = input.getCommandLine().getArgs();
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
        public boolean checkPermission() {
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
            MyConfig conf = new ConfigCaster<MyConfig>().cast(MyConfig.class, input.getContext().getConfig());
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
        public boolean checkPermission() {
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

    public static class MyConfig extends Config {
        public final Entry myChannel = new Entry("792009238062039070");
        public final Entry myRole = new Entry("777555228");

        @OverrideEntry
        public final Entry prefix = new Entry("t.", true);

        public MyConfig() {
            super.prefix = prefix;
        }
    }

    public static void main(String[] args) throws LoginException {
        ZDSBot bot = new ZDSBotBuilder(args[0])
                .defaultSetup()
                .setConfig(new MyConfig())
                .addCommands(Pepega.class, Kek.class)
                .build();
    }
}
