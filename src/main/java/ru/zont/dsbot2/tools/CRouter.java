package ru.zont.dsbot2.tools;

import ru.zont.dsbot2.commands.Input;
import ru.zont.dsbot2.commands.UserInvalidInputException;

import java.util.HashMap;

public class CRouter {
    private final int index;
    private final HashMap<String, Case> map;
    private String error = ZDSBStrings.STR.getString("err.invalid_arg");

    public CRouter(int index) {
        this.index = index;
        map = new HashMap<>();
    }

    public CRouter setError(String error) {
        this.error = error;
        return this;
    }

    public CRouter addCase(Case c, String... keys) {
        for (String key: keys) map.put(key, c);
        return this;
    }

    public void acceptInput(Input input) {
        String[] args = input.getArgs();
        if (args.length < index + 1)
            throw new UserInvalidInputException(ZDSBStrings.STR.getString("err.insufficient_args"));
        final Case thisCase = map.get(args[index]);
        if (thisCase == null) throw new UserInvalidInputException(error);
        thisCase.accept(input);
    }

    public interface Case {
        void accept(Input input);
    }
}
