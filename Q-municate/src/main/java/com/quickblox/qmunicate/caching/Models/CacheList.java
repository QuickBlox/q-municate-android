package com.quickblox.qmunicate.caching.Models;

import com.j256.ormlite.dao.ForeignCollection;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.field.ForeignCollectionField;
import com.j256.ormlite.table.DatabaseTable;

import java.util.ArrayList;
import java.util.List;

@DatabaseTable
public class CacheList<T> {

    @DatabaseField(generatedId = true)
    private int id;

    @DatabaseField
    private String name;

    @ForeignCollectionField
    private ForeignCollection<T> cacheList;

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public List<T> getItems() {
        ArrayList<T> itemsList = new ArrayList<T>();
        for (T item : cacheList) {
            itemsList.add(item);
        }
        return itemsList;
    }

    public void setItems(ForeignCollection<T> friendsList) {
        this.cacheList = friendsList;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}