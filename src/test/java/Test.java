import ru.zont.dsbot.core.ZDSBot;

import javax.security.auth.login.LoginException;

public class Test {
    public static void main(String[] args) throws LoginException, InterruptedException {
        ZDSBot bot = new ZDSBot(args[0], "test", "test.commands", "test.handlers");
        bot.create().awaitReady();
    }
}
