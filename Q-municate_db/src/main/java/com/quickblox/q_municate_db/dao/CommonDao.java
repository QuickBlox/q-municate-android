package com.quickblox.q_municate_db.dao;

import java.util.List;

public interface CommonDao<T> {

    public T createIfNotExists(T item);

    public List<T> getAll();

    public T get(int id);

    public void update(T item);

    public void delete(T item);
}