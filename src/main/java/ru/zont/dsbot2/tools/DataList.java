package ru.zont.dsbot2.tools;

import java.io.*;
import java.util.ArrayList;

public class DataList<T extends Serializable> extends Data<ArrayList<T>> {

    public DataList(String name) {
        super(name);
    }

    @Override
    protected void load() {
        super.load();
        if (super.data == null)
            super.data = new ArrayList<>();
    }

    @Override
    public ArrayList<T> getData() {
        load();
        return new ArrayList<>(super.getData());
    }
}
