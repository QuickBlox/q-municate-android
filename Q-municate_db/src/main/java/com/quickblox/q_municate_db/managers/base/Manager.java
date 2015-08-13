package com.quickblox.q_municate_db.managers.base;

import java.util.Collection;
import java.util.List;

public interface Manager<T> {

    void create(T object);

    void createOrUpdate(T object);

    void createOrUpdate(Collection<T> objectsCollection);

    T get(long id);

    List<T> getAll();

    void update(T object);

    void delete(T object);

    void deleteById(long id);

    boolean exists(long id);
}