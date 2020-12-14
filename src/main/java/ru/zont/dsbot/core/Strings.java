package ru.zont.dsbot.core;

import java.util.ResourceBundle;

public class Strings {
    public static final ResourceBundle STR = ResourceBundle.getBundle("strings", new UTF8Control());

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
        return original.substring(0, Math.min(count, length)) + "...";
    }
}
