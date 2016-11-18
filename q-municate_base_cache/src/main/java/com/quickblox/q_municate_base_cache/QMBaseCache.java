package com.quickblox.q_municate_base_cache;

import java.util.Collection;
import java.util.List;

public interface QMBaseCache<T> {

    void create(T object);

    void createOrUpdate(T object);

    void createOrUpdateAll(Collection<T> objectsCollection);

    T get(long id);

    List<T> getAll();

    List<T> getAllSorted(String sortedColumn, boolean ascending);

    void update(T object);

    void updateAll(Collection<T> objectsCollection);

    void delete(T object);

    void deleteById(long id);

    boolean exists(long id);

    void clear();
}