package com.quickblox.q_municate_db.managers.base;

import java.util.Collection;
import java.util.List;

public interface Manager<T> {

    void create(T object);

    void createOrUpdate(T object);

    void createOrUpdate(T object, boolean notify);

    void createOrUpdateAll(Collection<T> objectsCollection);

    T get(long id);

    List<T> getAll();

    List<T> getAllSorted(String sortedColumn, boolean ascending);

    void update(T object);

    void update(T object, boolean notify);

    void updateAll(Collection<T> objectsCollection);

    void delete(T object);

    //TODO VT some objects have id with type 'String'. Need review this method
    void deleteById(long id);

    boolean exists(long id);
}