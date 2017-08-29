package com.quickblox.q_municate.utils.helpers;

import android.content.Context;

import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.InviteContact;
import com.quickblox.q_municate_core.qb.helpers.QBChatHelper;
import com.quickblox.q_municate_core.qb.helpers.QBFriendListHelper;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.model.QMUser;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;

public class FriendListServiceManager {

    private static final String TAG = FriendListServiceManager.class.getSimpleName();
    private static FriendListServiceManager instance;
    private final Context context;
    private QBFriendListHelper friendListHelper;

    public static FriendListServiceManager getInstance(){
        if (instance == null){
            instance = new FriendListServiceManager();
        }

        return instance;
    }

    private FriendListServiceManager() {
        this.context = App.getInstance();
        this.friendListHelper = new QBFriendListHelper(context);
        QBChatHelper chatHelper = new QBChatHelper(context);
        chatHelper.initChatService();
        chatHelper.init(AppSession.getSession().getUser());
        friendListHelper.init(chatHelper);
    }

    public Observable<List<QMUser>> findContactsOnQb(List<InviteContact> friendsList) {
        final List<Observable<List<QMUser>>> observableArrayList = new ArrayList<>();

        ArrayList<String> friendsPhonesList = new ArrayList<>();
        ArrayList<String> friendsEmailsList = new ArrayList<>();
        ArrayList<String> friendsFacebookList = new ArrayList<>();

        for (InviteContact inviteContact : friendsList) {
            switch (inviteContact.getViaLabelType()) {
                case InviteContact.VIA_PHONE_TYPE:
                    friendsPhonesList.add(inviteContact.getId());
                    break;
                case InviteContact.VIA_EMAIL_TYPE:
                    friendsEmailsList.add(inviteContact.getId());
                    break;
                case InviteContact.VIA_FACEBOOK_TYPE:
                    friendsFacebookList.add(inviteContact.getId());
            }
        }

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        requestBuilder.setPerPage(ConstsCore.USERS_PER_PAGE);

        if (!friendsPhonesList.isEmpty()) {
            observableArrayList.add(QMUserService.getInstance().getUsersByPhoneNumbers(friendsPhonesList, requestBuilder, true));
        }

        if (!friendsEmailsList.isEmpty()) {
            observableArrayList.add(QMUserService.getInstance().getUsersByEmails(friendsEmailsList, requestBuilder, true));
        }

        if (!friendsFacebookList.isEmpty()) {
            observableArrayList.add(QMUserService.getInstance().getUsersByFacebookId(friendsFacebookList, requestBuilder, true));
        }

        Observable<List<QMUser>> result = Observable.from(observableArrayList)
                .flatMap(new Func1<Observable<List<QMUser>>, Observable<List<QMUser>>>() {
                    @Override
                    public Observable<List<QMUser>> call(Observable<List<QMUser>> listObservable) {
                        return listObservable;
                    }
                })
                .toList()
                .map(new Func1<List<List<QMUser>>, List<QMUser>>() {
                    @Override
                    public List<QMUser> call(List<List<QMUser>> lists) {
                        List<QMUser> realQbFriends = new ArrayList<>();

                        for (List<QMUser> tempList : lists){
                            realQbFriends.addAll(tempList);
                        }

                        return friendListHelper.getNotInvitedUsers(realQbFriends);
                    }
                });

        return result;
    }

    public Observable<List<Integer>> addFriends(final List<Integer> idsToAdding) {
        Observable<List<Integer>> result = Observable.create(new Observable.OnSubscribe<List<Integer>>() {
            @Override
            public void call(Subscriber<? super List<Integer>> subscriber) {
                try {
                    friendListHelper.addFriends(idsToAdding);
                    subscriber.onNext(idsToAdding);
                    subscriber.onCompleted();
                } catch (Exception e) {
                    subscriber.onError(e);
                }
            }
        });

        return result;
    }

    public Observable<List<Integer>> addFriend(final Integer userId) {
        return addFriends(Collections.singletonList(userId));
    }
}
