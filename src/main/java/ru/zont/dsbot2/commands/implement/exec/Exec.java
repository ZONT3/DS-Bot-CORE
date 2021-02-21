package ru.zont.dsbot2.commands.implement.exec;

import net.dv8tion.jda.api.entities.MessageChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.Options;
import ru.zont.dsbot2.DescribedException;
import ru.zont.dsbot2.NotImplementedException;
import ru.zont.dsbot2.ZDSBot;
import ru.zont.dsbot2.commands.CommandAdapter;
import ru.zont.dsbot2.commands.Input;
import ru.zont.dsbot2.commands.UserInvalidInputException;
import ru.zont.dsbot2.tools.Commons;
import ru.zont.dsbot2.tools.ZDSBMessages;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static ru.zont.dsbot2.tools.ZDSBStrings.STR;

public class Exec extends CommandAdapter {
    static {
        if (Commons.isUnix())
            PYTHON = "python3.9";
        else PYTHON = "py";
    }
    public static final String PYTHON;

    private static long nextPid = 1;
    private static final Map<Long, ExecHandler> processes = Collections.synchronizedMap(new HashMap<>());

    public Exec(ZDSBot.GuildContext context) {
        super(context);
    }

    @Override
    public void onCall(Input inputObj) {
        MessageReceivedEvent event = inputObj.getEvent();
        if (event == null) throw new IllegalStateException("Provided input must contain event");
        MessageChannel channel = event.getChannel();
        String input = inputObj.get();

        Pattern pattern = Pattern.compile("[^\\w]*(--?\\w+ +)*[^\\w]*```(\\w+)\\n((.|\\n)+)```[^\\w]*");
        Matcher matcher = pattern.matcher(input);

        Options options = new Options();
        options.addOption("b", "buffer", false, "");
        options.addOption("s", "silent", false, "");
        options.addOption("S", "single", false, "");
        options.addOption("V", "verbose", false, "");
        options.addOption("n", "name", true, "");
        CommandLine cl = inputObj.getCommandLine(options, true);

        String lineToExec;
        String name;
        ExecHandler.Parameters params = new ExecHandler.Parameters();
        SubprocessListener.Builder builder = new SubprocessListener.Builder();
        if (matcher.find()) {
            String lang = matcher.group(2);
            String code = matcher.group(3).replaceAll("\\\\`", "`");

            boolean buff = cl.hasOption("b");
            boolean silent = cl.hasOption("s");
            boolean single = cl.hasOption("S");
            if (silent) {
                params.verbose = false;
                event.getMessage().delete().queue();
            }
            if (single) params.single_window = true;

            File tempFile;
            switch (lang) {
                case "py", "python" -> {
                    name = "Python code";
                    tempFile = toTemp(code);
                    String utf;
                    if (Commons.isWindows()) utf = "-X utf8 ";
                    else utf = "";
                    lineToExec = String.format("%s %s%s\"%s\"",
                            PYTHON,
                            utf,
                            buff ? "" : "-u ",
                            tempFile.getAbsolutePath());
                }
                case "java" ->
                        throw new NotImplementedException();
                default -> throw new RuntimeException("Unknown programming language");
            }
            params.onVeryFinish = param -> {
                if (!tempFile.delete())
                    System.err.println("Cannot delete temp file!");
            };
        } else if (cl.hasOption("c")) {
            if (!Commons.isWindows()) throw new DescribedException(STR.getString("err.general"),
                    "Cannot perform it on non-windows machine");
            input = inputObj.stripPrefixOpts();
            String[] args = input.split(" ");
            if (args.length < 1) throw new UserInvalidInputException("Corrupted input, may be empty", false);
            name = args[0];
            lineToExec = "cmd /c " + input;
            params.verbose = false;
            builder.setCharset(Charset.forName("866"));
        } else {
            input = inputObj.stripPrefixOpts();
            String[] args = input.split(" ");
            if (args.length < 1) throw new UserInvalidInputException("Corrupted input, may be empty", false);

            if (cl.hasOption("V")) params.verbose = false;

            final String nameParam = cl.getOptionValue("name");
            if (!nameParam.isEmpty()) name = nameParam;
            else name = args[0];
            lineToExec = input;
        }

        if (name != null)
            newHandler(builder.build(name, lineToExec), params, channel);
        else ZDSBMessages.printError(channel, "Error", "Cannot handle input");
    }

    public synchronized void newHandler(SubprocessListener sl, ExecHandler.Parameters params, MessageChannel channel) {
        processes.put(nextPid, new ExecHandler(sl, nextPid, channel, params));
        nextPid++;
    }

    private File toTemp(String code) {
        try {
            File f = File.createTempFile("tempCode", ".py");
            FileOutputStream fos = new FileOutputStream(f);
            fos.write(code.getBytes(StandardCharsets.UTF_8));
            fos.close();
            return f;
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    static ExecHandler findProcess(long id) {
        return processes.getOrDefault(id, null);
    }

    public static void removeProcess(long pid) {
        processes.remove(pid);
    }

    @Override
    public String getCommandName() {
        return "exec";
    }

    @Override
    public String getSynopsis() {
        return "exec [-c] <input>\n" +
                "exec [-sSb] <code in PL, with discord-highlighting>";
    }

    @Override
    public String getDescription() {
        return STR.getString("comms.exec.desc");
    }

    @Override
    public boolean checkPermission(MessageReceivedEvent event) {
        return false;
    }

    @Override
    public boolean allowPM() {
        return true;
    }

    @Override
    public boolean allowForeignGuilds() {
        return false;
    }
}
