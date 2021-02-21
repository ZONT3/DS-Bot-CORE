package ru.zont.dsbot2;

@SuppressWarnings("unchecked")
public class ConfigCaster<T extends Config> {
    public T cast(Class<T> klass, Config config) {
        return (T) config;
    }
}
