package ru.zont.dsbot2.tools;

import java.io.*;
import java.util.function.Consumer;

public class Data<T extends Serializable> {
    public static final File DIR = new File("db");
    private final File file;

    protected T data = null;

    public Data(String name) {
        file = new File(DIR, name + ".bin");
    }

    @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
    protected void load() {
        DIR.mkdirs();
        if (!file.exists()) {
            data = null;
            return;
        }
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream in = new ObjectInputStream(fis)) {
            data = (T) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    protected void commit() {
        DIR.mkdirs();
        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream out = new ObjectOutputStream(fos)) {
            out.writeObject(data);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public T getData() {
        return data;
    }

    /**
     * Operate on object under consumer, then commit modified
     * @param consumer Operations with collection
     */
    public void op(Consumer<T> consumer) {
        load();
        consumer.accept(data);
        commit();
    }

    /**
     * {@link Data#op(Consumer)} but if data is null, will be replaced by {@code defaultValue}
     * @param defaultValue Default value
     * @param consumer Operations with collection
     */
    public void op(T defaultValue, Consumer<T> consumer) {
        load();
        if (data == null) data = defaultValue;
        consumer.accept(data);
        commit();
    }

    /**
     * {@link Data#op(T, Consumer)}
     * @param defaultValue Default value
     * @param callback Operations and result
     * @param <R> callback's result type
     */
    public <R> R op(T defaultValue, Callback<T, R> callback) {
        load();
        if (data == null) data = defaultValue;
        R res = callback.call(data);
        commit();
        return res;
    }

    public interface Callback<A, R> {
        R call(A arg);
    }
}
