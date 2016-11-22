package com.quickblox.q_municate_user_service.cache;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Function;
import com.annimon.stream.function.Predicate;
import com.quickblox.q_municate_db.models.User;
import com.quickblox.q_municate_user_service.model.QMUserColumns;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public class QMUserMemoryCache implements QMUserCache {

    private Map<Long, QBUser> usersMap = new HashMap<>();

    @Override
    public void create(QBUser object) {
        usersMap.put(object.getId().longValue(), object);
    }

    @Override
    public void createOrUpdate(QBUser object) {
        usersMap.put(object.getId().longValue(), object);
    }

    @Override
    public void createOrUpdateAll(Collection<QBUser> objectsCollection) {
        for(QBUser user : objectsCollection) {
            createOrUpdate(user);
        }
    }

    @Override
    public QBUser get(long id) {
        return usersMap.get(id);
    }

    @Override
    public List<QBUser> getAll() {
        return new ArrayList<QBUser>(usersMap.values());
    }

    @Override
    public List<QBUser> getAllSorted(String sortedColumn, boolean ascending) {
        List<QBUser> result = null;
        result = Stream.of(usersMap.values()).sorted(new Comparator<QBUser>() {
            @Override
            public int compare(QBUser o1, QBUser o2) {
                return 0;
            }
        }).collect(Collectors.<QBUser>toList());

        return result;
    }

    @Override
    public void update(QBUser object) {
        usersMap.put(object.getId().longValue(), object);
    }

    @Override
    public void updateAll(Collection<QBUser> objectsCollection) {
        for(QBUser user : objectsCollection) {
            update(user);
        }
    }

    @Override
    public void delete(QBUser object) {
        usersMap.remove(object.getId().longValue());
    }

    @Override
    public void deleteById(long id) {
        usersMap.remove(id);
    }

    @Override
    public boolean exists(long id) {
        return usersMap.containsKey(id);
    }

    @Override
    public void clear() {
        usersMap.clear();
    }

    @Override
    public void deleteUserByExternalId(String externalId) {

    }

    @Override
    public List<QBUser> getUsersByIDs(final Collection<Integer> idsList) {
        List<QBUser> result = null;
        result = Stream.of(usersMap.values()).filter(new Predicate<QBUser>() {
            @Override
            public boolean test(QBUser value) {
                return idsList.contains(value.getId());
            }
        }).collect(Collectors.<QBUser>toList());

        return result;
    }

    @Override
    public QBUser getUserByColumn(final String column, final String value) {
        Optional<QBUser> result = Stream.of(usersMap.values()).filter(new Predicate<QBUser>() {
            @Override
            public boolean test(QBUser user) {
                return getValueByColumn(user, column).equals(value);
            }
        }).findFirst();

        return result.get();
    }

    @Override
    public List<QBUser> getUsersByFilter(Collection<?> filterValue, String filter) {
        return null;
    }

    @Override
    public List<QBUser> getUsersByColumn(final String column, final String value) {
        List<QBUser> result = null;
        result = Stream.of(usersMap.values()).filter(new Predicate<QBUser>() {
            @Override
            public boolean test(QBUser user) {
                return  getValueByColumn(user, column).equals(value);
            }
        }).collect(Collectors.<QBUser>toList());

        return result;
    }

    @Override
    public List<QBUser> getUsersByColumn(final String column, final Collection<String> values) {
        List<QBUser> result = null;
        result = Stream.of(usersMap.values()).filter(new Predicate<QBUser>() {
            @Override
            public boolean test(QBUser user) {
                return values.contains(getValueByColumn(user,column));
            }
        }).collect(Collectors.<QBUser>toList());

        return result;
    }

    private String getValueByColumn(QBUser user, String column){
        String result = null;
        switch (column){
            case  QMUserColumns.ID:
                result = user.getId().toString();
                break;
            case QMUserColumns.FULL_NAME:
                result = user.getFullName();
                break;
            case QMUserColumns.EMAIL:
                result = user.getEmail();
                break;
            case QMUserColumns.LOGIN:
                result = user.getEmail();
                break;
            case QMUserColumns.PHONE:
                result = user.getPhone();
                break;
            case QMUserColumns.WEBSITE:
                result = user.getWebsite();
                break;
            case QMUserColumns.LAST_REQUEST_AT:
                result = user.getLastRequestAt().toString();
                break;
            case QMUserColumns.EXTERNAL_ID:
                result = user.getExternalId();
                break;
            case QMUserColumns.FACEBOOK_ID:
                result = user.getFacebookId();
                break;
            case QMUserColumns.TWITTER_ID:
                result = user.getTwitterId();
                break;
            case QMUserColumns.TWITTER_DIGITS_ID:
                result = user.getTwitterDigitsId();
                break;
            case QMUserColumns.BLOB_ID:
                result = null;
            case QMUserColumns.TAGS:
                result = null;
                break;
            case QMUserColumns.PASSWORD:
                result = user.getPassword();
                break;
            case QMUserColumns.OLD_PASSWORD:
                result = user.getOldPassword();
                break;
            case QMUserColumns.CUSTOM_DATE:
                result = user.getCustomData();
                break;

        }
        return result;
    }
}
