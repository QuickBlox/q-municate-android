package com.quickblox.q_municate_db.managers;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_db.dao.CommonDao;
import com.quickblox.q_municate_db.models.Role;
import com.quickblox.q_municate_db.utils.ErrorUtils;

import java.sql.SQLException;
import java.util.List;

public class RoleManager implements CommonDao<Role> {

    private Dao<Role, Integer> roleDao;

    public RoleManager(Dao<Role, Integer> roleDao) {
        this.roleDao = roleDao;
    }

    @Override
    public Role createIfNotExists(Role item) {
        try {
            return roleDao.createIfNotExists(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return null;
    }

    @Override
    public List<Role> getAll() {
        List<Role> roleList = null;
        try {
            roleList = roleDao.queryForAll();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return roleList;
    }

    @Override
    public Role get(int id) {
        Role role = null;
        try {
            role = roleDao.queryForId(id);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return role;
    }

    @Override
    public void update(Role item) {
        try {
            roleDao.update(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    @Override
    public void delete(Role item) {
        try {
            roleDao.delete(item);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    public Role getByRoleType(Role.Type type) {
        Role role = null;
        try {
            QueryBuilder<Role, Integer> queryBuilder = roleDao.queryBuilder();
            queryBuilder.where().eq(Role.COLUMN_ROLE, type);
            PreparedQuery<Role> preparedQuery = queryBuilder.prepare();
            role = roleDao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
        return role;
    }
}