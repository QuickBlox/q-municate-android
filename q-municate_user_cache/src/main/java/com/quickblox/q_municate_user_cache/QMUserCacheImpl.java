package com.quickblox.q_municate_user_cache;


import android.content.Context;

import com.j256.ormlite.dao.Dao;
import com.j256.ormlite.stmt.DeleteBuilder;
import com.j256.ormlite.stmt.PreparedDelete;
import com.j256.ormlite.stmt.PreparedQuery;
import com.j256.ormlite.stmt.QueryBuilder;
import com.quickblox.q_municate_base_cache.utils.ErrorUtils;
import com.quickblox.q_municate_user_cache.model.QMUserColumns;
import com.quickblox.q_municate_user_service.cache.QMUserCache;
import com.quickblox.users.model.QBUser;

import java.sql.SQLException;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;


public class QMUserCacheImpl implements QMUserCache, QMUserColumns {

    private static final String TAG = QMUserCacheImpl.class.getSimpleName();

    private QMUserDataHelper dataHelper;
    private Dao<QBUser, Long> userDao;

    public QMUserCacheImpl(Context context) {
        dataHelper = new QMUserDataHelper(context);
        userDao = dataHelper.getDaoByClass(QBUser.class);
    }

    private QMUserDataHelper getDataHelper() {
        return dataHelper;
    }


