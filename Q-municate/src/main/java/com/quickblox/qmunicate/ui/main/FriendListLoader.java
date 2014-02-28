package com.quickblox.qmunicate.ui.main;

import android.content.Context;
import android.os.Bundle;

import com.quickblox.internal.core.exception.QBResponseException;
import com.quickblox.internal.core.request.QBPagedRequestBuilder;
import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.core.ui.BaseLoader;
import com.quickblox.qmunicate.model.Friend;

import java.util.ArrayList;
import java.util.List;

public class FriendListLoader extends BaseLoader<FriendListLoader.Result> {
    public static final int ID = 0;

    private static final String CLASS_NAME = "Friend";
    private static final String FIELD_USER_ID = "user_id";
    private static final String FIELD_FRIEND_ID = "FriendID";

    public FriendListLoader(Context context) {
        super(context);
    }

    @Override
    public Result performInBackground() throws QBResponseException {
        Result result = new Result();

        QBCustomObjectRequestBuilder builder = new QBCustomObjectRequestBuilder();
        builder.eq(FIELD_USER_ID, App.getInstance().getUser().getId());
        builder.setPagesLimit(2);

        List<QBCustomObject> objects = QBCustomObjects.getObjects(CLASS_NAME, builder);
        List<Integer> userIds = new ArrayList<Integer>();
        for (QBCustomObject o : objects) {
            userIds.add(Integer.parseInt((String) o.getFields().get(FIELD_FRIEND_ID)));
        }
        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        requestBuilder.setPage(1);
        requestBuilder.setPerPage(10);

        Bundle params = new Bundle();
        List<QBUser> users = QBUsers.getUsersByIDs(userIds, requestBuilder, params);

        result.friends = Friend.toFriends(users);

        return result;
    }

    public static class Result {
        List<Friend> friends;
    }
}
