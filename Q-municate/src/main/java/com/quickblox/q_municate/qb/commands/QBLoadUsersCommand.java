package com.quickblox.q_municate.qb.commands;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import com.quickblox.internal.core.request.QBPagedRequestBuilder;
import com.quickblox.module.users.QBUsers;
import com.quickblox.module.users.model.QBUser;
import com.quickblox.q_municate.core.command.ServiceCommand;
import com.quickblox.q_municate.model.AppSession;
import com.quickblox.q_municate.model.User;
import com.quickblox.q_municate.service.QBService;
import com.quickblox.q_municate.service.QBServiceConsts;
import com.quickblox.q_municate.utils.Consts;
import com.quickblox.q_municate.utils.FriendUtils;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class QBLoadUsersCommand extends ServiceCommand {

    public QBLoadUsersCommand(Context context, String successAction, String failAction) {
        super(context, successAction, failAction);
    }

    public static void start(Context context, String constraint) {
        Intent intent = new Intent(QBServiceConsts.LOAD_USERS_ACTION, null, context, QBService.class);
        intent.putExtra(QBServiceConsts.EXTRA_CONSTRAINT, constraint);
        context.startService(intent);
    }

    @Override
    public Bundle perform(Bundle extras) throws Exception {
        String constraint = (String) extras.getSerializable(QBServiceConsts.EXTRA_CONSTRAINT);

        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
        requestBuilder.setPage(Consts.FL_FRIENDS_PAGE_NUM);
        requestBuilder.setPerPage(Consts.FL_FRIENDS_PER_PAGE);

        Bundle requestParams = new Bundle();
        List<QBUser> userList = QBUsers.getUsersByFullName(constraint, requestBuilder, requestParams);
        Collections.sort(userList, new UserComparator());
        List<User> friendList = FriendUtils.createUsersList(userList);
        friendList.remove(FriendUtils.createUser(AppSession.getSession().getUser()));

        Bundle params = new Bundle();
        params.putSerializable(QBServiceConsts.EXTRA_FRIENDS, (java.io.Serializable) friendList);
        return params;
    }

    private class UserComparator implements Comparator<QBUser> {

        @Override
        public int compare(QBUser first, QBUser second) {
            if (first.getFullName() == null || second.getFullName() == null) {
                return 0;
            }
            return String.CASE_INSENSITIVE_ORDER.compare(first.getFullName(), second.getFullName());
        }
    }
}