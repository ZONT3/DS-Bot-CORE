import ru.zont.dsbot2.Config;

import java.io.IOException;

public class Various {


    public static void testConfigs(String[] args) throws IOException {
        Main.MyConfig config = new Main.MyConfig();
        FakeGuild guild = new FakeGuild();

        Main.MyConfig loaded = ((Main.MyConfig) Config.forGuild(guild, config));
        Main.MyConfig global = ((Main.MyConfig) Config.getGlobal(config));

        System.out.println(loaded);
        System.out.println(global.toString(true));
        System.out.println();

        loaded.myChannel.set("14882281337");
        loaded.commit(guild);

        global.prefix.set("t.");
        global.commit(null);

        System.out.println(loaded);
        System.out.println(global.toString(true));
        System.out.println();
    }
}
