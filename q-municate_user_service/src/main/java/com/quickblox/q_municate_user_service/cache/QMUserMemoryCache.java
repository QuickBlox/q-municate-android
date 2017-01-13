package com.quickblox.q_municate_user_service.cache;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Function;
import com.annimon.stream.function.Predicate;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.q_municate_user_service.model.QMUserColumns;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Observable;

public class QMUserMemoryCache extends Observable implements QMUserCache {

    private Map<Long, QMUser> usersMap = new HashMap<>();

    @Override
    public void create(QMUser object) {
        usersMap.put(object.getId().longValue(), object);
    }

    @Override
    public void createOrUpdate(QMUser object) {
        usersMap.put(object.getId().longValue(), object);
    }

    @Override
    public void createOrUpdateAll(Collection<QMUser> objectsCollection) {
        for(QMUser user : objectsCollection) {
            createOrUpdate(user);
        }
    }

    @Override
    public QMUser get(Long id) {
        return usersMap.get(id);
    }

    @Override
    public List<QMUser> getAll() {
        return new ArrayList<QMUser>(usersMap.values());
    }

    @Override
    public List<QMUser> getAllSorted(String sortedColumn, boolean ascending) {
        List<QMUser> result = null;
        result = Stream.of(usersMap.values()).sorted(new Comparator<QMUser>() {
            @Override
            public int compare(QMUser o1, QMUser o2) {
                return 0;
            }
        }).collect(Collectors.<QMUser>toList());

        return result;
    }

    @Override
    public void update(QMUser object) {
        usersMap.put(object.getId().longValue(), object);
    }

    @Override
    public void updateAll(Collection<QMUser> objectsCollection) {
        for(QMUser user : objectsCollection) {
            update(user);
        }
    }

    @Override
    public void delete(QMUser object) {
        usersMap.remove(object.getId().longValue());
    }

    @Override
    public void deleteById(Long id) {
        usersMap.remove(id);
    }

    @Override
    public boolean exists(Long id) {
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
    public List<QMUser> getUsersByIDs(final Collection<Integer> idsList) {
        List<QMUser> result = null;
        result = Stream.of(usersMap.values()).filter(new Predicate<QMUser>() {
            @Override
            public boolean test(QMUser value) {
                return idsList.contains(value.getId());
            }
        }).collect(Collectors.<QMUser>toList());

        return result;
    }

    @Override
    public QMUser getUserByColumn(final String column, final String value) {
        Optional<QMUser> result = Stream.of(usersMap.values()).filter(new Predicate<QMUser>() {
            @Override
            public boolean test(QMUser user) {
                return getValueByColumn(user, column).equals(value);
            }
        }).findFirst();

        return result.get();
    }

    @Override
    public List<QMUser> getUsersByFilter(Collection<?> filterValue, String filter) {
        return null;
    }

    @Override
    public List<QMUser> getByColumn(final String column, final String value) {
        List<QMUser> result = null;
        result = Stream.of(usersMap.values()).filter(new Predicate<QMUser>() {
            @Override
            public boolean test(QMUser user) {
                return  getValueByColumn(user, column).equals(value);
            }
        }).collect(Collectors.<QMUser>toList());

        return result;
    }

    @Override
    public List<QMUser> getByColumn(final String column, final Collection<String> values) {
        List<QMUser> result = null;
        result = Stream.of(usersMap.values()).filter(new Predicate<QMUser>() {
            @Override
            public boolean test(QMUser user) {
                return values.contains(getValueByColumn(user,column));
            }
        }).collect(Collectors.<QMUser>toList());

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