    @Override
    public QBUser getUser(long id) {
        try {
            return userDao.queryForId(id);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return null;
    }

    @Override
    public QBUser getUserByEmail(String email) {
        return getUserByColumn(EMAIL, email);
    }

    @Override
    public QBUser getUserByLogin(String login) {
        return getUserByColumn(LOGIN, login);
    }

    @Override
    public QBUser getUserByFacebookId(String facebookId) {
        return getUserByColumn(FACEBOOK_ID, facebookId);
    }

    @Override
    public QBUser getUserByTwitterId(String twitterId) {
        return getUserByColumn(TWITTER_ID, twitterId);
    }

    @Override
    public QBUser getUserByTwitterDigitsId(String twitterDigitsId) {
        return getUserByColumn(TWITTER_DIGITS_ID, twitterDigitsId);
    }

    @Override
    public QBUser getUserByExternalID(String externalId) {
        return getUserByColumn(EXTERNAL_ID, externalId);
    }

    @Override
    public QBUser getUserByFullName(String fullName) {
        return getUserByColumn(FULL_NAME, fullName);
    }

    @Override
    public QBUser getUserByTag(String tag) {
        return getUserByColumn(TAGS, tag);
    }

    @Override
    public QBUser getUserByPhoneNumber(String phoneNumber) {
        return null;
    }

    @Override
    public void createUser(QBUser user) {
        try {
            userDao.create(user);
        } catch (SQLException e) {
            ErrorUtils.logError(TAG, "create() - " + e.getMessage());
        }
    }

    @Override
    public void createOrUpdateUser(QBUser user) {
        try {
            userDao.createOrUpdate(user);
        } catch (SQLException e) {
            ErrorUtils.logError(TAG, "createOrUpdateUser(Object) - " + e.getMessage());
        }
    }

    @Override
    public void updateUser(QBUser user) {
        try {
            userDao.update(user);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }


    @Override
    public void deleteUser(QBUser user) {
        try {
            userDao.delete(user);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    @Override
    public void deleteUserById(long id) {
        try {
            userDao.deleteById(id);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    @Override
    public void deleteUserByExternalId(String externalId) {
        try {
            DeleteBuilder<QBUser, Long> deleteBuilder = userDao.deleteBuilder();
            deleteBuilder.where().eq(EXTERNAL_ID,externalId);
            PreparedDelete<QBUser> preparedQuery = deleteBuilder.prepare();
            userDao.delete(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }

    @Override
    public boolean existsUser(long id) {
        try {
            return userDao.idExists(id);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return false;
    }

    @Override
    public List<QBUser> getAllUsers() {
        try {
            return userDao.queryForAll();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return Collections.emptyList();
    }

    @Override
    public List<QBUser> getAllUsersSorted(String sortedColumn, boolean ascending) {
        List<QBUser> usersList  = Collections.emptyList();

        try {
            QueryBuilder<QBUser, Long> queryBuilder = userDao.queryBuilder();
            queryBuilder.orderBy(sortedColumn, ascending);
            PreparedQuery<QBUser> preparedQuery = queryBuilder.prepare();
            usersList  = userDao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return usersList;
    }

    @Override
    public List<QBUser> getUsersByIDs(Collection<Integer> idsList) {
        List<QBUser> usersList  = Collections.emptyList();

        try {
            QueryBuilder<QBUser, Long> queryBuilder = userDao.queryBuilder();
            queryBuilder.where().in(ID, idsList);
            PreparedQuery<QBUser> preparedQuery = queryBuilder.prepare();
            usersList = userDao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return usersList;
    }

    @Override
    public List<QBUser> getUsersByEmails(Collection<String> usersEmails) {
        return getUsersByColumn(EMAIL, usersEmails);
    }

    @Override
    public List<QBUser> getUsersByLogins(Collection<String> usersLogins) {
        return getUsersByColumn(LOGIN, usersLogins);
    }

    @Override
    public List<QBUser> getUsersByFacebookIds(Collection<String> usersFacebookIds) {
        return getUsersByColumn(FACEBOOK_ID, usersFacebookIds);
    }

    @Override
    public List<QBUser> getUsersByTwitterIds(Collection<String> usersTwitterIds) {
        return getUsersByColumn(TWITTER_ID, usersTwitterIds);
    }

    @Override
    public List<QBUser> getUsersByTwitterDigitsIds(Collection<String> usersTwitterDigitsIds) {
        return getUsersByColumn(TWITTER_DIGITS_ID, usersTwitterDigitsIds);
    }

    @Override
    public List<QBUser> getUsersByExternalIds(Collection<String> usersExternalIds) {
        return getUsersByColumn(EXTERNAL_ID, usersExternalIds);
    }

    @Override
    public List<QBUser> getUsersByFullName(String fullName) {
        return getUsersByColumn(FULL_NAME, fullName);
    }

    @Override
    public List<QBUser> getUsersByTags(Collection<String> tags) {
        return getUsersByColumn(TAGS, tags);
    }

    @Override
    public List<QBUser> getUsersByPhoneNumbers(Collection<String> usersPhoneNumbers) {
        return getUsersByColumn(PHONE, usersPhoneNumbers);
    }

    @Override
    public List<QBUser> getUsersByFilter(Collection<?> filterValue, String filter) {
        return null;
    }

    @Override
    public void createOrUpdateAllUsers(final Collection<QBUser> usersCollection) {
        try {
            userDao.callBatchTasks(new Callable() {
                @Override
                public QBUser call() throws Exception {
                    for (QBUser user : usersCollection) {
                        createOrUpdateUser(user);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            ErrorUtils.logError(TAG, "createOrUpdateAllUsers(Collection) - " + e.getMessage());
        }
    }

    @Override
    public void updateAllUsers(final Collection<QBUser> usersCollection) {
        try {
            userDao.callBatchTasks(new Callable() {
                @Override
                public QBUser call() throws Exception {
                    for (QBUser user : usersCollection) {
                        updateUser(user);
                    }
                    return null;
                }
            });
        } catch (Exception e) {
            ErrorUtils.logError(TAG, "updateAllUsers(Collection) - " + e.getMessage());
        }
    }


    private QBUser getUserByColumn(String column, String value) {
        QBUser user = null;

        try {
            QueryBuilder<QBUser, Long> queryBuilder = userDao.queryBuilder();
            queryBuilder.where().eq(column, value);
            PreparedQuery<QBUser> preparedQuery = queryBuilder.prepare();
            user = userDao.queryForFirst(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return user;
    }

    private List<QBUser> getUsersByColumn(String column, Collection<String> values) {
        List<QBUser> usersList  = Collections.emptyList();

        try {
            QueryBuilder<QBUser, Long> queryBuilder = userDao.queryBuilder();
            queryBuilder.where().in(column, values);
            PreparedQuery<QBUser> preparedQuery = queryBuilder.prepare();
            usersList = userDao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return usersList;
    }

    private List<QBUser> getUsersByColumn(String column, String value) {
        List<QBUser> usersList  = Collections.emptyList();

        try {
            QueryBuilder<QBUser, Long> queryBuilder = userDao.queryBuilder();
            queryBuilder.where().eq(column, value);
            PreparedQuery<QBUser> preparedQuery = queryBuilder.prepare();
            usersList = userDao.query(preparedQuery);
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }

        return usersList;
    }

    @Override
    public void clear() {
        DeleteBuilder<QBUser, Long> deleteBuilder = userDao.deleteBuilder();
        try {
            deleteBuilder.delete();
        } catch (SQLException e) {
            ErrorUtils.logError(e);
        }
    }
}
