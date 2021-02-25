package ru.zont.dsbot2;

@SuppressWarnings("unchecked")
public class ConfigCaster {
    public static <T extends Config> T cast(Config config) {
        return (T) config;
    }
}
