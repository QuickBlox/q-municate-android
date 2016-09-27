package com.quickblox.q_municate_user_cache;


import com.quickblox.q_municate_base_cache.QMBaseCache;
import com.quickblox.q_municate_user_cache.model.User;

import java.util.Collection;
import java.util.List;

public class QMUserCache implements QMBaseCache<User> {

    @Override
    public void create(User object) {

    }

    @Override
    public void createOrUpdate(User object) {

    }

    @Override
    public void createOrUpdate(User object, boolean notify) {

    }

    @Override
    public void createOrUpdateAll(Collection<User> objectsCollection) {

    }

    @Override
    public User get(long id) {
        return null;
    }

    @Override
    public List<User> getAll() {
        return null;
    }

    @Override
    public List<User> getAllSorted(String sortedColumn, boolean ascending) {
        return null;
    }

    @Override
    public void update(User object) {

    }

    @Override
    public void update(User object, boolean notify) {

    }

    @Override
    public void updateAll(Collection<User> objectsCollection) {

    }

    @Override
    public void delete(User object) {

    }

    @Override
    public void deleteById(long id) {

    }

    @Override
    public boolean exists(long id) {
        return false;
    }
}
