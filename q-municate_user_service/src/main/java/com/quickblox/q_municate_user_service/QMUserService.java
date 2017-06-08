package com.quickblox.q_municate_user_service;


import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.server.Performer;
import com.quickblox.extensions.RxJavaPerformProcessor;
import com.quickblox.q_municate_base_service.QMBaseService;
import com.quickblox.q_municate_user_service.cache.QMUserCache;
import com.quickblox.q_municate_user_service.cache.QMUserMemoryCache;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.q_municate_user_service.model.QMUserColumns;
import com.quickblox.users.QBUsers;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


import javax.inject.Inject;

import rx.Observable;
import rx.functions.Func0;
import rx.functions.Func1;

public class QMUserService extends QMBaseService {

    private static QMUserService instance;

    public static QMUserService getInstance(){
        return instance;
    }

    @Inject
    protected QMUserCache userCache;

    public static void init(QMUserCache userCache){
        instance = new QMUserService(userCache);
    }

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

    public QMUserCache getUserCache() {
        return userCache;
    }

    public Observable<QMUser> getUser(final int userId){
        return getUser(userId, true);
    }

    public Observable<QMUser> getUser(final int userId, boolean forceLoad){
        return  getUserByColumn(QMUserColumns.ID, String.valueOf(userId), forceLoad);
    }

    public QMUser getUserSync(final int userId, boolean forceLoad) throws QBResponseException {
        return  getUserByColumnSync(QMUserColumns.ID, String.valueOf(userId), forceLoad);
    }

    public Observable<QMUser> getUserByLogin(final String login){
        return getUserByLogin(login, true);
    }

    public Observable<QMUser> getUserByLogin(final String login, boolean forceLoad){
        return  getUserByColumn(QMUserColumns.LOGIN, login, forceLoad);
    }

    public Observable<QMUser> getUserByFacebookId(final String facebookId){
        return getUserByFacebookId(facebookId, true);
    }

    public Observable<QMUser> getUserByFacebookId(final String facebookId, boolean forceLoad){
        return  getUserByColumn(QMUserColumns.FACEBOOK_ID, facebookId, forceLoad);

    }

    public Observable<QMUser> getUserByTwitterId(final String twitterId){
        return getUserByTwitterId(twitterId, true);
    }

    public Observable<QMUser> getUserByTwitterId(final String twitterId, boolean forceLoad) {
        return getUserByColumn(QMUserColumns.TWITTER_ID, twitterId, forceLoad);
    }

    public Observable<QMUser> getUserByTwitterDigitsId(final String twitterDigitsId){
        return getUserByTwitterDigitsId(twitterDigitsId, true);
    }

    public Observable<QMUser> getUserByTwitterDigitsId(final String twitterDigitsId, boolean forceLoad){
        return getUserByColumn(QMUserColumns.TWITTER_DIGITS_ID, twitterDigitsId, forceLoad);
    }

    public Observable<QMUser> getUserByEmail(final String email){
        return getUserByEmail(email, true);
    }

    public Observable<QMUser> getUserByEmail(final String email, boolean forceLoad){
        return getUserByColumn(QMUserColumns.EMAIL, email, forceLoad);
    }

    public Observable<QMUser> getUserByExternalId(final String externalId){
        return getUserByExternalId(externalId, true);
    }

    public Observable<QMUser> getUserByExternalId(final String externalId, boolean forceLoad){
        return getUserByColumn(QMUserColumns.EXTERNAL_ID, externalId, forceLoad);
    }

    public Observable<QMUser> updateUser(final QMUser user){
        Observable<QMUser> result = null;

        Performer<QBUser> performer = QBUsers.updateUser(user);
        final Observable<QBUser> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.map(new Func1<QBUser,QMUser>() {
            @Override
            public QMUser call(QBUser qbUser) {
                QMUser result = QMUser.convert(qbUser);
                userCache.update(result);
                return result;
            }
        });

        return result;
    }

