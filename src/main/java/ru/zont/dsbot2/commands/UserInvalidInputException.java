package ru.zont.dsbot2.commands;

public class UserInvalidInputException extends RuntimeException {
    public final boolean printSyntax;

    public UserInvalidInputException(String s) {
        this(s, true);
    }

    public UserInvalidInputException(String s, boolean printSyntax) {
        super(s);
        this.printSyntax = printSyntax;
    }
}
