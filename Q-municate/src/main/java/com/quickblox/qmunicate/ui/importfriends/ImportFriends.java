package com.quickblox.qmunicate.ui.importfriends;

import android.app.Activity;
import android.content.Loader;
import android.os.AsyncTask;
import android.os.Bundle;

import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.core.ui.BaseLoader;
import com.quickblox.qmunicate.core.ui.LoaderHelper;
import com.quickblox.qmunicate.core.ui.LoaderManager;
import com.quickblox.qmunicate.core.ui.LoaderResult;
import com.quickblox.qmunicate.core.ui.OnLoadFinishedListener;
import com.quickblox.qmunicate.model.InviteFriend;
import com.quickblox.qmunicate.qb.QBAddFriendsCommand;
import com.quickblox.qmunicate.ui.base.QMLoaderHelper;
import com.quickblox.qmunicate.ui.utils.Consts;
import com.quickblox.qmunicate.ui.utils.FacebookHelper;
import com.quickblox.qmunicate.ui.utils.FriendsUtils;

import java.util.ArrayList;
import java.util.List;

public class ImportFriends implements OnLoadFinishedListener<List<QBUser>>, LoaderManager<List<QBUser>> {
    public Activity activity;

    private LoaderHelper<List<QBUser>> loaderHelper;
    private FacebookHelper facebookHelper;
    private FriendsUtils friendsUtils;
    private List<QBUser> users;
    private List<InviteFriend> friendsFacebookList;
    private List<InviteFriend> friendsContactsList;
    private boolean isGetFacebookFriends;
    private int expectedFriendsCallbacks;
    private int realFriendsCallbacks;

    public ImportFriends(Activity activity, FacebookHelper facebookHelper) {
        this.activity = activity;

        loaderHelper = new QMLoaderHelper<List<QBUser>>(activity, this, this);

        this.facebookHelper = facebookHelper;
        this.facebookHelper.loginWithFacebook();

        friendsUtils = new FriendsUtils(activity);
        users = new ArrayList<QBUser>();
    }

    public void startGetFriendsListTask(boolean isGetFacebookFriends) {
        this.isGetFacebookFriends = isGetFacebookFriends;
        new GetFriendsListTask().execute();
    }

    private void startUserListLoader(boolean isFacebookFriends, List<InviteFriend> friends) {
        if (isFacebookFriends) {
            runLoader(GetUsersByFBLoader.ID, GetUsersByFBLoader.newArguments(Consts.LOAD_PAGE_NUM, Consts.LOAD_PER_PAGE, getIDs(friends)));
        } else {
            runLoader(UsersByEmailLoader.ID, UsersByEmailLoader.newArguments(Consts.LOAD_PAGE_NUM, Consts.LOAD_PER_PAGE, getIDs(friends)));
        }
    }

    @Override
    public Loader<LoaderResult<List<QBUser>>> onLoaderCreate(int id, Bundle args) {
        switch (id) {
            case GetUsersByFBLoader.ID:
                return new GetUsersByFBLoader(activity);
            case UsersByEmailLoader.ID:
                return new UsersByEmailLoader(activity);
            default:
                return null;
        }
    }

    @Override
    public void onLoaderResult(int id, List<QBUser> data) {
        switch (id) {
            case GetUsersByFBLoader.ID:
                users.addAll(data);
                fiendsReceived();
                break;
            case UsersByEmailLoader.ID:
                users.addAll(data);
                fiendsReceived();
                break;
        }
    }

    @Override
    public void onLoaderException(int id, Exception e) {
        loaderHelper.onLoaderException(id, e);
    }

    @Override
    public <L extends Loader<?>> L getLoader(int id) {
        return loaderHelper.getLoader(id);
    }

    @Override
    public void onLoaderReset(Loader<LoaderResult<List<QBUser>>> loader) {
        loaderHelper.onLoaderReset(loader);
    }

    @Override
    public BaseLoader<List<QBUser>> runLoader(int id) {
        return loaderHelper.runLoader(id);
    }

    @Override
    public BaseLoader<List<QBUser>> runLoader(int id, BaseLoader.Args args) {
        return loaderHelper.runLoader(id, args);
    }

    private String[] getSelectedUsers() {
        ArrayList<String> arrayList = new ArrayList<String>();
        for (int i = 0; i < users.size(); i++) {
            QBUser user = users.get(i);
            arrayList.add(user.getId().toString());
        }
        return arrayList.toArray(new String[arrayList.size()]);
    }

    private ArrayList<String> getIDs(List<InviteFriend> friends) {
        ArrayList<String> idsList = new ArrayList<String>();
        for (InviteFriend friend : friends) {
            idsList.add(friend.getId());
        }
        return idsList;
    }

    private void getFacebookFriendsList() {
        Request.executeMyFriendsRequestAsync(Session.getActiveSession(), new Request.GraphUserListCallback() {

            @Override
            public void onCompleted(List<com.facebook.model.GraphUser> users, Response response) {
                friendsFacebookList = new ArrayList<InviteFriend>();
                for (com.facebook.model.GraphUser user : users) {
                    friendsFacebookList.add(new InviteFriend(user.getId(), user.getName(), user.getLink(), InviteFriend.VIA_FACEBOOK_TYPE, null, false));
                }
                startUserListLoader(true, friendsFacebookList);
            }
        });
    }

    public void fiendsReceived() {
        realFriendsCallbacks++;
        if (realFriendsCallbacks == expectedFriendsCallbacks) {
            String[] users = getSelectedUsers();
            if(users.length > 0) {
                QBAddFriendsCommand.start(activity, users);
            }
        }
    }

    private class GetFriendsListTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            if (isGetFacebookFriends) {
                expectedFriendsCallbacks++;
                getFacebookFriendsList();
            }
        }

        @Override
        protected Void doInBackground(Void... params) {
            expectedFriendsCallbacks++;
            friendsContactsList = friendsUtils.getContactsWithEmail();
            startUserListLoader(false, friendsContactsList);
            return null;
        }
    }
}