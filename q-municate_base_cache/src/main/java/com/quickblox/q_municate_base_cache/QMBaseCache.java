package com.quickblox.q_municate_base_cache;

import com.quickblox.users.model.QBUser;

import java.util.Collection;
import java.util.List;

public interface QMBaseCache<T, ID> {

    void create(T object);

    void createOrUpdate(T object);

    void createOrUpdateAll(Collection<T> objectsCollection);

    T get(ID id);

    List<T> getAll();

    List<T> getAllSorted(String sortedColumn, boolean ascending);

    List<T> getByColumn(String column, String value);

    List<T> getByColumn(String column, Collection<String> values);

    void update(T object);

    void updateAll(Collection<T> objectsCollection);

    void delete(T object);

    void deleteById(ID id);

    boolean exists(ID id);

    void clear();
}