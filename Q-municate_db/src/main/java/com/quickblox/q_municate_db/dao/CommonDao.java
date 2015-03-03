package com.quickblox.q_municate_db.dao;

import com.j256.ormlite.dao.Dao;

import java.util.List;

public interface CommonDao<T> {

    public Dao.CreateOrUpdateStatus createOrUpdate(T item);

    public List<T> getAll();

    public T get(int id);

    public void update(T item);

    public void delete(T item);
}