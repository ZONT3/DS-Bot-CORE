package ru.zont.dsbot2.commands.implement;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.ChannelType;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import ru.zont.dsbot2.ZDSBot;
import ru.zont.dsbot2.commands.CommandAdapter;
import ru.zont.dsbot2.commands.Input;
import ru.zont.dsbot2.commands.UserInvalidInputException;
import ru.zont.dsbot2.tools.ZDSBStrings;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.zont.dsbot2.tools.ZDSBStrings.*;

public class Say extends CommandAdapter {
    public Say(ZDSBot.GuildContext context) {
        super(context);
    }

    @Override
    public void onCall(Input input) {

        Options options = new Options();
        options.addOption("t", "title", true, STR.getString("comms.say.opt.title"));
        options.addOption("c", "color", true, STR.getString("comms.say.opt.title"));

        MessageReceivedEvent event = input.getEvent();
        if (!event.isFromType(ChannelType.PRIVATE)) return;
        String content = input.getContentRaw().replaceFirst(".+?(?=\\d{7,})", "");
        String[] s = content.split(" ");
        if (s.length < 2) throw new UserInvalidInputException(STR.getString("err.incorrect_args"));
        if (!s[0].matches("\\d+")) throw new UserInvalidInputException("First arg should be a channel ID!");
        String id = s[0];

        TextChannel channel = null;
        for (Guild guild: event.getJDA().getGuilds()) {
            channel = guild.getTextChannelById(id);
            if (channel != null) break;
        }
        if (channel == null) throw new UserInvalidInputException("Cannot find channel");

        String text = content.replaceFirst("\\d+ ", "");

        CommandLine cli = input.getCommandLine(options, true);
        if (cli.hasOption("t") || cli.hasOption("c")) {
            EmbedBuilder builder = new EmbedBuilder();
            if (cli.hasOption("t"))
                builder.setTitle(cli.getOptionValue("t"));

            if (cli.hasOption("c")) {
                String c = cli.getOptionValue("c");

                Matcher matcher = Pattern.compile("(0[xX]|#)?([0-9a-fA-F]+)").matcher(c);
                if (!matcher.find())
                    throw new UserInvalidInputException("Invalid color. Should be HEX: 0x11ffaa, #11ffaa or 11ffaa");
                String clr = matcher.group(2);

                builder.setColor(Integer.parseInt(clr, 16));
            }

            builder.setDescription(text);
            channel.sendMessage(builder.build()).complete();
        } else channel.sendMessage(text).complete();
    }

    @Override
    public String getCommandName() {
        return "say";
    }

    @Override
    public boolean checkPermission(Input input) {
        Member member = input.getMember();
        return member != null && member.hasPermission(Permission.ADMINISTRATOR);
    }

    @Override
    public boolean allowPM() {
        return true;
    }

    @Override
    public boolean allowForeignGuilds() {
        return false;
    }

    @Override
    public String getSynopsis() {
        return "say [options] <id> <msg>";
    }

    @Override
    public String getDescription() {
        return STR.getString("comms.say.desc");
    }

    @Override
    public boolean isHidden() {
        return true;
    }
}