    public QMUser updateUserSync(final QMUser user) throws QBResponseException {
        QMUser result = null;

        result = QMUser.convert(QBUsers.updateUser(user).perform());

        userCache.update(result);

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


    public Observable<List<QMUser>> getUsers(QBPagedRequestBuilder requestBuilder) {
        Observable<List<QMUser>> result = null;
        Performer<ArrayList<QBUser>> performer = QBUsers.getUsers(requestBuilder);
        final Observable<List<QBUser>> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.map(new Func1<List<QBUser>,List<QMUser>>() {
            @Override
            public List<QMUser> call(List<QBUser> qbUsers) {
                List<QMUser> result = QMUser.convertList(qbUsers);
                userCache.createOrUpdateAll(result);
                return result;
            }
        });
        return result;
    }


    public Observable<List<QMUser>> getUsersByIDs(final Collection<Integer> usersIds, final QBPagedRequestBuilder requestBuilder) {
        return  getUsersByIDs(usersIds, requestBuilder, true);
    }

    public  List<QMUser> getUsersByIDsSync(final Collection<Integer> usersIds, final QBPagedRequestBuilder requestBuilder) throws QBResponseException {
        return getUsersByIDsSync(usersIds,requestBuilder, true);
    }

    public Observable<List<QMUser>> getUsersByIDs(final Collection<Integer> usersIds, final QBPagedRequestBuilder requestBuilder, boolean forceLoad){
        Observable<List<QMUser>> result = null;

        if (!forceLoad) {
            result = Observable.defer(new Func0<Observable<List<QMUser>>>() {
                @Override
                public Observable<List<QMUser>> call() {
                    List<QMUser> qmUsers = userCache.getUsersByIDs(usersIds);
                    return  qmUsers.size() == 0 ? getUsersByIDs(usersIds, requestBuilder, true) : Observable.just(qmUsers);
                }
            });
            return result;
        }

        Performer<ArrayList<QBUser>> performer = QBUsers.getUsersByIDs(usersIds, requestBuilder);
        final Observable<List<QBUser>> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.map(new Func1<List<QBUser>,List<QMUser>>() {
            @Override
            public List<QMUser> call(List<QBUser> qbUsers) {
                List<QMUser> result = QMUser.convertList(qbUsers);
                userCache.createOrUpdateAll(result);
                return result;
            }
        });

        return result;
    }

    public  List<QMUser> getUsersByIDsSync(final Collection<Integer> usersIds, final QBPagedRequestBuilder requestBuilder, boolean forceLoad) throws QBResponseException {
        List<QMUser> result = null;

        if (!forceLoad) {
            List<QMUser> qbUsers = userCache.getUsersByIDs(usersIds);
            return qbUsers.size() == 0 ? getUsersByIDsSync(usersIds, requestBuilder, true) : qbUsers;
        }

        result = QMUser.convertList(QBUsers.getUsersByIDs(usersIds, requestBuilder).perform());
        userCache.createOrUpdateAll(result);

        return result;
    }

//    public Performer<ArrayList<QBUser>> getUsersByIDsPerformer(final Collection<Integer> usersIds, final QBPagedRequestBuilder requestBuilder, final boolean forceLoad){
//        Performer<ArrayList<QBUser>>  result = null;
//
//            result = new Performer<ArrayList<QBUser>>() {
//
//                private boolean canceled;
//
//                @Override
//                public void performAsync(QBEntityCallback<ArrayList<QBUser>> callback) {
//                    if(canceled){
//                        return;
//                    }
//                }
//
//                @Override
//                public ArrayList<QBUser> perform() throws QBResponseException {
//                    ArrayList<QBUser> result = null;
//                    if (!forceLoad) {
//                        result = (ArrayList<QBUser>)userCache.getUsersByIDs(usersIds);
//                        if(result.size()>0){
//                            return  result;
//                        }
//                    }
//                    result = QBUsers.getUsersByIDs(usersIds, requestBuilder).perform();
//                    userCache.createOrUpdateAll(result);
//                    return result;
//                }
//
//                @Override
//                public <R> R convertTo(PerformProcessor<?> performProcessor) {
//                    return (R)performProcessor.process(this);
//                }
//
//                @Override
//                public boolean isCanceled() {
//                    return canceled;
//                }
//
//                @Override
//                public void cancel() {
//                    canceled = true;
//                }
//            };
//            return result;
//
//      }

    public List<QMUser> getUsersByFacebookIdSync(final Collection<String> usersFacebookIds, final QBPagedRequestBuilder requestBuilder, boolean forceLoad) throws QBResponseException {
        return getUsersByColumnSync(QMUserColumns.FACEBOOK_ID, usersFacebookIds, requestBuilder, forceLoad);
    }

    public List<QMUser> getUsersByEmailsSync(final Collection<String> usersEmails, final QBPagedRequestBuilder requestBuilder, boolean forceLoad) throws QBResponseException {
        return getUsersByColumnSync(QMUserColumns.EMAIL, usersEmails, requestBuilder, forceLoad);
    }


    public Observable<List<QMUser>>  getUsersByEmails(final Collection<String> usersEmails, final QBPagedRequestBuilder requestBuilder, boolean forceLoad){
        return getUsersByColumn(QMUserColumns.EMAIL, usersEmails, requestBuilder, forceLoad);
    }

    public Observable<List<QMUser>> getUsersByLogins(final Collection<String> usersLogins, final QBPagedRequestBuilder requestBuilder,  boolean forceLoad){
        return getUsersByColumn(QMUserColumns.LOGIN, usersLogins, requestBuilder, forceLoad);
    }

    public Observable<List<QMUser>> getUsersByFacebookId(final Collection<String> usersFacebookIds, final QBPagedRequestBuilder requestBuilder,  boolean forceLoad){
        return getUsersByColumn(QMUserColumns.FACEBOOK_ID, usersFacebookIds, requestBuilder, forceLoad);
    }

    public Observable<List<QMUser>> getUsersByTwitterIds(final Collection<String> usersTwitterIds, final QBPagedRequestBuilder requestBuilder,  boolean forceLoad){
        return getUsersByColumn(QMUserColumns.TWITTER_ID, usersTwitterIds, requestBuilder, forceLoad);
    }

    public Observable<List<QMUser>> getUsersByTwitterDigitsIds(final Collection<String> usersTwitterDigitsIds, final QBPagedRequestBuilder requestBuilder,  boolean forceLoad){
        return getUsersByColumn(QMUserColumns.TWITTER_DIGITS_ID, usersTwitterDigitsIds, requestBuilder, forceLoad);
    }

    public Observable<List<QMUser>> getUsersByFullName(final String fullName, final QBPagedRequestBuilder requestBuilder,  boolean forceLoad){
        return getUsersByColumn(QMUserColumns.FULL_NAME, fullName, requestBuilder, forceLoad);
    }

    public Observable<List<QMUser>>  getUsersByTags(final Collection<String> tags, final QBPagedRequestBuilder requestBuilder,  boolean forceLoad){
        return getUsersByColumn(QMUserColumns.TAGS, tags, requestBuilder, forceLoad);
    }

    public Observable<List<QMUser>>  getUsersByPhoneNumbers(final Collection<String> usersPhoneNumbers, final QBPagedRequestBuilder requestBuilder,  boolean forceLoad){
        return getUsersByColumn(QMUserColumns.PHONE, usersPhoneNumbers, requestBuilder, forceLoad);
    }

    public Observable<List<QMUser>> getUsersByFilter(final Collection<?> filterValue, final String filter, final QBPagedRequestBuilder requestBuilder,  boolean forceLoad) {
        Observable<List<QMUser>> result = null;

        if (!forceLoad) {
            result = Observable.defer(new Func0<Observable<List<QMUser>>>() {
                @Override
                public Observable<List<QMUser>> call() {
                    List<QMUser> qmUsers = userCache.getUsersByFilter(filterValue, filter);
                    return qmUsers.size() == 0 ? getUsersByFilter(filterValue, filter, requestBuilder, true) : Observable.just(qmUsers);
                }
            });
            return result;
        }

        Performer<ArrayList<QBUser>> performer = QBUsers.getUsersByFilter(filterValue, filter, requestBuilder);
        final Observable<List<QBUser>> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.map(new Func1<List<QBUser>,List<QMUser>>() {
            @Override
            public List<QMUser> call(List<QBUser> qbUsers) {
                List<QMUser> result = QMUser.convertList(qbUsers);
                userCache.createOrUpdateAll(result);
                return result;
            }
        });

        return result;
    }


    private Observable<QMUser> getUserByColumn(final String column, final String value,  boolean forceLoad){
        Observable<QMUser> result = null;

        if (!forceLoad) {
            result = Observable.defer(new Func0<Observable<QMUser>>() {
                @Override
                public Observable<QMUser> call() {
                    QMUser qmUser = userCache.getUserByColumn(column, value);
                    return  qmUser == null ? getUserByColumn(column, value, true) :  Observable.just(qmUser);
                }
            });
            return result;
        }

        Performer<QBUser> performer = getUserByColumnFromServer(column,value);
        final Observable<QBUser> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.map(new Func1<QBUser,QMUser>() {
            @Override
            public QMUser call(QBUser qbUser) {
                //TODO VT temp code before implement feature "last user activity" in SDK
                QBUser userWithActualLastActivity = getUserWithLatestLastActivity(qbUser);
                QMUser result = QMUser.convert(userWithActualLastActivity);
                userCache.createOrUpdate(result);
                return result;
            }
        });

        return result;
    }

    private QMUser getUserByColumnSync(final String column, final String value, boolean forceLoad) throws QBResponseException {
        QMUser result = null;

        if (!forceLoad) {
           QMUser qbUser = userCache.getUserByColumn(column, value);
           return  qbUser == null ? getUserByColumnSync(column, value, true) : qbUser;
        }

        QBUser loadedUser = getUserByColumnFromServer(column,value).perform();

        //TODO VT temp code before implement feature "last user activity" in SDK
        QBUser userWithActualLastActivity = getUserWithLatestLastActivity(loadedUser);

        result = QMUser.convert(userWithActualLastActivity);
        userCache.createOrUpdate(result);
        return result;
    }

    private QBUser getUserWithLatestLastActivity(QBUser loadedUser) {
        if (loadedUser != null && loadedUser.getLastRequestAt() != null){
            QMUser tempQmUser = userCache.getUserByColumn(QMUserColumns.ID, String.valueOf(loadedUser.getId()));
            if (tempQmUser != null) {
                QBUser existUser = QMUser.convert(tempQmUser);
                if (existUser.getLastRequestAt() != null) {
                    if (existUser.getLastRequestAt().after(loadedUser.getLastRequestAt())) {
                        //sets for loaded user last activity saved before from roster listener
                        loadedUser.setLastRequestAt(existUser.getLastRequestAt());
                    }
                }
            }
        }

        return loadedUser;
    }

    private Observable<List<QMUser>> getUsersByColumn(final String column, final String value, final QBPagedRequestBuilder requestBuilder,  boolean forceLoad){
        Observable<List<QMUser>> result = null;

        if (!forceLoad) {
            result = Observable.defer(new Func0<Observable<List<QMUser>>>() {
                @Override
                public Observable<List<QMUser>> call() {
                    List<QMUser> qmUsers =  userCache.getByColumn(column, value);
                    return  qmUsers.size() == 0 ? getUsersByColumn(column, value, requestBuilder, true) : Observable.just(qmUsers);
                }
            });
            return result;
        }

        Performer<ArrayList<QBUser>> performer = getUsersByColumnFromServer(column, value,  requestBuilder);
        final Observable<List<QBUser>> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.map(new Func1<List<QBUser>,List<QMUser>>() {
            @Override
            public List<QMUser> call(List<QBUser> qbUsers) {
                List<QMUser> result = QMUser.convertList(qbUsers);
                userCache.createOrUpdateAll(result);
                return result;
            }
        });
        return result;
    }

    public Observable<List<QMUser>>  getUsersByColumn(final String column, final Collection<String> values, final QBPagedRequestBuilder requestBuilder,  boolean forceLoad){
        Observable<List<QMUser>> result = null;

        if (!forceLoad) {
            result = Observable.defer(new Func0<Observable<List<QMUser>>>() {
                @Override
                public Observable<List<QMUser>> call() {
                    List<QMUser> qmUsers = userCache.getByColumn(column, values);
                    return  qmUsers.size() == 0 ? getUsersByColumn(column, values, requestBuilder, true): Observable.just(qmUsers);
                }
            });
            return result;
        }

        Performer<ArrayList<QBUser>> performer = getUsersByColumnFromServer(column, values, requestBuilder);
        final Observable<List<QBUser>> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.map(new Func1<List<QBUser>,List<QMUser>>() {
            @Override
            public List<QMUser> call(List<QBUser> qbUsers) {
                List<QMUser> result = QMUser.convertList(qbUsers);
                userCache.createOrUpdateAll(result);
                return result;
            }
        });

        return result;
    }


    public List<QMUser> getUsersByColumnSync(final String column, final Collection<String> values, final QBPagedRequestBuilder requestBuilder, boolean forceLoad) throws QBResponseException {
        List<QMUser> result = null;

        if (!forceLoad) {
                    List<QMUser> qmUsers = userCache.getByColumn(column, values);
                    return  qmUsers.size() == 0 ? getUsersByColumnSync(column, values, requestBuilder, true):qmUsers;
        }

        result = QMUser.convertList(getUsersByColumnFromServer(column, values, requestBuilder).perform());
        userCache.createOrUpdateAll(result);

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


    private Performer<ArrayList<QBUser>> getUsersByColumnFromServer(String column, String value, QBPagedRequestBuilder  requestBuilder){
        Performer<ArrayList<QBUser>> result = null;
        switch (column){
            case QMUserColumns.ID:
                result = null;
                break;
            case QMUserColumns.FULL_NAME:
                result = QBUsers.getUsersByFullName(value, requestBuilder);
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

    private Performer<ArrayList<QBUser>> getUsersByColumnFromServer(String column, Collection<String> values, QBPagedRequestBuilder  requestBuilder){
        Performer<ArrayList<QBUser>> result = null;
        switch (column){
            case QMUserColumns.ID:
                result = null;
                break;
            case QMUserColumns.FULL_NAME:
                result = null;
                break;
            case QMUserColumns.EMAIL:
                result = QBUsers.getUsersByEmails(values, requestBuilder);
                break;
            case QMUserColumns.LOGIN:
                result = QBUsers.getUsersByLogins(values, requestBuilder);
                break;
            case QMUserColumns.PHONE:
                result = QBUsers.getUsersByPhoneNumbers(values, requestBuilder);
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
                result = QBUsers.getUsersByFacebookId(values, requestBuilder);
                break;
            case QMUserColumns.TWITTER_ID:
                result = QBUsers.getUsersByTwitterId(values, requestBuilder);
                break;
            case QMUserColumns.TWITTER_DIGITS_ID:
                result = QBUsers.getUsersByTwitterDigitsId(values, requestBuilder);
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

    private List<QMUser> getUsersByEmailsFromCache(Collection<String> usersEmails) {
        return userCache.getByColumn(QMUserColumns.EMAIL, usersEmails);
    }

    private List<QMUser> getUsersByLoginsFromCache(Collection<String> usersLogins) {
        return userCache.getByColumn(QMUserColumns.LOGIN, usersLogins);
    }

    private List<QMUser> getUsersByFacebookIdsFromCache(Collection<String> usersFacebookIds) {
        return userCache.getByColumn(QMUserColumns.FACEBOOK_ID, usersFacebookIds);
    }

    private List<QMUser> getUsersByTwitterIdsFromCache(Collection<String> usersTwitterIds) {
        return userCache.getByColumn(QMUserColumns.TWITTER_ID, usersTwitterIds);
    }

    private List<QMUser> getUsersByTwitterDigitsIdsFromCache(Collection<String> usersTwitterDigitsIds) {
        return userCache.getByColumn(QMUserColumns.TWITTER_DIGITS_ID, usersTwitterDigitsIds);
    }

    private List<QMUser> getUsersByExternalIdsFromCache(Collection<String> usersExternalIds) {
        return userCache.getByColumn(QMUserColumns.EXTERNAL_ID, usersExternalIds);
    }

    private List<QMUser> getUsersByFullNameFromCache(String fullName) {
        return userCache.getByColumn(QMUserColumns.FULL_NAME, fullName);
    }

    private List<QMUser> getUsersByTagsFromCache(Collection<String> tags) {
        return userCache.getByColumn(QMUserColumns.TAGS, tags);
    }

    private List<QMUser> getUsersByPhoneNumbersFromCache(Collection<String> usersPhoneNumbers) {
        return userCache.getByColumn(QMUserColumns.PHONE, usersPhoneNumbers);
    }

}
