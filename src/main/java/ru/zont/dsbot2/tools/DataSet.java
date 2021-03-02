package ru.zont.dsbot2.tools;

import java.io.Serializable;
import java.util.HashSet;

public class DataSet<T extends Serializable> extends Data<HashSet<T>> {
    public DataSet(String name) {
        super(name);
    }

    @Override
    protected void load() {
        super.load();
        if (super.data == null)
            super.data = new HashSet<>();
    }

    @Override
    public HashSet<T> getData() {
        load();
        return new HashSet<>(super.getData());
    }
}
