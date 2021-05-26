package ru.zont.dsbot2.tools;

import ru.zont.dsbot2.UTF8Control;

import javax.annotation.PropertyKey;
import java.util.ResourceBundle;

public class ZDSBStrings {
    public static final ResourceBundle STR_CORE = ResourceBundle.getBundle("strings_core", new UTF8Control());

    public static class STR {
        public static String getString(@PropertyKey String key) {
            if (STR_CORE.containsKey(key))
                return STR_CORE.getString(key);
            return key;
        }
    }

    public static String getPlural(int count, String one, String few, String other) {
        int c = (count % 100);

        if (c == 1 || (c > 20 && c % 10 == 1))
            return String.format(one, count);
        if ((c < 10 || c > 20) && c % 10 >= 2 && c % 10 <= 4)
            return String.format(few, count);
        return String.format(other, count);
    }

    public static String trimSnippet(String original, int count) {
        int length = original.length();
        if (length < count) return original;
        return original.substring(0, Math.min(count, length)) + "...";
    }
}
