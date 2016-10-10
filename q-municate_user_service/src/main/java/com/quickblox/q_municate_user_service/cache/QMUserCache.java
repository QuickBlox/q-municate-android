package com.quickblox.q_municate_user_service.cache;

import com.quickblox.q_municate_base_cache.QMBaseCache;
import com.quickblox.users.model.QBUser;

import java.util.Collection;
import java.util.List;

import rx.Observable;

public interface QMUserCache extends QMBaseCache {

    QBUser getUser(long id);

    QBUser getUserByEmail(String email);

    QBUser getUserByLogin(String login);

    QBUser getUserByFacebookId(String facebookId);

    QBUser getUserByTwitterId(String twitterId);

    QBUser getUserByTwitterDigitsId(String twitterDigitsId);

    QBUser getUserByExternalID(String externalId);

    QBUser getUserByFullName(String fullName);

    QBUser getUserByTag(String tag);

    QBUser getUserByPhoneNumber(String phoneNumber);

    void createUser(QBUser user);

    void createOrUpdateUser(QBUser user);

    void updateUser(QBUser user);

    void deleteUser(QBUser user);

    void deleteUserById(long id);

    void deleteUserByExternalId(String externalId);

    boolean existsUser(long id);


    List<QBUser> getAllUsers();

    List<QBUser> getAllUsersSorted(String sortedColumn, boolean ascending);

    List<QBUser> getUsersByIDs(Collection<Integer> idsList) ;

    List<QBUser> getUsersByEmails(Collection<String> usersEmails);

    List<QBUser> getUsersByLogins(Collection<String> usersLogins);

    List<QBUser> getUsersByFacebookIds(Collection<String> usersFacebookIds);

    List<QBUser> getUsersByTwitterIds(Collection<String> usersTwitterIds);

    List<QBUser> getUsersByTwitterDigitsIds(Collection<String> usersTwitterDigitsIds);

    List<QBUser> getUsersByExternalIds(Collection<String> usersExternalIds);

    List<QBUser> getUsersByFullName(String fullName);

    List<QBUser> getUsersByTags(Collection<String> tags);

    List<QBUser> getUsersByPhoneNumbers(Collection<String> usersPhoneNumbers);

    List<QBUser> getUsersByFilter(Collection<?> filterValue, String filter);

    void createOrUpdateAllUsers(Collection<QBUser> users);

    void updateAllUsers(Collection<QBUser> users);
}
