package com.quickblox.q_municate.utils.helpers;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.quickblox.auth.model.QBProvider;
import com.quickblox.auth.session.QBSessionManager;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_core.utils.Utils;
import com.quickblox.q_municate_core.utils.helpers.CoreSharedHelper;
import com.quickblox.users.model.QBUser;

import rx.Observable;
import rx.android.schedulers.AndroidSchedulers;
import rx.exceptions.Exceptions;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class FriendListServiceManager {

    private static FriendListServiceManager instance;
    private final Context context;

    public static FriendListServiceManager getInstance(){
        if (instance == null){
            instance = new FriendListServiceManager();
        }

        return instance;
    }

    private FriendListServiceManager() {
        this.context = App.getInstance();
    }

    public Observable<QBUser> login(final String socialProvider, final String accessToken, final String accessTokenSecret) {
        Observable<QBUser> result = authService.login(socialProvider, accessToken, accessTokenSecret).subscribeOn(Schedulers.io())
                .map(new Func1<QBUser, QBUser>() {
                    @Override
                    public QBUser call(QBUser qbUser) {
                        Log.d(TAG, "login observer call " + qbUser);
                        UserCustomData userCustomData = Utils.customDataToObject(qbUser.getCustomData());
                        if (QBProvider.FACEBOOK.equals(socialProvider) && TextUtils.isEmpty(userCustomData.getAvatarUrl())) {
                            //Actions for first login via Facebook
                            CoreSharedHelper.getInstance().saveUsersImportInitialized(false);
                            getFBUserWithAvatar(qbUser);
                        } else if (QBProvider.TWITTER_DIGITS.equals(socialProvider) && TextUtils.isEmpty(qbUser.getFullName())) {
                            //Actions for first login via Twitter Digits
                            CoreSharedHelper.getInstance().saveUsersImportInitialized(false);
                            getTDUserWithFullName(qbUser);
                        }
                        try {
                            updateUserSync(qbUser);
                        } catch (QBResponseException e) {
                            Log.d(TAG, "updateUser " + e.getMessage());
                            throw Exceptions.propagate(e);
                        }

                        qbUser.setPassword(QBSessionManager.getInstance().getToken());

                        saveOwnerUser(qbUser);

                        AppSession.startSession(qbUser);

                        return qbUser;
                    }
                })
                .observeOn(AndroidSchedulers.mainThread());

        return result;
    }
}
