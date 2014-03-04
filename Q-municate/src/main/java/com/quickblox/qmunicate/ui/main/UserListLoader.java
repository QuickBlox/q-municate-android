package com.quickblox.qmunicate.ui.main;

import android.content.Context;
import android.os.Bundle;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.internal.core.request.QBPagedRequestBuilder;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.core.ui.BaseLoader;
import com.quickblox.qmunicate.model.Friend;

import java.util.List;

public class UserListLoader extends BaseLoader<FriendListLoader.Result> {
    public static final int ID = 3;

    public UserListLoader(Context context) {
        super(context);
    }

    public static Arguments newArguments(String constraint, int page, int perPage) {
        Arguments arguments = new Arguments();
        arguments.constraint = constraint;
        arguments.page = page;
        arguments.perPage = perPage;
        return arguments;
    }

    @Override
    public FriendListLoader.Result performInBackground() throws QBResponseException {
        Arguments arguments = (Arguments) args;

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        requestBuilder.setPage(arguments.page);
        requestBuilder.setPerPage(arguments.perPage);

        Bundle params = new Bundle();
        List<QBUser> users = QBUsers.getUsersByFullName(arguments.constraint, requestBuilder, params);

        FriendListLoader.Result result = new FriendListLoader.Result();
        result.friends = Friend.toFriends(users);
        return result;
    }

    private static class Arguments extends BaseLoader.Args {
        String constraint;
        int page;
        int perPage;
    }
}