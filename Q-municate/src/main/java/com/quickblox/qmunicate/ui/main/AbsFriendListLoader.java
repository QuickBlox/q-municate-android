package com.quickblox.qmunicate.ui.main;


import android.content.Context;
import android.os.Bundle;

import com.quickblox.internal.core.request.QBPagedRequestBuilder;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.core.ui.BaseLoader;
import com.quickblox.qmunicate.model.Friend;

import java.util.List;

public abstract class AbsFriendListLoader extends BaseLoader<List<Friend>> {

    abstract protected List<Integer> getUserIds() throws Exception;

    public AbsFriendListLoader(Context context) {
        super(context);
    }

    public static Arguments newArguments(int page, int perPage) {
        Arguments arguments = new Arguments();
        arguments.page = page;
        arguments.perPage = perPage;
        return arguments;
    }

    @Override
    public List<Friend> performInBackground() throws Exception {
        Arguments arguments = (Arguments) args;
        List<Integer> userIds = getUserIds();
        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        requestBuilder.setPage(arguments.page);
        requestBuilder.setPerPage(arguments.perPage);

        Bundle params = new Bundle();
        List<QBUser> users = QBUsers.getUsersByIDs(userIds, requestBuilder, params);

        return Friend.createFriends(users);
    }

}
