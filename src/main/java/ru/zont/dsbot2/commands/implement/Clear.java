package ru.zont.dsbot2.commands.implement;

import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.MessageHistory;
import net.dv8tion.jda.api.entities.TextChannel;
import ru.zont.dsbot2.ZDSBot;
import ru.zont.dsbot2.commands.CommandAdapter;
import ru.zont.dsbot2.commands.Input;
import ru.zont.dsbot2.commands.UserInvalidInputException;
import ru.zont.dsbot2.tools.ZDSBMessages;

import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.zont.dsbot2.tools.ZDSBStrings.*;

public class Clear extends CommandAdapter {
    public Clear(ZDSBot.GuildContext context) {
        super(context);
    }

    @Override
    public void onCall(Input input) {
        if (input.getArgs().length < 1)
            throw new UserInvalidInputException(STR.getString("err.insufficient_args"));

        TextChannel inputChannel = input.getEvent().getTextChannel();
        TextChannel toClear;
        int amount;
        if (input.getArgs().length == 1) {
            if (!input.getArg(0).matches("\\d+"))
                throw new UserInvalidInputException(STR.getString("comm.clear.err.syntax"));
            toClear = inputChannel;
            amount = Integer.parseInt(input.getArg(0));
        } else {
            final String ref = input.getArg(0);
            final Matcher matcher = Pattern.compile("<#!?(\\d+)>").matcher(ref);
            if (!matcher.find() || !input.getArg(1).matches("\\d+"))
                throw new UserInvalidInputException(STR.getString("comm.clear.err.syntax"));
            if (!input.getEvent().isFromGuild())
                throw new UserInvalidInputException(STR.getString("comm.clear.err.pm"), false);
            toClear = input.getEvent().getGuild().getTextChannelById(matcher.group(1));
            amount = Integer.parseInt(input.getArg(1));
        }
        if (toClear == null)
            throw new RuntimeException(STR.getString("comm.clear.err.null_channel"));

        final boolean b = toClear == inputChannel;
        if (b) input.getMessage().delete().complete();
        else ZDSBMessages.addOK(input.getEvent().getMessage());

        final MessageHistory history = toClear.getHistory();
        final ArrayList<Message> msg = new ArrayList<>();
        for (int i = amount; i > 0; i -= 100)
            msg.addAll(history.retrievePast(Math.min(i, 100)).complete());
        for (Message message: msg) message.delete().queue();
    }

    @Override
    public String getCommandName() {
        return "clear";
    }

    @Override
    public boolean checkPermission(Input input) {
        Member member = input.getMember();
        if (member == null) return true;
        return member.hasPermission(Permission.MESSAGE_MANAGE);
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
        return "clear [#channel] <amount>";
    }

    @Override
    public String getDescription() {
        return STR.getString("comms.clear.desc");
    }
}
