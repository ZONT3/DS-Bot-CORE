package ru.zont.dsbot2;

public class DescribedException extends RuntimeException {
    private final String title;
    private final String description;

    public DescribedException(String title, String description) {
        super(title);
        this.title = title;
        this.description = description;
    }

    public DescribedException(String title, String description, Throwable cause) {
        super(description, cause);
        this.title = title;
        this.description = description;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }
}
