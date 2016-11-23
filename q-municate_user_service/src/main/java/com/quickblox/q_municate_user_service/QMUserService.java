package com.quickblox.q_municate_user_service;


import com.quickblox.core.QBSettings;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.server.Performer;
import com.quickblox.extensions.RxJavaPerformProcessor;
import com.quickblox.q_municate_base_service.QMBaseService;
import com.quickblox.q_municate_base_service.QMServiceManagerListener;
import com.quickblox.q_municate_user_service.cache.QMUserCache;
import com.quickblox.q_municate_user_service.cache.QMUserMemoryCache;
import com.quickblox.q_municate_user_service.model.QMUserColumns;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;
import com.quickblox.users.query.QueryGetUsers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

public class QMUserService extends QMBaseService {

    @Inject
    protected QMUserCache userCache;

    public QMUserService(){
        super.init(userCache) ;
    }

    public QMUserService(QMUserCache userCache){
        this.userCache = userCache != null ? userCache : new QMUserMemoryCache();
        super.init(userCache) ;
    }

    @Override
    protected void serviceWillStart() {
    }

    public Observable<QBUser> getUser(final int userId){
        return getUser(userId, true);
    }

    public Observable<QBUser> getUser(final int userId, boolean forceLoad){
        return  getUserByColumn(QMUserColumns.ID, String.valueOf(userId), forceLoad);

    }

    public Observable<QBUser> getUserByLogin(final String login){
        return getUserByLogin(login, true);
    }

    public Observable<QBUser> getUserByLogin(final String login, boolean forceLoad){
        return  getUserByColumn(QMUserColumns.LOGIN, login, forceLoad);
    }

    public Observable<QBUser> getUserByFacebookId(final String facebookId){
        return getUserByFacebookId(facebookId, true);
    }

    public Observable<QBUser> getUserByFacebookId(final String facebookId, boolean forceLoad){
        return  getUserByColumn(QMUserColumns.FACEBOOK_ID, facebookId, forceLoad);

    }

    public Observable<QBUser> getUserByTwitterId(final String twitterId){
        return getUserByTwitterId(twitterId, true);
    }

    public Observable<QBUser> getUserByTwitterId(final String twitterId, boolean forceLoad) {
        return getUserByColumn(QMUserColumns.TWITTER_ID, twitterId, forceLoad);
    }

    public Observable<QBUser> getUserByTwitterDigitsId(final String twitterDigitsId){
        return getUserByTwitterDigitsId(twitterDigitsId, true);
    }

    public Observable<QBUser> getUserByTwitterDigitsId(final String twitterDigitsId, boolean forceLoad){
        return getUserByColumn(QMUserColumns.TWITTER_DIGITS_ID, twitterDigitsId, forceLoad);
    }

    public Observable<QBUser> getUserByEmail(final String email){
        return getUserByEmail(email, true);
    }

    public Observable<QBUser> getUserByEmail(final String email, boolean forceLoad){
        return getUserByColumn(QMUserColumns.EMAIL, email, forceLoad);
    }

    public Observable<QBUser> getUserByExternalId(final String externalId){
        return getUserByExternalId(externalId, true);
    }

    public Observable<QBUser> getUserByExternalId(final String externalId, boolean forceLoad){
        return getUserByColumn(QMUserColumns.EXTERNAL_ID, externalId, forceLoad);
    }

    public Observable<QBUser> updateUser(final QBUser user){
        Observable<QBUser> result = null;

        Performer<QBUser> performer = QBUsers.updateUser(user);
        final Observable<QBUser> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.flatMap(new Func1<QBUser, Observable<QBUser>>() {
            @Override
            public Observable<QBUser> call(QBUser qbUser) {
                userCache.update(qbUser);
                return observable;
            }
        });

        return result;
    }

    public Observable<Void> deleteUser(final int userId){
        Observable<Void> result = null;

        Performer<Void> performer = QBUsers.deleteUser(userId);
        final Observable<Void> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.flatMap(new Func1<Void, Observable<Void>>() {
            @Override
            public Observable<Void> call(Void qbUser) {
                userCache.deleteById(Long.valueOf(userId));
                return observable;
            }
        });

        return result;
    }

