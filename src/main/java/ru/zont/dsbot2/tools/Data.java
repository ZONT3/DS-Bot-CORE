package ru.zont.dsbot2.tools;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class Data<T extends Serializable> {
    public static final File DIR = new File("db");
    private final File file;

    private List<T> list = null;

    public Data(String name) {
        file = new File(DIR, name + ".bin");
    }

    @SuppressWarnings({"unchecked", "ResultOfMethodCallIgnored"})
    private void load() {
        DIR.mkdirs();
        if (!file.exists()) {
            list = new ArrayList<>();
            return;
        }
        try (FileInputStream fis = new FileInputStream(file);
             ObjectInputStream in = new ObjectInputStream(fis)) {
            list = (List<T>) in.readObject();
        } catch (IOException | ClassNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    private void commit() {
        DIR.mkdirs();
        try (FileOutputStream fos = new FileOutputStream(file);
             ObjectOutputStream out = new ObjectOutputStream(fos)) {
            out.writeObject(list);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public void op(Consumer<List<T>> consumer) {
        load();
        consumer.accept(list);
        commit();
    }

    public List<T> get() {
        load();
        return new ArrayList<>(list);
    }
}
