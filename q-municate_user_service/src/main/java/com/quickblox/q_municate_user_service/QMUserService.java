package com.quickblox.q_municate_user_service;


import com.quickblox.core.QBSettings;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.core.server.Performer;
import com.quickblox.extensions.RxJavaPerformProcessor;
import com.quickblox.q_municate_base_service.QMBaseService;
import com.quickblox.q_municate_base_service.QMServiceManagerListener;
import com.quickblox.q_municate_user_service.cache.QMUserCache;
import com.quickblox.q_municate_user_service.cache.QMUserMemoryCache;
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
        Observable<QBUser> result = null;

         if (!forceLoad) {
             result = Observable.defer(new Func0<Observable<QBUser>>() {
                                           @Override
                                           public Observable<QBUser> call() {
                                               QBUser qbUser = userCache.getUser(userId);
                                               return  qbUser == null ? getUser(userId, true) :  Observable.just(qbUser);
                                           }
                                       });
             return result;
         }

        Performer<QBUser> performer = QBUsers.getUser(userId);
        final Observable<QBUser> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.flatMap(new Func1<QBUser, Observable<QBUser>>() {
                    @Override
                    public Observable<QBUser> call(QBUser qbUser) {
                        userCache.createOrUpdateUser(qbUser);
                        return observable;
                    }
                });

        return result;
    }

    public Observable<QBUser> getUserByLogin(final String login){
        return getUserByLogin(login, true);
    }

    public Observable<QBUser> getUserByLogin(final String login, boolean forceLoad){
        Observable<QBUser> result = null;

        if (!forceLoad) {
            result = Observable.defer(new Func0<Observable<QBUser>>() {
                @Override
                public Observable<QBUser> call() {
                    QBUser qbUser = userCache.getUserByLogin(login);
                    return  qbUser == null ? getUserByLogin(login, true) :  Observable.just(qbUser);
                }
            });
            return result;
        }

        Performer<QBUser> performer = QBUsers.getUserByLogin(login);
        final Observable<QBUser> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.flatMap(new Func1<QBUser, Observable<QBUser>>() {
            @Override
            public Observable<QBUser> call(QBUser qbUser) {
                userCache.createOrUpdateUser(qbUser);
                return observable;
            }
        });

        return result;
    }

    public Observable<QBUser> getUserByFacebookId(final String facebookId){
        return getUserByFacebookId(facebookId, true);
    }

    public Observable<QBUser> getUserByFacebookId(final String facebookId, boolean forceLoad){
        Observable<QBUser> result = null;

        if (!forceLoad) {
            result = Observable.defer(new Func0<Observable<QBUser>>() {
                @Override
                public Observable<QBUser> call() {
                    QBUser qbUser = userCache.getUserByFacebookId(facebookId);
                    return  qbUser == null ? getUserByFacebookId(facebookId, true) :  Observable.just(qbUser);
                }
            });
            return result;
        }

        Performer<QBUser> performer = QBUsers.getUserByFacebookId(facebookId);
        final Observable<QBUser> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.flatMap(new Func1<QBUser, Observable<QBUser>>() {
            @Override
            public Observable<QBUser> call(QBUser qbUser) {
                userCache.createOrUpdateUser(qbUser);
                return observable;
            }
        });

        return result;
    }

    public Observable<QBUser> getUserByTwitterId(final String twitterId){
        return getUserByTwitterId(twitterId, true);
    }

    public Observable<QBUser> getUserByTwitterId(final String twitterId, boolean forceLoad){
        Observable<QBUser> result = null;

        if (!forceLoad) {
            result = Observable.defer(new Func0<Observable<QBUser>>() {
                @Override
                public Observable<QBUser> call() {
                    QBUser qbUser = userCache.getUserByTwitterId(twitterId);
                    return  qbUser == null ? getUserByTwitterId(twitterId, true) :  Observable.just(qbUser);
                }
            });
            return result;
        }

        Performer<QBUser> performer = QBUsers.getUserByTwitterId(twitterId);
        final Observable<QBUser> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.flatMap(new Func1<QBUser, Observable<QBUser>>() {
            @Override
            public Observable<QBUser> call(QBUser qbUser) {
                userCache.createOrUpdateUser(qbUser);
                return observable;
            }
        });

        return result;
    }

    public Observable<QBUser> getUserByTwitterDigitsId(final String twitterDigitsId){
        return getUserByTwitterDigitsId(twitterDigitsId, true);
    }

    public Observable<QBUser> getUserByTwitterDigitsId(final String twitterDigitsId, boolean forceLoad){
        Observable<QBUser> result = null;

        if (!forceLoad) {
            result = Observable.defer(new Func0<Observable<QBUser>>() {
                @Override
                public Observable<QBUser> call() {
                    QBUser qbUser = userCache.getUserByTwitterDigitsId(twitterDigitsId);
                    return  qbUser == null ? getUserByTwitterDigitsId(twitterDigitsId, true) :  Observable.just(qbUser);
                }
            });
            return result;
        }

        Performer<QBUser> performer = QBUsers.getUserByTwitterDigitsId(twitterDigitsId);
        final Observable<QBUser> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.flatMap(new Func1<QBUser, Observable<QBUser>>() {
            @Override
            public Observable<QBUser> call(QBUser qbUser) {
                userCache.createOrUpdateUser(qbUser);
                return observable;
            }
        });

        return result;
    }

    public Observable<QBUser> getUserByEmail(final String email){
        return getUserByEmail(email, true);
    }

    public Observable<QBUser> getUserByEmail(final String email, boolean forceLoad){
        Observable<QBUser> result = null;

        if (!forceLoad) {
            result = Observable.defer(new Func0<Observable<QBUser>>() {
                @Override
                public Observable<QBUser> call() {
                    QBUser qbUser = userCache.getUserByEmail(email);
                    return  qbUser == null ? getUserByEmail(email, true) :  Observable.just(qbUser);
                }
            });
            return result;
        }

        Performer<QBUser> performer = QBUsers.getUserByEmail(email);
        final Observable<QBUser> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.flatMap(new Func1<QBUser, Observable<QBUser>>() {
            @Override
            public Observable<QBUser> call(QBUser qbUser) {
                userCache.createOrUpdateUser(qbUser);
                return observable;
            }
        });

        return result;
    }

    public Observable<QBUser> getUserByExternalId(final String externalId){
        return getUserByExternalId(externalId, true);
    }

    public Observable<QBUser> getUserByExternalId(final String externalId, boolean forceLoad){
        Observable<QBUser> result = null;

        if (!forceLoad) {
            result = Observable.defer(new Func0<Observable<QBUser>>() {
                @Override
                public Observable<QBUser> call() {
                    QBUser qbUser = userCache.getUserByExternalID(externalId);
                    return  qbUser == null ? getUserByExternalId(externalId, true) :  Observable.just(qbUser);
                }
            });
            return result;
        }

        Performer<QBUser> performer = QBUsers.getUserByExternalId(externalId);
        final Observable<QBUser> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.flatMap(new Func1<QBUser, Observable<QBUser>>() {
            @Override
            public Observable<QBUser> call(QBUser qbUser) {
                userCache.createOrUpdateUser(qbUser);
                return observable;
            }
        });

        return result;
    }

    public Observable<QBUser> updateUser(final QBUser user){
        Observable<QBUser> result = null;

        Performer<QBUser> performer = QBUsers.updateUser(user);
        final Observable<QBUser> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.flatMap(new Func1<QBUser, Observable<QBUser>>() {
            @Override
            public Observable<QBUser> call(QBUser qbUser) {
                userCache.updateUser(qbUser);
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
                userCache.deleteUserById(userId);
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
                userCache.createOrUpdateAllUsers(qbUsers);
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
                userCache.createOrUpdateAllUsers(qbUsers);
                return observable;
            }
        });

        return result;
    }

    public Observable<List<QBUser>>  getUsersByEmails(final Collection<String> usersEmails, boolean forceLoad){
        Observable<List<QBUser>> result = null;

        if (!forceLoad) {
            result = Observable.defer(new Func0<Observable<List<QBUser>>>() {
                @Override
                public Observable<List<QBUser>> call() {
                    List<QBUser> qbUsers = userCache.getUsersByEmails(usersEmails);
                    return  qbUsers.size() == 0 ? getUsersByEmails(usersEmails, true) : Observable.just(qbUsers);
                }
            });
            return result;
        }

        Performer<ArrayList<QBUser>> performer = QBUsers.getUsersByEmails(usersEmails, new QBPagedRequestBuilder());
        final Observable<List<QBUser>> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.flatMap(new Func1<List<QBUser>, Observable<List<QBUser>>>() {
            @Override
            public Observable<List<QBUser>> call(List<QBUser> qbUsers) {
                userCache.createOrUpdateAllUsers(qbUsers);
                return observable;
            }
        });

        return result;
    }

    public Observable<List<QBUser>> getUsersByLogins(final Collection<String> usersLogins, boolean forceLoad){
        Observable<List<QBUser>> result = null;

        if (!forceLoad) {
            result = Observable.defer(new Func0<Observable<List<QBUser>>>() {
                @Override
                public Observable<List<QBUser>> call() {
                    List<QBUser> qbUsers = userCache.getUsersByLogins(usersLogins);
                    return  qbUsers.size() == 0 ? getUsersByLogins(usersLogins, true) : Observable.just(qbUsers);
                }
            });
            return result;
        }

        Performer<ArrayList<QBUser>> performer = QBUsers.getUsersByLogins(usersLogins, new QBPagedRequestBuilder());
        final Observable<List<QBUser>> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.flatMap(new Func1<List<QBUser>, Observable<List<QBUser>>>() {
            @Override
            public Observable<List<QBUser>> call(List<QBUser> qbUsers) {
                userCache.createOrUpdateAllUsers(qbUsers);
                return observable;
            }
        });

        return result;
    }

    public Observable<List<QBUser>> getUsersByFacebookId(final Collection<String> usersFacebookIds, boolean forceLoad){
        Observable<List<QBUser>> result = null;

        if (!forceLoad) {
            result = Observable.defer(new Func0<Observable<List<QBUser>>>() {
                @Override
                public Observable<List<QBUser>> call() {
                    List<QBUser> qbUsers = userCache.getUsersByFacebookIds(usersFacebookIds);
                    return  qbUsers.size() == 0 ? getUsersByFacebookId(usersFacebookIds, true) : Observable.just(qbUsers);
                }
            });
            return result;
        }

        Performer<ArrayList<QBUser>> performer = QBUsers.getUsersByFacebookId(usersFacebookIds, new QBPagedRequestBuilder());
        final Observable<List<QBUser>> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.flatMap(new Func1<List<QBUser>, Observable<List<QBUser>>>() {
            @Override
            public Observable<List<QBUser>> call(List<QBUser> qbUsers) {
                userCache.createOrUpdateAllUsers(qbUsers);
                return observable;
            }
        });

        return result;
    }

    public Observable<List<QBUser>> getUsersByTwitterIds(final Collection<String> usersTwitterIds, boolean forceLoad){
        Observable<List<QBUser>> result = null;

        if (!forceLoad) {
            result = Observable.defer(new Func0<Observable<List<QBUser>>>() {
                @Override
                public Observable<List<QBUser>> call() {
                    List<QBUser> qbUsers = userCache.getUsersByTwitterIds(usersTwitterIds);
                    return  qbUsers.size() == 0 ? getUsersByTwitterIds(usersTwitterIds, true) : Observable.just(qbUsers);
                }
            });
            return result;
        }

        Performer<ArrayList<QBUser>> performer = QBUsers.getUsersByTwitterId(usersTwitterIds, new QBPagedRequestBuilder());
        final Observable<List<QBUser>> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.flatMap(new Func1<List<QBUser>, Observable<List<QBUser>>>() {
            @Override
            public Observable<List<QBUser>> call(List<QBUser> qbUsers) {
                userCache.createOrUpdateAllUsers(qbUsers);
                return observable;
            }
        });

        return result;
    }

    public Observable<List<QBUser>> getUsersByTwitterDigitsIds(final Collection<String> usersTwitterDigitsIds, boolean forceLoad){
        Observable<List<QBUser>> result = null;

        if (!forceLoad) {
            result = Observable.defer(new Func0<Observable<List<QBUser>>>() {
                @Override
                public Observable<List<QBUser>> call() {
                    List<QBUser> qbUsers = userCache.getUsersByTwitterDigitsIds(usersTwitterDigitsIds);
                    return  qbUsers.size() == 0 ? getUsersByTwitterDigitsIds(usersTwitterDigitsIds, true) : Observable.just(qbUsers);
                }
            });
            return result;
        }

        Performer<ArrayList<QBUser>> performer = QBUsers.getUsersByTwitterDigitsId(usersTwitterDigitsIds, new QBPagedRequestBuilder());
        final Observable<List<QBUser>> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.flatMap(new Func1<List<QBUser>, Observable<List<QBUser>>>() {
            @Override
            public Observable<List<QBUser>> call(List<QBUser> qbUsers) {
                userCache.createOrUpdateAllUsers(qbUsers);
                return observable;
            }
        });

        return result;
    }

    public Observable<List<QBUser>> getUsersByFullName(final String fullName, boolean forceLoad){
        Observable<List<QBUser>> result = null;

        if (!forceLoad) {
            result = Observable.defer(new Func0<Observable<List<QBUser>>>() {
                @Override
                public Observable<List<QBUser>> call() {
                    List<QBUser> qbUsers = userCache.getUsersByFullName(fullName);
                    return  qbUsers.size() == 0 ? getUsersByFullName(fullName, true): Observable.just(qbUsers);
                }
            });
            return result;
        }

        Performer<ArrayList<QBUser>> performer = QBUsers.getUsersByFullName(fullName, new QBPagedRequestBuilder());
        final Observable<List<QBUser>> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.flatMap(new Func1<List<QBUser>, Observable<List<QBUser>>>() {
            @Override
            public Observable<List<QBUser>> call(List<QBUser> qbUsers) {
                userCache.createOrUpdateAllUsers(qbUsers);
                return observable;
            }
        });

        return result;
    }

    public Observable<List<QBUser>>  getUsersByTags(final Collection<String> tags, boolean forceLoad){
        Observable<List<QBUser>> result = null;

        if (!forceLoad) {
            result = Observable.defer(new Func0<Observable<List<QBUser>>>() {
                @Override
                public Observable<List<QBUser>> call() {
                    List<QBUser> qbUsers = userCache.getUsersByTags(tags);
                    return  qbUsers.size() == 0 ? getUsersByTags(tags, true): Observable.just(qbUsers);
                }
            });
            return result;
        }

        Performer<ArrayList<QBUser>> performer = QBUsers.getUsersByTags(tags, new QBPagedRequestBuilder());
        final Observable<List<QBUser>> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.flatMap(new Func1<List<QBUser>, Observable<List<QBUser>>>() {
            @Override
            public Observable<List<QBUser>> call(List<QBUser> qbUsers) {
                userCache.createOrUpdateAllUsers(qbUsers);
                return observable;
            }
        });

        return result;
    }

    public Observable<List<QBUser>>  getUsersByPhoneNumbers(final Collection<String> usersPhoneNumbers, boolean forceLoad){
        Observable<List<QBUser>> result = null;

        if (!forceLoad) {
            result = Observable.defer(new Func0<Observable<List<QBUser>>>() {
                @Override
                public Observable<List<QBUser>> call() {
                    List<QBUser> qbUsers = userCache.getUsersByPhoneNumbers(usersPhoneNumbers);
                    return  qbUsers.size() == 0 ? getUsersByPhoneNumbers(usersPhoneNumbers, true): Observable.just(qbUsers);
                }
            });
            return result;
        }

        Performer<ArrayList<QBUser>> performer = QBUsers.getUsersByPhoneNumbers(usersPhoneNumbers, new QBPagedRequestBuilder());
        final Observable<List<QBUser>> observable = performer.convertTo(RxJavaPerformProcessor.INSTANCE);

        result = observable.flatMap(new Func1<List<QBUser>, Observable<List<QBUser>>>() {
            @Override
            public Observable<List<QBUser>> call(List<QBUser> qbUsers) {
                userCache.createOrUpdateAllUsers(qbUsers);
                return observable;
            }
        });

        return result;
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
                userCache.createOrUpdateAllUsers(qbUsers);
                return observable;
            }
        });

        return result;
    }

}
