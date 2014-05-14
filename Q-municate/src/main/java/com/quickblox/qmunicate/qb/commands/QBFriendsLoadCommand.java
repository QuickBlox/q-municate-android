package com.quickblox.qmunicate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.internal.core.request.QBPagedRequestBuilder;
import com.quickblox.internal.module.custom.request.QBCustomObjectRequestBuilder;
import com.quickblox.module.custom.QBCustomObjects;
import com.quickblox.module.custom.model.QBCustomObject;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.core.command.ServiceCommand;
import com.quickblox.qmunicate.model.Friend;
import com.quickblox.qmunicate.service.QBService;
import com.quickblox.qmunicate.service.QBServiceConsts;
import com.quickblox.qmunicate.utils.Consts;

import java.util.ArrayList;
import java.util.List;

public class QBFriendsLoadCommand extends ServiceCommand {

    public QBFriendsLoadCommand(Context context, String successAction, String failAction) {
        super(context, successAction, failAction);
    }

    public static void start(Context context) {
        Intent intent = new Intent(QBServiceConsts.FRIENDS_LOAD_ACTION, null, context, QBService.class);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        List<Integer> userIds = getUserIds();
        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        requestBuilder.setPage(Consts.FL_FRIENDS_PAGE_NUM);
        requestBuilder.setPerPage(Consts.FL_FRIENDS_PER_PAGE);

        Bundle params = new Bundle();
        List<QBUser> usersList = QBUsers.getUsersByIDs(userIds, requestBuilder, params);
        params.putSerializable(QBServiceConsts.EXTRA_FRIENDS, (java.io.Serializable) Friend.createFriends(
                usersList));
        return params;
    }

    private List<Integer> getUserIds() throws Exception {
        QBCustomObjectRequestBuilder builder = new QBCustomObjectRequestBuilder();
        QBUser user = App.getInstance().getUser();
        builder.eq(Consts.FRIEND_FIELD_USER_ID, user.getId());
        builder.setPagesLimit(Consts.FL_FRIENDS_PER_PAGE);
        int pagesSkip = Consts.FL_FRIENDS_PER_PAGE * (Consts.FL_FRIENDS_PAGE_NUM - 1);
        builder.setPagesSkip(pagesSkip);

        List<QBCustomObject> objects = QBCustomObjects.getObjects(Consts.FRIEND_CLASS_NAME, builder);
        List<Integer> userIds = new ArrayList<Integer>();
        for (QBCustomObject o : objects) {
            userIds.add(Integer.parseInt((String) o.getFields().get(Consts.FRIEND_FIELD_FRIEND_ID)));
        }
        return userIds;
    }
}