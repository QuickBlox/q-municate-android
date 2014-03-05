package com.quickblox.qmunicate.ui.friend;

import android.content.Context;
import android.os.Bundle;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.internal.core.request.QBPagedRequestBuilder;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.core.ui.BaseLoader;
import com.quickblox.qmunicate.model.Friend;

import java.util.ArrayList;
import java.util.List;

public class FriendDetailsLoader extends BaseLoader<Friend> {
    public static final int ID = 1;

    public static Arguments newArguments(int friendId) {
        Arguments arguments = new Arguments();
        arguments.friendId = friendId;
        return arguments;
    }

    public FriendDetailsLoader(Context context) {
        super(context);
    }

    @Override
    public Friend performInBackground() throws QBResponseException {
        Arguments arguments = (Arguments) args;

        List<Integer> userIds = new ArrayList<Integer>();
        userIds.add(arguments.friendId);

        List<QBUser> users = QBUsers.getUsersByIDs(userIds, new QBPagedRequestBuilder(), new Bundle());

        return new Friend(users.get(0));
    }

    private static class Arguments extends BaseLoader.Args {
        int friendId;
    }
}