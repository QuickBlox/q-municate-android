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

public class FriendDetailsLoader extends BaseLoader<FriendDetailsLoader.Result> {
    public static final int ID = 1;

    public FriendDetailsLoader(Context context) {
        super(context);
    }

    public static Arguments newArguments(int friendId) {
        Arguments arguments = new Arguments();
        arguments.friendId = friendId;
        return arguments;
    }

    @Override
    public Result performInBackground() throws QBResponseException {
        Arguments arguments = (Arguments) args;

        List<Integer> userIds = new ArrayList<Integer>();
        userIds.add(arguments.friendId);

        List<QBUser> users = QBUsers.getUsersByIDs(userIds, new QBPagedRequestBuilder(), new Bundle());

        Result result = new Result();
        result.friend = new Friend(users.get(0));
        return result;
    }

    private static class Arguments extends BaseLoader.Args {
        int friendId;
    }

    public static class Result {
        Friend friend;
    }
}