    public Observable<Void> deleteByExternalId(final String externalId){
        Observable<Void> result = null;

        Performer<Void> performer = QBUsers.deleteByExternalId(externalId);
        final Observable<Void> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.flatMap(new Func1<Void, Observable<Void>>() {
            @Override
            public Observable<Void> call(Void qbUser) {
                userCache.deleteUserByExternalId(externalId);
                return observable;
            }
        });

        return result;
    }


    public Observable<List<QBUser>> getUsers(QBPagedRequestBuilder requestBuilder) {
        Observable<List<QBUser>> result = null;
        Performer<ArrayList<QBUser>> performer = QBUsers.getUsers(requestBuilder);
        final Observable<List<QBUser>> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);
        result = observable.flatMap(new Func1<List<QBUser>, Observable<List<QBUser>>>() {
            @Override
            public Observable<List<QBUser>> call(List<QBUser> qbUsers) {
                userCache.createOrUpdateAll(qbUsers);
                return observable;
            }
        });
        return result;
    }


    public Observable<List<QBUser>> getUsersByIDs(final Collection<Integer> usersIds) {
        return  getUsersByIDs(usersIds, true);
    }

    public Observable<List<QBUser>> getUsersByIDs(final Collection<Integer> usersIds, boolean forceLoad){
        Observable<List<QBUser>> result = null;

        if (!forceLoad) {
            result = Observable.defer(new Func0<Observable<List<QBUser>>>() {
                @Override
                public Observable<List<QBUser>> call() {
                    List<QBUser> qbUsers = userCache.getUsersByIDs(usersIds);
                    return  qbUsers.size() == 0 ? getUsersByIDs(usersIds, true) : Observable.just(qbUsers);
                }
            });
            return result;
        }

        Performer<ArrayList<QBUser>> performer = QBUsers.getUsersByIDs(usersIds, new QBPagedRequestBuilder());
        final Observable<List<QBUser>> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.flatMap(new Func1<List<QBUser>, Observable<List<QBUser>>>() {
            @Override
            public Observable<List<QBUser>> call(List<QBUser> qbUsers) {
                userCache.createOrUpdateAll(qbUsers);
                return observable;
            }
        });

        return result;
    }

    public Observable<List<QBUser>>  getUsersByEmails(final Collection<String> usersEmails, boolean forceLoad){
        return getUsersByColumn(QMUserColumns.EMAIL, usersEmails, forceLoad);
    }

    public Observable<List<QBUser>> getUsersByLogins(final Collection<String> usersLogins, boolean forceLoad){
        return getUsersByColumn(QMUserColumns.LOGIN, usersLogins, forceLoad);
    }

    public Observable<List<QBUser>> getUsersByFacebookId(final Collection<String> usersFacebookIds, boolean forceLoad){
        return getUsersByColumn(QMUserColumns.FACEBOOK_ID, usersFacebookIds, forceLoad);
    }

    public Observable<List<QBUser>> getUsersByTwitterIds(final Collection<String> usersTwitterIds, boolean forceLoad){
        return getUsersByColumn(QMUserColumns.TWITTER_ID, usersTwitterIds, forceLoad);
    }

    public Observable<List<QBUser>> getUsersByTwitterDigitsIds(final Collection<String> usersTwitterDigitsIds, boolean forceLoad){
        return getUsersByColumn(QMUserColumns.TWITTER_DIGITS_ID, usersTwitterDigitsIds, forceLoad);
    }

    public Observable<List<QBUser>> getUsersByFullName(final String fullName, boolean forceLoad){
        return getUsersByColumn(QMUserColumns.FULL_NAME, fullName, forceLoad);
    }

    public Observable<List<QBUser>>  getUsersByTags(final Collection<String> tags, boolean forceLoad){
        return getUsersByColumn(QMUserColumns.TAGS, tags, forceLoad);
    }

    public Observable<List<QBUser>>  getUsersByPhoneNumbers(final Collection<String> usersPhoneNumbers, boolean forceLoad){
        return getUsersByColumn(QMUserColumns.PHONE, usersPhoneNumbers, forceLoad);
    }

    public Observable<List<QBUser>> getUsersByFilter(final Collection<?> filterValue, final String filter, boolean forceLoad) {
        Observable<List<QBUser>> result = null;

        if (!forceLoad) {
            result = Observable.defer(new Func0<Observable<List<QBUser>>>() {
                @Override
                public Observable<List<QBUser>> call() {
                    List<QBUser> qbUsers = userCache.getUsersByFilter(filterValue, filter);
                    return qbUsers.size() == 0 ? getUsersByFilter(filterValue, filter, true) : Observable.just(qbUsers);
                }
            });
            return result;
        }

        Performer<ArrayList<QBUser>> performer = QBUsers.getUsersByFilter(filterValue, filter, new QBPagedRequestBuilder());
        final Observable<List<QBUser>> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.flatMap(new Func1<List<QBUser>, Observable<List<QBUser>>>() {
            @Override
            public Observable<List<QBUser>> call(List<QBUser> qbUsers) {
                userCache.createOrUpdateAll(qbUsers);
                return observable;
            }
        });

        return result;
    }


    private Observable<QBUser> getUserByColumn(final String column, final String value, boolean forceLoad){
        Observable<QBUser> result = null;

        if (!forceLoad) {
            result = Observable.defer(new Func0<Observable<QBUser>>() {
                @Override
                public Observable<QBUser> call() {
                    QBUser qbUser = userCache.getUserByColumn(column, value);
                    return  qbUser == null ? getUserByColumn(column, value, true) :  Observable.just(qbUser);
                }
            });
            return result;
        }

        Performer<QBUser> performer = getUserByColumnFromServer(column,value);
        final Observable<QBUser> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.flatMap(new Func1<QBUser, Observable<QBUser>>() {
            @Override
            public Observable<QBUser> call(QBUser qbUser) {
                userCache.createOrUpdate(qbUser);
                return observable;
            }
        });

        return result;
    }

    private Observable<List<QBUser>> getUsersByColumn(final String column, final String value, boolean forceLoad){
        Observable<List<QBUser>> result = null;

        if (!forceLoad) {
            result = Observable.defer(new Func0<Observable<List<QBUser>>>() {
                @Override
                public Observable<List<QBUser>> call() {
                    List<QBUser> qbUsers =  userCache.getByColumn(column, value);
                    return  qbUsers.size() == 0 ? getUsersByColumn(column, value, true) : Observable.just(qbUsers);
                }
            });
            return result;
        }

        Performer<ArrayList<QBUser>> performer = getUsersByColumnFromServer(column, value);
        final Observable<List<QBUser>> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.flatMap(new Func1<List<QBUser>, Observable<List<QBUser>>>() {
            @Override
            public Observable<List<QBUser>> call(List<QBUser> qbUsers) {
                userCache.createOrUpdateAll(qbUsers);
                return observable;
            }
        });

        return result;
    }

    public Observable<List<QBUser>>  getUsersByColumn(final String column, final Collection<String> values, boolean forceLoad){
        Observable<List<QBUser>> result = null;

        if (!forceLoad) {
            result = Observable.defer(new Func0<Observable<List<QBUser>>>() {
                @Override
                public Observable<List<QBUser>> call() {
                    List<QBUser> qbUsers = userCache.getByColumn(column, values);
                    return  qbUsers.size() == 0 ? getUsersByColumn(column, values, true): Observable.just(qbUsers);
                }
            });
            return result;
        }

        Performer<ArrayList<QBUser>> performer = getUsersByColumnFromServer(column, values);
        final Observable<List<QBUser>> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.flatMap(new Func1<List<QBUser>, Observable<List<QBUser>>>() {
            @Override
            public Observable<List<QBUser>> call(List<QBUser> qbUsers) {
                userCache.createOrUpdateAll(qbUsers);
                return observable;
            }
        });

        return result;
    }


    private Performer<QBUser> getUserByColumnFromServer(String column, String value){
        Performer<QBUser> result = null;
        switch (column){
            case QMUserColumns.ID:
                result = QBUsers.getUser(Integer.parseInt(value));
                break;
            case QMUserColumns.FULL_NAME:
                result = null;
                break;
            case QMUserColumns.EMAIL:
                result = QBUsers.getUserByEmail(value);
                break;
            case QMUserColumns.LOGIN:
                result = QBUsers.getUserByLogin(value);
                break;
            case QMUserColumns.PHONE:
                result = null;
                break;
            case QMUserColumns.WEBSITE:
                result = null;
                break;
            case QMUserColumns.LAST_REQUEST_AT:
                result = null;
                break;
            case QMUserColumns.EXTERNAL_ID:
                result = QBUsers.getUserByExternalId(value);
                break;
            case QMUserColumns.FACEBOOK_ID:
                result = QBUsers.getUserByFacebookId(value);
                break;
            case QMUserColumns.TWITTER_ID:
                result = QBUsers.getUserByTwitterId(value);
                break;
            case QMUserColumns.TWITTER_DIGITS_ID:
                result = QBUsers.getUserByTwitterDigitsId(value);
                break;
            case QMUserColumns.BLOB_ID:
                result = null;
                break;
            case QMUserColumns.TAGS:
                result = null;
                break;
            case QMUserColumns.PASSWORD:
                result = null;
                break;
            case QMUserColumns.OLD_PASSWORD:
                result = null;
                break;
            case QMUserColumns.CUSTOM_DATE:
                result = null;
                break;
        }
        return result;
    }


    private Performer<ArrayList<QBUser>> getUsersByColumnFromServer(String column, String value){
        Performer<ArrayList<QBUser>> result = null;
        switch (column){
            case QMUserColumns.ID:
                //result = QBUsers.getUsersByIDs(value, new QBPagedRequestBuilder());
                result = null;
                break;
            case QMUserColumns.FULL_NAME:
                result = QBUsers.getUsersByFullName(value, new QBPagedRequestBuilder());
                break;
            case QMUserColumns.EMAIL:
                result = null;
                break;
            case QMUserColumns.LOGIN:
                result = null;
                break;
            case QMUserColumns.PHONE:
                result = null;
                break;
            case QMUserColumns.WEBSITE:
                result = null;
                break;
            case QMUserColumns.LAST_REQUEST_AT:
                result = null;
                break;
            case QMUserColumns.EXTERNAL_ID:
                result =null;
                break;
            case QMUserColumns.FACEBOOK_ID:
                result = null;
                break;
            case QMUserColumns.TWITTER_ID:
                result = null;
                break;
            case QMUserColumns.TWITTER_DIGITS_ID:
                result = null;
                break;
            case QMUserColumns.BLOB_ID:
                result = null;
                break;
            case QMUserColumns.TAGS:
                result = null;
                break;
            case QMUserColumns.PASSWORD:
                result = null;
                break;
            case QMUserColumns.OLD_PASSWORD:
                result = null;
                break;
            case QMUserColumns.CUSTOM_DATE:
                result = null;
                break;
        }
        return result;
    }

    private Performer<ArrayList<QBUser>> getUsersByColumnFromServer(String column, Collection<String> values){
        Performer<ArrayList<QBUser>> result = null;
        switch (column){
            case QMUserColumns.ID:
                //result = QBUsers.getUsersByIDs(values, new QBPagedRequestBuilder());
                result = null;
                break;
            case QMUserColumns.FULL_NAME:
                result = null;
                break;
            case QMUserColumns.EMAIL:
                result = QBUsers.getUsersByEmails(values, new QBPagedRequestBuilder());
                break;
            case QMUserColumns.LOGIN:
                result = QBUsers.getUsersByLogins(values, new QBPagedRequestBuilder());
                break;
            case QMUserColumns.PHONE:
                result = QBUsers.getUsersByPhoneNumbers(values, new QBPagedRequestBuilder());
                break;
            case QMUserColumns.WEBSITE:
                result = null;
                break;
            case QMUserColumns.LAST_REQUEST_AT:
                result = null;
                break;
            case QMUserColumns.EXTERNAL_ID:
                result = null;
                break;
            case QMUserColumns.FACEBOOK_ID:
                result = QBUsers.getUsersByFacebookId(values, new QBPagedRequestBuilder());
                break;
            case QMUserColumns.TWITTER_ID:
                result = QBUsers.getUsersByTwitterId(values, new QBPagedRequestBuilder());
                break;
            case QMUserColumns.TWITTER_DIGITS_ID:
                result = QBUsers.getUsersByTwitterDigitsId(values, new QBPagedRequestBuilder());
                break;
            case QMUserColumns.BLOB_ID:
                result = null;
                break;
            case QMUserColumns.TAGS:
                result = null;
                break;
            case QMUserColumns.PASSWORD:
                result = null;
                break;
            case QMUserColumns.OLD_PASSWORD:
                result = null;
                break;
            case QMUserColumns.CUSTOM_DATE:
                result = null;
                break;
        }
        return result;
    }


    private  QBUser getUserByEmailFromCache(String email) {
        return userCache.getUserByColumn(QMUserColumns.EMAIL, email);
    }

    private QBUser getUserByLoginFromCache(String login) {
        return userCache.getUserByColumn(QMUserColumns.LOGIN, login);
    }

    private QBUser getUserByFacebookIdFromCache(String facebookId) {
        return userCache.getUserByColumn(QMUserColumns.FACEBOOK_ID, facebookId);
    }

    private QBUser getUserByTwitterIdFromCache(String twitterId) {
        return userCache.getUserByColumn(QMUserColumns.TWITTER_ID, twitterId);
    }

    private QBUser getUserByTwitterDigitsIdFromCache(String twitterDigitsId) {
        return userCache.getUserByColumn(QMUserColumns.TWITTER_DIGITS_ID, twitterDigitsId);
    }

    private QBUser getUserByExternalIDFromCache(String externalId) {
        return userCache.getUserByColumn(QMUserColumns.EXTERNAL_ID, externalId);
    }

    private QBUser getUserByFullNameFromCache(String fullName) {
        return userCache.getUserByColumn(QMUserColumns.FULL_NAME, fullName);
    }

    private QBUser getUserByTagFromCache(String tag) {
        return userCache.getUserByColumn(QMUserColumns.TAGS, tag);
    }

    private QBUser getUserByPhoneNumberFromCache(String phoneNumber) {
        return null;
    }

    private List<QBUser> getUsersByEmailsFromCache(Collection<String> usersEmails) {
        return userCache.getByColumn(QMUserColumns.EMAIL, usersEmails);
    }

    private List<QBUser> getUsersByLoginsFromCache(Collection<String> usersLogins) {
        return userCache.getByColumn(QMUserColumns.LOGIN, usersLogins);
    }

    private List<QBUser> getUsersByFacebookIdsFromCache(Collection<String> usersFacebookIds) {
        return userCache.getByColumn(QMUserColumns.FACEBOOK_ID, usersFacebookIds);
    }

    private List<QBUser> getUsersByTwitterIdsFromCache(Collection<String> usersTwitterIds) {
        return userCache.getByColumn(QMUserColumns.TWITTER_ID, usersTwitterIds);
    }

    private List<QBUser> getUsersByTwitterDigitsIdsFromCache(Collection<String> usersTwitterDigitsIds) {
        return userCache.getByColumn(QMUserColumns.TWITTER_DIGITS_ID, usersTwitterDigitsIds);
    }

    private List<QBUser> getUsersByExternalIdsFromCache(Collection<String> usersExternalIds) {
        return userCache.getByColumn(QMUserColumns.EXTERNAL_ID, usersExternalIds);
    }

    private List<QBUser> getUsersByFullNameFromCache(String fullName) {
        return userCache.getByColumn(QMUserColumns.FULL_NAME, fullName);
    }

    private List<QBUser> getUsersByTagsFromCache(Collection<String> tags) {
        return userCache.getByColumn(QMUserColumns.TAGS, tags);
    }

    private List<QBUser> getUsersByPhoneNumbersFromCache(Collection<String> usersPhoneNumbers) {
        return userCache.getByColumn(QMUserColumns.PHONE, usersPhoneNumbers);
    }

}
