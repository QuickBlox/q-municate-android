package com.quickblox.q_municate_user_service.cache;

import com.annimon.stream.Collectors;
import com.annimon.stream.Optional;
import com.annimon.stream.Stream;
import com.annimon.stream.function.Function;
import com.annimon.stream.function.Predicate;
import com.quickblox.q_municate_db.models.User;
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
    public QBUser getUser(long id) {
        return usersMap.get(id);
    }

    @Override
    public QBUser getUserByEmail(final String email) {
        Optional<QBUser> result = Stream.of(usersMap.values()).filter(new Predicate<QBUser>() {
            @Override
            public boolean test(QBUser value) {
                return value.getEmail().equals(email);
            }
        }).findFirst();

        return result.get();
    }

    @Override
    public QBUser getUserByLogin(final String login) {
        Optional<QBUser> result = Stream.of(usersMap.values()).filter(new Predicate<QBUser>() {
            @Override
            public boolean test(QBUser value) {
                return value.getLogin().equals(login);
            }
        }).findFirst();

        return result.get();
    }

    @Override
    public QBUser getUserByFacebookId(final String facebookId) {
        Optional<QBUser> result = Stream.of(usersMap.values()).filter(new Predicate<QBUser>() {
            @Override
            public boolean test(QBUser value) {
                return value.getFacebookId().equals(facebookId);
            }
        }).findFirst();

        return result.get();
    }

    @Override
    public QBUser getUserByTwitterId(final String twitterId) {
        Optional<QBUser> result = Stream.of(usersMap.values()).filter(new Predicate<QBUser>() {
            @Override
            public boolean test(QBUser value) {
                return value.getTwitterId().equals(twitterId);
            }
        }).findFirst();

        return result.get();
    }

    @Override
    public QBUser getUserByTwitterDigitsId(final String twitterDigitsId) {
        Optional<QBUser> result = Stream.of(usersMap.values()).filter(new Predicate<QBUser>() {
            @Override
            public boolean test(QBUser value) {
                return value.getTwitterDigitsId().equals(twitterDigitsId);
            }
        }).findFirst();

        return result.get();
    }

    @Override
    public QBUser getUserByExternalID(final String externalId) {
        Optional<QBUser> result = Stream.of(usersMap.values()).filter(new Predicate<QBUser>() {
            @Override
            public boolean test(QBUser value) {
                return value.getExternalId().equals(externalId);
            }
        }).findFirst();

        return result.get();
    }

    @Override
    public QBUser getUserByFullName(final String fullName) {
        Optional<QBUser> result = Stream.of(usersMap.values()).filter(new Predicate<QBUser>() {
            @Override
            public boolean test(QBUser value) {
                return value.getFullName().equals(fullName);
            }
        }).findFirst();

        return result.get();
    }

    @Override
    public QBUser getUserByTag(final String tag) {
        Optional<QBUser> result = Stream.of(usersMap.values()).filter(new Predicate<QBUser>() {
            @Override
            public boolean test(QBUser value) {
                return value.getTags().contains(tag);
            }
        }).findFirst();

        return result.get();
    }

    @Override
    public QBUser getUserByPhoneNumber(final String phoneNumber) {
        Optional<QBUser> result = Stream.of(usersMap.values()).filter(new Predicate<QBUser>() {
            @Override
            public boolean test(QBUser value) {
                return value.getPhone().equals(phoneNumber);
            }
        }).findFirst();

        return result.get();
    }

    @Override
    public void createUser(QBUser user) {
        usersMap.put(user.getId().longValue(), user);
    }

    @Override
    public void createOrUpdateUser(QBUser user) {
        usersMap.put(user.getId().longValue(), user);
    }

    @Override
    public void updateUser(QBUser user) {
        usersMap.put(user.getId().longValue(), user);
    }

    @Override
    public void deleteUser(QBUser user) {
        usersMap.remove(user.getId().longValue());
    }

    @Override
    public void deleteUserById(long id) {
        usersMap.remove(id);
    }

    @Override
    public void deleteUserByExternalId(String externalId) {

    }

    @Override
    public boolean existsUser(long id) {
        return usersMap.containsKey(id);
    }

    @Override
    public List<QBUser> getAllUsers() {
        return new ArrayList<QBUser>(usersMap.values());
    }

    @Override
    public List<QBUser> getAllUsersSorted(String sortedColumn, boolean ascending) {
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
    public QBUser getUserByColumn(String column, String value) {
        return null;
    }

    @Override
    public List<QBUser> getUsersByEmails(final Collection<String> usersEmails) {
        List<QBUser> result = null;
        result = Stream.of(usersMap.values()).filter(new Predicate<QBUser>() {
            @Override
            public boolean test(QBUser value) {
                return usersEmails.contains(value.getEmail());
            }
        }).collect(Collectors.<QBUser>toList());

        return result;
    }

    @Override
    public List<QBUser> getUsersByLogins(final Collection<String> usersLogins) {
        List<QBUser> result = null;
        result = Stream.of(usersMap.values()).filter(new Predicate<QBUser>() {
            @Override
            public boolean test(QBUser value) {
                return usersLogins.contains(value.getLogin());
            }
        }).collect(Collectors.<QBUser>toList());

        return result;
    }

    @Override
    public List<QBUser> getUsersByFacebookIds(final Collection<String> usersFacebookIds) {
        List<QBUser> result = null;
        result = Stream.of(usersMap.values()).filter(new Predicate<QBUser>() {
            @Override
            public boolean test(QBUser value) {
                return usersFacebookIds.contains(value.getFacebookId());
            }
        }).collect(Collectors.<QBUser>toList());

        return result;
    }

    @Override
    public List<QBUser> getUsersByTwitterIds(final Collection<String> usersTwitterIds) {
        List<QBUser> result = null;
        result = Stream.of(usersMap.values()).filter(new Predicate<QBUser>() {
            @Override
            public boolean test(QBUser value) {
                return usersTwitterIds.contains(value.getTwitterId());
            }
        }).collect(Collectors.<QBUser>toList());

        return result;
    }

    @Override
    public List<QBUser> getUsersByTwitterDigitsIds(final Collection<String> usersTwitterDigitsIds) {
        List<QBUser> result = null;
        result = Stream.of(usersMap.values()).filter(new Predicate<QBUser>() {
            @Override
            public boolean test(QBUser value) {
                return usersTwitterDigitsIds.contains(value.getTwitterDigitsId());
            }
        }).collect(Collectors.<QBUser>toList());

        return result;
    }

    @Override
    public List<QBUser> getUsersByExternalIds(final Collection<String> usersExternalIds) {
        List<QBUser> result = null;
        result = Stream.of(usersMap.values()).filter(new Predicate<QBUser>() {
            @Override
            public boolean test(QBUser value) {
                return usersExternalIds.contains(value.getExternalId());
            }
        }).collect(Collectors.<QBUser>toList());

        return result;
    }

    @Override
    public List<QBUser> getUsersByFullName(final String fullName) {
        List<QBUser> result = null;
        result = Stream.of(usersMap.values()).filter(new Predicate<QBUser>() {
            @Override
            public boolean test(QBUser value) {
                return fullName.equals(value.getFullName());
            }
        }).collect(Collectors.<QBUser>toList());

        return result;
    }

    @Override
    public List<QBUser> getUsersByTags(final Collection<String> tags) {
        List<QBUser> result = null;
        result = Stream.of(usersMap.values()).filter(new Predicate<QBUser>() {
            @Override
            public boolean test(QBUser value) {
                return tags.contains(value.getTags());
            }
        }).collect(Collectors.<QBUser>toList());

        return result;
    }

    @Override
    public List<QBUser> getUsersByPhoneNumbers(final Collection<String> usersPhoneNumbers) {
        List<QBUser> result = null;
        result = Stream.of(usersMap.values()).filter(new Predicate<QBUser>() {
            @Override
            public boolean test(QBUser value) {
                return usersPhoneNumbers.contains(value.getPhone());
            }
        }).collect(Collectors.<QBUser>toList());

        return result;
    }

    @Override
    public List<QBUser> getUsersByFilter(Collection<?> filterValue, String filter) {
        return null;
    }

    @Override
    public List<QBUser> getUsersByColumn(String column, String value) {
        return null;
    }

    @Override
    public List<QBUser> getUsersByColumn(String column, Collection<String> values) {
        return null;
    }

    @Override
    public void createOrUpdateAllUsers(Collection<QBUser> users) {
        for(QBUser user : users) {
            usersMap.put(user.getId().longValue(), user);
        }
    }

    @Override
    public void updateAllUsers(Collection<QBUser> users) {
        for(QBUser user : users) {
            usersMap.put(user.getId().longValue(), user);
        }
    }

    @Override
    public void create(QBUser object) {

    }

    @Override
    public void createOrUpdate(QBUser object) {

    }

    @Override
    public void createOrUpdateAll(Collection<QBUser> objectsCollection) {

    }

    @Override
    public QBUser get(long id) {
        return null;
    }

    @Override
    public List<QBUser> getAll() {
        return null;
    }

    @Override
    public List<QBUser> getAllSorted(String sortedColumn, boolean ascending) {
        return null;
    }

    @Override
    public void update(QBUser object) {

    }

    @Override
    public void updateAll(Collection<QBUser> objectsCollection) {

    }

    @Override
    public void delete(QBUser object) {

    }

    @Override
    public void deleteById(long id) {

    }

    @Override
    public boolean exists(long id) {
        return false;
    }

    @Override
    public void clear() {
        usersMap.clear();
    }
}
